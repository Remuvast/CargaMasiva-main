package com.becas.exceluploader.service;

import com.becas.exceluploader.validation.ExcelRowValidator;
import com.becas.exceluploader.validation.ValidationError;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

@Service
public class ExcelProcessingService {

    private final DataSource dataSource;

    public ExcelProcessingService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public String procesarExcel(MultipartFile file) {

        StringBuilder resultado = new StringBuilder();
        int totalProcesados = 0;
        int totalErrores = 0;
        int batchSize = 300;

        try (
                Connection conn = dataSource.getConnection();
                InputStream input = file.getInputStream();
                Workbook workbook = new XSSFWorkbook(input)
        ) {

            conn.setAutoCommit(false);

            Sheet hoja = workbook.getSheetAt(0);
            int lastRow = getLastDataRow(hoja);

            // ================= 🔥 CARGA EN MEMORIA =================

            Set<Long> nivelesValidos = cargarIds(conn,
                    "SELECT id FROM catalogos WHERE tipos_catalogos_id = 11");

            Set<Long> areasValidas = cargarIds(conn, "SELECT id FROM areas_estudio");
            Set<Long> carrerasValidas = cargarIds(conn, "SELECT id FROM carreras");
            Set<Long> universidadesValidas = cargarIds(conn, "SELECT id FROM universidades");
            Set<Long> paisesValidos = cargarIds(conn, "SELECT id FROM ubicaciones_geograficas");
            Set<Long> titulosValidos = cargarIds(conn, "SELECT id FROM catalogos");
            Set<Long> idiomasValidos = cargarIds(conn, "SELECT id FROM catalogos");

            // =======================================================

            String sqlValidaCedula = "SELECT 1 FROM solicitantes WHERE numero_identificacion = ?";

            try (PreparedStatement psValidaCedula = conn.prepareStatement(sqlValidaCedula);

                 PreparedStatement ps1 = conn.prepareStatement(""" 
                    UPDATE solicitudes so
                    SET catalogos_historial_becas_id = ?,
                        resultado = ?,
                        criterio_tecnico = ?
                    FROM solicitantes sl
                    WHERE sl.numero_identificacion = ?
                      AND so.numero_tramite = ?
                      AND sl.id = so.solicitantes_id
                 """);

                 PreparedStatement ps2 = conn.prepareStatement(""" 
                    UPDATE solicitudes_programas_requisitos spr
                    SET resultado = ?
                    FROM solicitudes so, solicitantes sl, programas_requisitos pr
                    WHERE so.id = spr.solicitudes_id
                      AND sl.id = so.solicitantes_id
                      AND spr.programas_requisitos_id = pr.id
                      AND pr.requisito_obligatorio = false
                      AND sl.numero_identificacion = ?
                      AND so.numero_tramite = ?
                 """);

                 PreparedStatement ps3 = conn.prepareStatement(""" 
                    INSERT INTO solicitudes_datos_estudio (...)
                 """);

                 PreparedStatement ps4 = conn.prepareStatement(""" 
                    UPDATE solicitudes so
                    SET fecha_inicio_financiamiento = ?,
                        fecha_fin_financiamiento = ?,
                        duracion_financiamiento = ?,
                        fecha_inicio_financiamiento_ac = ?,
                        fecha_fin_financiamiento_ac = ?,
                        duracion_financiamiento_ac = ?,
                        presupuesto_beca = ?
                    FROM solicitantes sl
                    WHERE sl.numero_identificacion = ?
                      AND so.numero_tramite = ?
                      AND sl.id = so.solicitantes_id
                 """);

                 PreparedStatement ps5 = conn.prepareStatement(""" 
                    INSERT INTO solicitudes_rubros (...)
                 """)
            ) {

                boolean hayErroresGlobales = false;

                // ================= 🔥 FASE 1: VALIDACIÓN =================

                for (int i = 6; i <= lastRow; i++) {

                    Row fila = hoja.getRow(i);

                    if (isRowEmpty(fila)) {
                        resultado.append("⚠️ Fila ").append(i + 1)
                            .append(" vacía (omitir o borrar fila vacia)\n");
                        continue;
                    }

                    List<ValidationError> errores = new ArrayList<>();

                    // Validaciones base
                    errores.addAll(ExcelRowValidator.validarFila(fila, i + 1));

                    // FK VALIDACIONES
                    Long nivel = getCellLong(fila.getCell(10));
                    if (nivel != null && nivel > 0 && !nivelesValidos.contains(nivel)) {
                        errores.add(new ValidationError(i + 1, "Nivel de estudio", "No existe en catálogoe"));
                    }

                    Long area = getCellLong(fila.getCell(13));
                    if (area != null && area > 0 && !areasValidas.contains(area)) {
                        errores.add(new ValidationError(i + 1, "Área de estudio", "No existe en catálogo"));
                    }

                    Long carrera = getCellLong(fila.getCell(14));
                    if (carrera != null && carrera > 0 && !carrerasValidas.contains(carrera)) {
                        errores.add(new ValidationError(i + 1, "Carrera", "No existe en catálogo"));
                    }

                    Long universidad = getCellLong(fila.getCell(15));
                    if (universidad != null && universidad > 0 && !universidadesValidas.contains(universidad)) {
                        errores.add(new ValidationError(i + 1, "Institución Educativa", "No existe en catálogo"));
                    }

                    Long pais = getCellLong(fila.getCell(16));
                    if (pais != null && pais > 0 && !paisesValidos.contains(pais)) {
                        errores.add(new ValidationError(i + 1, "País", "No existe en catálogo"));
                    }

                    Long titulo = getCellLong(fila.getCell(17));
                    if (titulo != null && titulo > 0 && !titulosValidos.contains(titulo)) {
                        errores.add(new ValidationError(i + 1, "Título", "No existe en catálogo"));
                    }

                    Long idioma = getCellLong(fila.getCell(18));
                    if (idioma != null && idioma > 0 && !idiomasValidos.contains(idioma)) {
                        errores.add(new ValidationError(i + 1, "Idioma", "No existe en catálogo"));
                    }

                    // Validación cédula (solo si tiene valor)
                    String cedula = getCellString(fila.getCell(0));
                    if (cedula != null && !cedula.isBlank() && !existeCedula(psValidaCedula, cedula)) {
                        errores.add(new ValidationError(i + 1, "Cédula", "No existe en el Sistema"));
                    }
                    
                    // ================= RESULTADO POR FILA =================
                    
                    if (!errores.isEmpty()) {
                        hayErroresGlobales = true;

                        for (ValidationError e : errores) {
                            resultado.append("❌ ").append(e.toString()).append("\n");
                        }

                        totalErrores++;
                    }
                }

                // 🔥 VALIDACION DE ERRORES
                resultado.append("\n📊 VALIDACIÓN:");

                if (totalErrores > 0) {
                    resultado.append("\n❌ Filas con errores encontrados: ").append(totalErrores).append("\n");
                } else {
                    resultado.append("\n✅ Validación exitosa sin errores\n");
                }

                // 🔥 SI HAY ERRORES → CANCELA TODO
                if (hayErroresGlobales) {
                    conn.rollback();
                    resultado.append("\n⛔ PROCESO CANCELADO: Existen errores. No se guardó nada. Favor corregir el archivo excel e intentarlo nuevamente");
                    return resultado.toString();
                }

                // ================= 🔥 FASE 2: EJECUCIÓN =================

                int countBatch = 0;

                for (int i = 6; i <= lastRow; i++) {

                    Row fila = hoja.getRow(i);
                    if (isRowEmpty(fila)) {
                        continue;
                    }

                    String cedula = getCellString(fila.getCell(0));
                    String tramite = getCellString(fila.getCell(4));

                    Long nivel = getCellLong(fila.getCell(10));
                    String presupuesto = getCellString(fila.getCell(25));

                    // (Aquí mantienes TODO tu seteo actual sin tocar)

                    ps1.setLong(1, getCellLong(fila.getCell(5)));
                    ps1.setString(2, getCellString(fila.getCell(6)));
                    ps1.setString(3, getCellString(fila.getCell(7)));
                    ps1.setString(4, cedula);
                    ps1.setString(5, tramite);
                    ps1.addBatch();

                    countBatch++;
                    totalProcesados++;

                    if (countBatch % batchSize == 0) {
                        ps1.executeBatch();
                        conn.commit();
                    }
                }

                ps1.executeBatch();
                conn.commit();
            }

        } catch (Exception e) {
            resultado.append("❌ ERROR GENERAL: ").append(e.getMessage());
        }

        resultado.append("\n\n🚀 EJECUCIÓN:");
        resultado.append("\n✔️ Procesados: ").append(totalProcesados);

        return resultado.toString();
    }

