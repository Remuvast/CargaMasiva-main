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

            String sqlValidaTramite = """
                SELECT 1
                FROM solicitudes so
                JOIN solicitantes sl ON sl.id = so.solicitantes_id
                WHERE so.numero_tramite = ?
                AND sl.numero_identificacion = ?
            """;

            try (PreparedStatement psValidaCedula = conn.prepareStatement(sqlValidaCedula);
                PreparedStatement psValidaTramite = conn.prepareStatement(sqlValidaTramite);

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
                    INSERT INTO solicitudes_datos_estudio (
                        solicitudes_id,
                        programas_regiones_niv_est_id,
                        catalogos_nivel_estudio_id,
                        areas_estudio_id,
                        carreras_id,
                        universidades_id,
                        ubicaciones_geograficas_id,
                        catalogos_titulo_id,
                        catalogos_idioma_estudio_id,
                        fecha_inicio_estudios,
                        fecha_fin_estudios,
                        duracion_estudios,
                        estado
                    )
                    SELECT so.id, prne.id, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
                    FROM solicitudes so
                    JOIN solicitantes sl ON sl.id = so.solicitantes_id
                    JOIN programas p ON p.id = so.programas_id
                    JOIN programas_regiones pr ON pr.programas_id = p.id
                    JOIN programas_regiones_niv_est prne ON prne.programas_regiones_id = pr.id
                    WHERE sl.numero_identificacion = ?
                      AND so.numero_tramite = ?
                    ON CONFLICT (solicitudes_id)
                    DO UPDATE SET
                        catalogos_nivel_estudio_id = EXCLUDED.catalogos_nivel_estudio_id,
                        areas_estudio_id = EXCLUDED.areas_estudio_id,
                        carreras_id = EXCLUDED.carreras_id,
                        universidades_id = EXCLUDED.universidades_id,
                        ubicaciones_geograficas_id = EXCLUDED.ubicaciones_geograficas_id,
                        catalogos_titulo_id = EXCLUDED.catalogos_titulo_id,
                        catalogos_idioma_estudio_id = EXCLUDED.catalogos_idioma_estudio_id,
                        fecha_inicio_estudios = EXCLUDED.fecha_inicio_estudios,
                        fecha_fin_estudios = EXCLUDED.fecha_fin_estudios,
                        duracion_estudios = EXCLUDED.duracion_estudios,
                        estado = EXCLUDED.estado
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
                    INSERT INTO solicitudes_rubros (
                        solicitudes_id,
                        catalogos_periodicidad_id,
                        presupuesto_referencial,
                        programas_reg_niv_est_rub_id,
                        estado,
                        valor_maximo_financiamiento
                    )
                    SELECT so.id, null, ?, prner.id, true, ?
                    FROM solicitudes so
                    JOIN solicitantes sl ON sl.id = so.solicitantes_id
                    JOIN programas p ON p.id = so.programas_id
                    JOIN programas_regiones pr ON pr.programas_id = p.id
                    JOIN programas_regiones_niv_est prne ON prne.programas_regiones_id = pr.id
                    JOIN programas_reg_niv_est_rub prner ON prner.programas_regiones_niv_est_id = prne.id
                    JOIN rubros r ON r.id = prner.rubros_id
                    WHERE p.nombre_corto = ?
                      AND prne.catalogos_niveles_estudio_id = ?
                      AND sl.numero_identificacion = ?
                      AND so.numero_tramite = ?
                      AND r.nombre = ?
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

                    // ================= VALIDACIÓN PRESUPUESTO =================
                    String presupuestoStr = getCellString(fila.getCell(25));

                    if (presupuestoStr == null || presupuestoStr.isBlank()) {
                        errores.add(new ValidationError(
                                i + 1,
                                "Presupuesto Referencial",
                                "Es obligatorio"
                        ));
                    } else {

                        String normalizado = presupuestoStr.trim();

                        // Normalización (igual que tu método)
                        if (normalizado.contains(",") && !normalizado.contains(".")) {
                            normalizado = normalizado.replace(",", ".");
                        }
                        if (normalizado.contains(",") && normalizado.contains(".")) {
                            normalizado = normalizado.replace(",", "");
                        }

                        try {
                            BigDecimal valor = new BigDecimal(normalizado);

                            if (valor.compareTo(BigDecimal.ZERO) <= 0) {
                                errores.add(new ValidationError(
                                        i + 1,
                                        "Presupuesto Referencial",
                                        "Debe ser mayor a 0"
                                ));
                            }

                        } catch (Exception e) {
                            errores.add(new ValidationError(
                                    i + 1,
                                    "Presupuesto Referencial",
                                    "Formato inválido (ej: 1234.56)"
                            ));
                        }
                    }

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

                    // Validación número de trámite vs cédula
                    String tramite = getCellString(fila.getCell(4));

                    if (tramite != null && !tramite.isBlank()
                            && cedula != null && !cedula.isBlank()) {

                        if (!existeTramite(psValidaTramite, tramite, cedula)) {
                        errores.add(new ValidationError(
                                    i + 1,
                                    "Número de trámite",
                                    "No existe o no pertenece a la cédula"
                            ));
                        }
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
                    if (isRowEmpty(fila)) continue;

                    String cedula = getCellString(fila.getCell(0));
                    String tramite = getCellString(fila.getCell(4));
                    String nombrePrograma = getCellString(fila.getCell(3));
                    String nombreRubro = getCellString(fila.getCell(26));

                    Long nivel = getCellLong(fila.getCell(10));
                    Long area = getCellLong(fila.getCell(13));
                    Long carrera = getCellLong(fila.getCell(14));
                    Long universidad = getCellLong(fila.getCell(15));
                    Long pais = getCellLong(fila.getCell(16));
                    Long titulo = getCellLong(fila.getCell(17));
                    Long idioma = getCellLong(fila.getCell(18));

                    String presupuesto = getCellString(fila.getCell(25));
                    if (presupuesto.isBlank()) presupuesto = "0";

                    // ps1
                    ps1.setLong(1, getCellLong(fila.getCell(5)));
                    ps1.setString(2, getCellString(fila.getCell(6)));
                    ps1.setString(3, getCellString(fila.getCell(7)));
                    ps1.setString(4, cedula);
                    ps1.setString(5, tramite);
                    ps1.addBatch();

                    // ps2
                    ps2.setString(1, getCellString(fila.getCell(8)));
                    ps2.setString(2, cedula);
                    ps2.setString(3, tramite);
                    ps2.addBatch();

                    // ps3
                    ps3.setLong(1, nivel);
                    ps3.setLong(2, area);
                    ps3.setLong(3, carrera);
                    ps3.setLong(4, universidad);
                    ps3.setLong(5, pais);
                    ps3.setLong(6, titulo);
                    ps3.setLong(7, idioma);
                    ps3.setTimestamp(8, null);
                    ps3.setTimestamp(9, null);
                    ps3.setString(10, "");
                    ps3.setBoolean(11, true);
                    ps3.setString(12, cedula);
                    ps3.setString(13, tramite);
                    ps3.addBatch();

                    // ps4
                    ps4.setTimestamp(1, null);
                    ps4.setTimestamp(2, null);
                    ps4.setString(3, "");
                    ps4.setTimestamp(4, null);
                    ps4.setTimestamp(5, null);
                    ps4.setString(6, "");
                    ps4.setBigDecimal(7, parseBigDecimal(presupuesto));
                    ps4.setString(8, cedula);
                    ps4.setString(9, tramite);
                    ps4.addBatch();

                    // ps5
                    ps5.setBigDecimal(1, parseBigDecimal(presupuesto));
                    ps5.setBigDecimal(2, parseBigDecimal(presupuesto));
                    ps5.setString(3, nombrePrograma);
                    ps5.setLong(4, nivel);
                    ps5.setString(5, cedula);
                    ps5.setString(6, tramite);
                    ps5.setString(7, nombreRubro);
                    ps5.addBatch();

                    countBatch++;
                    totalProcesados++;

                    if (countBatch % batchSize == 0) {
                        ps1.executeBatch();
                        ps2.executeBatch();
                        ps3.executeBatch();
                        ps4.executeBatch();
                        ps5.executeBatch();
                        conn.commit();
                    }
                }

                ps1.executeBatch();
                ps2.executeBatch();
                ps3.executeBatch();
                ps4.executeBatch();
                ps5.executeBatch();
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

    private boolean existeTramite(PreparedStatement ps, String tramite, String cedula) throws SQLException {
        ps.setString(1, tramite);
        ps.setString(2, cedula);
        try (ResultSet rs = ps.executeQuery()) {
            return rs.next();
        }
    }

    private BigDecimal parseBigDecimal(String valor) {
        try {
            if (valor == null || valor.isBlank()) return BigDecimal.ZERO;

            valor = valor.trim();

            // Caso 1: viene con coma decimal (formato latino)
            if (valor.contains(",") && !valor.contains(".")) {
                valor = valor.replace(",", ".");
            }

            // Caso 2: viene con miles (1,234.56)
            if (valor.contains(",") && valor.contains(".")) {
                valor = valor.replace(",", "");
            }

            return new BigDecimal(valor);

        } catch (Exception e) {
            throw new RuntimeException("Valor inválido en presupuesto: " + valor);
        }
    }    

    private String getCellString(Cell cell) {
        try {
            if (cell == null) return "";
            switch (cell.getCellType()) {
                case STRING: return cell.getStringCellValue().trim();
                case NUMERIC: return BigDecimal.valueOf(cell.getNumericCellValue()).toPlainString();
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