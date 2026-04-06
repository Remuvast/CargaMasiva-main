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
import java.util.List;

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

            String sqlValidaCedula = "SELECT 1 FROM solicitantes WHERE numero_identificacion = ?";
            String sqlCatalogo = "SELECT 1 FROM catalogos WHERE id = ?";
            String sqlNivelEstudio = "SELECT 1 FROM catalogos WHERE id = ? AND tipos_catalogos_id = 11";

            try (
                    PreparedStatement psValidaCedula = conn.prepareStatement(sqlValidaCedula);
                    PreparedStatement psCatalogo = conn.prepareStatement(sqlCatalogo);
                    PreparedStatement psNivelEstudio = conn.prepareStatement(sqlNivelEstudio);

                    PreparedStatement ps1 = conn.prepareStatement(""" UPDATE solicitudes so SET catalogos_historial_becas_id=?, resultado=?, criterio_tecnico=? FROM solicitantes sl WHERE sl.numero_identificacion=? AND so.numero_tramite=? AND sl.id=so.solicitantes_id """);

                    PreparedStatement ps2 = conn.prepareStatement(""" UPDATE solicitudes_programas_requisitos spr SET resultado=? FROM solicitudes so, solicitantes sl, programas_requisitos pr WHERE so.id=spr.solicitudes_id AND sl.id=so.solicitantes_id AND spr.programas_requisitos_id=pr.id AND pr.requisito_obligatorio=false AND sl.numero_identificacion=? AND so.numero_tramite=? """);

                    PreparedStatement ps3 = conn.prepareStatement(""" INSERT INTO solicitudes_datos_estudio (solicitudes_id,programas_regiones_niv_est_id,catalogos_nivel_estudio_id,areas_estudio_id,carreras_id,universidades_id,ubicaciones_geograficas_id,catalogos_titulo_id,catalogos_idioma_estudio_id,fecha_inicio_estudios,fecha_fin_estudios,duracion_estudios,estado)
                    SELECT so.id, prne.id, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? FROM solicitudes so JOIN solicitantes sl ON sl.id=so.solicitantes_id JOIN programas p ON p.id=so.programas_id JOIN programas_regiones pr ON pr.programas_id=p.id JOIN programas_regiones_niv_est prne ON prne.programas_regiones_id=pr.id WHERE sl.numero_identificacion=? AND so.numero_tramite=? ON CONFLICT (solicitudes_id) DO UPDATE SET catalogos_nivel_estudio_id=EXCLUDED.catalogos_nivel_estudio_id """);

                    PreparedStatement ps4 = conn.prepareStatement(""" UPDATE solicitudes so SET fecha_inicio_financiamiento=?,fecha_fin_financiamiento=?,duracion_financiamiento=?,fecha_inicio_financiamiento_ac=?,fecha_fin_financiamiento_ac=?,duracion_financiamiento_ac=?,presupuesto_beca=? FROM solicitantes sl WHERE sl.numero_identificacion=? AND so.numero_tramite=? AND sl.id=so.solicitantes_id """);

                    PreparedStatement ps5 = conn.prepareStatement(""" INSERT INTO solicitudes_rubros (solicitudes_id,catalogos_periodicidad_id,presupuesto_referencial,programas_reg_niv_est_rub_id,estado,valor_maximo_financiamiento)
                    SELECT so.id,null,?,prner.id,true,? FROM solicitudes so JOIN solicitantes sl ON sl.id=so.solicitantes_id JOIN programas p ON p.id=so.programas_id JOIN programas_regiones pr ON pr.programas_id=p.id JOIN programas_regiones_niv_est prne ON prne.programas_regiones_id=pr.id JOIN programas_reg_niv_est_rub prner ON prner.programas_regiones_niv_est_id=prne.id JOIN rubros r ON r.id=prner.rubros_id WHERE p.nombre_corto=? AND prne.catalogos_niveles_estudio_id=? AND sl.numero_identificacion=? AND so.numero_tramite=? AND r.nombre=? """)
            ) {

                int countBatch = 0;

                for (int i = 6; i <= lastRow; i++) {

                    Row fila = hoja.getRow(i);

                    if (isRowEmpty(fila)) continue;

                    try {

                        // ✅ VALIDACIÓN BASE
                        List<ValidationError> errores = ExcelRowValidator.validarFila(fila, i + 1);
                        if (!errores.isEmpty()) {
                            errores.forEach(e -> resultado.append("❌ ").append(e).append("\n"));
                            totalErrores++;
                            continue;
                        }

                        String cedula = getCellString(fila.getCell(0));
                        String numeroTramite = getCellString(fila.getCell(4));
                        String nombreRubro = getCellString(fila.getCell(26));

                        if (!existeCedula(psValidaCedula, cedula)) {
                            resultado.append("❌ Fila ").append(i + 1).append(" | Cédula NO existe\n");
                            totalErrores++;
                            continue;
                        }

                        // 🔥 DATOS
                        Long nivelEstudio = getCellLong(fila.getCell(10));
                        Long campoDetallado = getCellLong(fila.getCell(13));
                        Long carrera = getCellLong(fila.getCell(14));
                        Long universidad = getCellLong(fila.getCell(15));
                        Long pais = getCellLong(fila.getCell(16));
                        Long titulo = getCellLong(fila.getCell(17));
                        Long idioma = getCellLong(fila.getCell(18));

                        // 🔥 VALIDACIÓN DE CATÁLOGOS
                        boolean valido = true;

                        if (!existeCatalogo(psNivelEstudio, nivelEstudio, "Nivel estudio", i + 1, resultado)) valido = false;
                        if (!existeCatalogo(psCatalogo, campoDetallado, "Área", i + 1, resultado)) valido = false;
                        if (!existeCatalogo(psCatalogo, carrera, "Carrera", i + 1, resultado)) valido = false;
                        if (!existeCatalogo(psCatalogo, universidad, "Universidad", i + 1, resultado)) valido = false;
                        if (!existeCatalogo(psCatalogo, pais, "País", i + 1, resultado)) valido = false;
                        if (!existeCatalogo(psCatalogo, titulo, "Título", i + 1, resultado)) valido = false;
                        if (!existeCatalogo(psCatalogo, idioma, "Idioma", i + 1, resultado)) valido = false;

                        if (!valido) {
                            totalErrores++;
                            continue;
                        }

                        // 🔥 AQUÍ SIGUE TU LÓGICA ORIGINAL (SIN CAMBIOS)

                        Long historialBecas = getCellLong(fila.getCell(5));
                        String resultadoVal = getCellString(fila.getCell(6));
                        String criterio = getCellString(fila.getCell(7));
                        String analistaResultado = getCellString(fila.getCell(8));

                        String fechaInicioEstudios = getCellString(fila.getCell(19));
                        String fechaFinEstudios = getCellString(fila.getCell(20));
                        String duracionEstudios = getCellString(fila.getCell(21));

                        String fechaInicioFin = getCellString(fila.getCell(22));
                        String fechaFinFin = getCellString(fila.getCell(23));
                        String duracionFin = getCellString(fila.getCell(24));

                        String presupuesto = getCellString(fila.getCell(25));
                        if (presupuesto.isBlank()) presupuesto = "0";

                        String nombrePrograma = getCellString(fila.getCell(3));

                        // BATCH
                        ps1.setLong(1, historialBecas);
                        ps1.setString(2, resultadoVal);
                        ps1.setString(3, criterio);
                        ps1.setString(4, cedula);
                        ps1.setString(5, numeroTramite);
                        ps1.addBatch();

                        ps2.addBatch();
                        ps3.addBatch();
                        ps4.addBatch();
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

                    } catch (Exception e) {
                        totalErrores++;
                        resultado.append("❌ Fila ").append(i + 1)
                                .append(" | Error: ").append(e.getMessage()).append("\n");
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

        resultado.append("\n\n📊 RESUMEN:");
        resultado.append("\n✔️ Procesados: ").append(totalProcesados);
        resultado.append("\n❌ Errores: ").append(totalErrores);

        return resultado.toString();
    }

    // ================= HELPERS =================

    private boolean existeCatalogo(PreparedStatement ps, Long id, String campo, int fila, StringBuilder resultado) throws SQLException {
        if (id == null || id == 0) return false;
        ps.setLong(1, id);
        try (ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                resultado.append("❌ Fila ").append(fila)
                        .append(" | ").append(campo)
                        .append(" inválido: ").append(id).append("\n");
                return false;
            }
        }
        return true;
    }

    private boolean existeCedula(PreparedStatement ps, String cedula) throws SQLException {
        ps.setString(1, cedula);
        try (ResultSet rs = ps.executeQuery()) {
            return rs.next();
        }
    }

    private int getLastDataRow(Sheet sheet) {
        for (int i = sheet.getLastRowNum(); i >= 0; i--) {
            if (!isRowEmpty(sheet.getRow(i))) return i;
        }
        return 0;
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && !getCellString(cell).isBlank()) return false;
        }
        return true;
    }

    private String getCellString(Cell cell) {
        try {
            if (cell == null) return "";
            if (cell.getCellType() == CellType.STRING) return cell.getStringCellValue().trim();
            if (cell.getCellType() == CellType.NUMERIC) return String.valueOf((long) cell.getNumericCellValue());
            return "";
        } catch (Exception e) {
            return "";
        }
    }

    private Long getCellLong(Cell cell) {
        try {
            if (cell == null) return 0L;
            if (cell.getCellType() == CellType.NUMERIC) return (long) cell.getNumericCellValue();
            if (cell.getCellType() == CellType.STRING) return Long.parseLong(cell.getStringCellValue().trim());
            return 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    private Timestamp parseTimestamp(String fecha) {
        try {
            if (fecha == null || fecha.isBlank()) return null;
            if (fecha.length() == 10) return Timestamp.valueOf(fecha + " 00:00:00");
            return Timestamp.valueOf(fecha);
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal parseBigDecimal(String valor) {
        try {
            if (valor == null || valor.isBlank()) return BigDecimal.ZERO;
            return new BigDecimal(valor.replace(",", "").trim());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}