    // ================= HELPERS =================

    private Set<Long> cargarIds(Connection conn, String sql) throws SQLException {
        Set<Long> set = new HashSet<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                set.add(rs.getLong(1));
            }
        }
        return set;
    }

    private int getLastDataRow(Sheet sheet) {
        for (int i = sheet.getLastRowNum(); i >= 0; i--) {
            Row row = sheet.getRow(i);
            if (!isRowEmpty(row)) return i;
        }
        return 0;
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            if (!getCellString(row.getCell(i)).isBlank()) return false;
        }
        return true;
    }

    private boolean existeCedula(PreparedStatement ps, String cedula) throws SQLException {
        ps.setString(1, cedula);
        try (ResultSet rs = ps.executeQuery()) {
            return rs.next();
        }
    }

    private String getCellString(Cell cell) {
        try {
            if (cell == null) return "";
            switch (cell.getCellType()) {
                case STRING: return cell.getStringCellValue().trim();
                case NUMERIC: return String.valueOf((long) cell.getNumericCellValue());
                default: return "";
            }
        } catch (Exception e) {
            return "";
        }
    }

    private Long getCellLong(Cell cell) {
        try {
            if (cell == null) return 0L;
            if (cell.getCellType() == CellType.NUMERIC)
                return (long) cell.getNumericCellValue();
            return Long.parseLong(cell.getStringCellValue().trim());
        } catch (Exception e) {
            return 0L;
        }
    }
}