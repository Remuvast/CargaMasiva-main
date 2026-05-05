package com.becas.exceluploader.validation;

import com.becas.exceluploader.util.ScriptGeneratorUtil;
import org.apache.poi.ss.usermodel.Row;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;

public class ExcelRowValidator {

    public static List<ValidationError> validarFila(Row fila, int rowIndex) {

        List<ValidationError> errores = new ArrayList<>();

        // ================= CAMPOS =================
        String cedula = ScriptGeneratorUtil.getCellString(fila.getCell(0));
        String nombrePrograma = ScriptGeneratorUtil.getCellString(fila.getCell(3));
        String tramite = ScriptGeneratorUtil.getCellString(fila.getCell(4));
        Long historialBecas = ScriptGeneratorUtil.getCellLong(fila.getCell(5));
        String resultado = ScriptGeneratorUtil.getCellString(fila.getCell(6));
        String criterio = ScriptGeneratorUtil.getCellString(fila.getCell(7));
        String analista = ScriptGeneratorUtil.getCellString(fila.getCell(8));

        Long nivelEstudio = ScriptGeneratorUtil.getCellLong(fila.getCell(10));
        Long campoDetallado = ScriptGeneratorUtil.getCellLong(fila.getCell(13));
        Long carrera = ScriptGeneratorUtil.getCellLong(fila.getCell(14));
        Long universidad = ScriptGeneratorUtil.getCellLong(fila.getCell(15));
        Long pais = ScriptGeneratorUtil.getCellLong(fila.getCell(16));
        Long titulo = ScriptGeneratorUtil.getCellLong(fila.getCell(17));
        Long idioma = ScriptGeneratorUtil.getCellLong(fila.getCell(18));

        Cell cellInicioEst = fila.getCell(19);
        Cell cellFinEst = fila.getCell(20);

        String fechaInicioEstudios = ScriptGeneratorUtil.getCellString(cellInicioEst);
        String fechaFinEstudios = ScriptGeneratorUtil.getCellString(cellFinEst);
        String duracionEstudios = ScriptGeneratorUtil.getCellString(fila.getCell(21));

        Cell cellInicioFin = fila.getCell(22);
        Cell cellFinFin = fila.getCell(23);

        String fechaInicioFin = ScriptGeneratorUtil.getCellString(cellInicioFin);
        String fechaFinFin = ScriptGeneratorUtil.getCellString(cellFinFin);
        String duracionFin = ScriptGeneratorUtil.getCellString(fila.getCell(24));

        String presupuesto = ScriptGeneratorUtil.getCellString(fila.getCell(25));
        String rubro = ScriptGeneratorUtil.getCellString(fila.getCell(26));

        // ================= VALIDACIONES OBLIGATORIAS =================

        validarTexto(errores, cedula, rowIndex, "Cédula");
        validarTexto(errores, nombrePrograma, rowIndex, "Programa");
        validarTexto(errores, tramite, rowIndex, "Trámite");
        validarNumero(errores, historialBecas, rowIndex, "Historial Becas");
        validarTexto(errores, resultado, rowIndex, "Resultado");
        validarTexto(errores, criterio, rowIndex, "Criterio Técnico");
        validarTexto(errores, analista, rowIndex, "Analista");

        validarTexto(errores, ScriptGeneratorUtil.getCellString(fila.getCell(10)), rowIndex, "Nivel de estudio");
        validarTexto(errores, ScriptGeneratorUtil.getCellString(fila.getCell(13)), rowIndex, "Campo detallado");
        validarTexto(errores, ScriptGeneratorUtil.getCellString(fila.getCell(14)), rowIndex, "Carrera");
        validarTexto(errores, ScriptGeneratorUtil.getCellString(fila.getCell(15)), rowIndex, "Universidad");
        validarTexto(errores, ScriptGeneratorUtil.getCellString(fila.getCell(16)), rowIndex, "País");
        validarTexto(errores, ScriptGeneratorUtil.getCellString(fila.getCell(17)), rowIndex, "Título");
        validarTexto(errores, ScriptGeneratorUtil.getCellString(fila.getCell(18)), rowIndex, "Idioma");

        validarTexto(errores, fechaInicioEstudios, rowIndex, "Fecha inicio estudios");
        validarTexto(errores, fechaFinEstudios, rowIndex, "Fecha fin estudios");
        validarTexto(errores, duracionEstudios, rowIndex, "Duración estudios");

        validarTexto(errores, fechaInicioFin, rowIndex, "Fecha inicio financiamiento");
        validarTexto(errores, fechaFinFin, rowIndex, "Fecha fin financiamiento");
        validarTexto(errores, duracionFin, rowIndex, "Duración financiamiento");

        validarTexto(errores, presupuesto, rowIndex, "Presupuesto");
        validarTexto(errores, rubro, rowIndex, "Rubro");

        // ================= VALIDACIÓN HISTORIAL BECAS Y RESULTADO =================
        
        // VALIDACIÓN RESULTADO (solo N o P)
        if (resultado != null && !resultado.isBlank()) {
            if (!resultado.equalsIgnoreCase("N") && !resultado.equalsIgnoreCase("P")) {
                errores.add(new ValidationError(
                        rowIndex,
                        "Resultado",
                        "Solo permite valores: N o P"
             ));
            }
        }
        // FIN VALIDACIÓN RESULTADO

        // VALIDACIÓN HISTORIAL BECAS (solo 970, 971, 972)
        if (historialBecas != null && historialBecas > 0) {
            if (historialBecas != 970 && historialBecas != 971 && historialBecas != 972) {
                errores.add(new ValidationError(
                        rowIndex,
                        "Historial Becas",
                        "Solo permite valores: 970, 971, 972"
                ));
            }
        }
        // FIN VALIDACIÓN HISTORIAL BECAS

        // VALIDACIÓN ANALISTA REQUISITOS (solo A o N)
        if (analista != null && !analista.isBlank()) {
            if (!analista.equalsIgnoreCase("A") && !analista.equalsIgnoreCase("N")) {
                errores.add(new ValidationError(
                        rowIndex,
                        "Analista",
                        "Solo permite valores: A o N"
                ));
            }
        }
        // FIN VALIDACIÓN ANALISTA REQUISITOS

        // ================= VALIDACIÓN DE FECHAS =================

        // 🔥 FECHAS DE ESTUDIO
        Timestamp inicioEst = getCellTimestamp(cellInicioEst);
        Timestamp finEst = getCellTimestamp(cellFinEst);

        if (inicioEst != null && finEst != null) {
            if (!finEst.after(inicioEst)) {
                errores.add(new ValidationError(
                        rowIndex,
                        "Fechas estudios",
                        "La fecha fin debe ser mayor que la fecha inicio"
                ));
            }
        }

        // 🔥 FECHAS DE FINANCIAMIENTO
        Timestamp inicioFinTs = getCellTimestamp(cellInicioFin);
        Timestamp finFinTs = getCellTimestamp(cellFinFin);

        if (inicioFinTs != null && finFinTs != null) {
            if (!finFinTs.after(inicioFinTs)) {
                errores.add(new ValidationError(
                        rowIndex,
                        "Fechas financiamiento",
                        "La fecha fin debe ser mayor que la fecha inicio"
                ));
            }
        }

        return errores;
    }

    // ================= MÉTODOS AUXILIARES =================

    private static void validarTexto(List<ValidationError> errores, String valor, int fila, String campo) {
        if (valor == null || valor.isBlank()) {
            errores.add(new ValidationError(fila, campo, "Es obligatorio"));
        }
    }

    private static void validarNumero(List<ValidationError> errores, Long valor, int fila, String campo) {
        if (valor == null || valor == 0) {
            errores.add(new ValidationError(fila, campo, "Es obligatorio o tiene datos inválidos"));
        }
    }

    private static Timestamp getCellTimestamp(Cell cell) {
        try {
            if (cell == null) return null;

            // Fecha real de Excel
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return new Timestamp(cell.getDateCellValue().getTime());
            }

            // Fórmula
            if (cell.getCellType() == CellType.FORMULA) {
                FormulaEvaluator evaluator = cell.getSheet()
                        .getWorkbook()
                        .getCreationHelper()
                        .createFormulaEvaluator();

                CellValue cellValue = evaluator.evaluate(cell);

                if (cellValue.getCellType() == CellType.NUMERIC
                        && DateUtil.isCellDateFormatted(cell)) {
                    return new Timestamp(cell.getDateCellValue().getTime());
                }
            }

            // Texto
            String fecha = ScriptGeneratorUtil.getCellString(cell);
            return parseTimestamp(fecha);

        } catch (Exception e) {
            return null;
        }
    }    

    private static Timestamp parseTimestamp(String fecha) {
        try {
            if (fecha == null || fecha.isBlank()) {
                return null;
            }

            fecha = fecha.trim().replaceAll("\\s+", " ");
            fecha = fecha.replace("/", "-");

            if (fecha.length() == 10) {
                return Timestamp.valueOf(fecha + " 00:00:00");
            }

            return Timestamp.valueOf(fecha);

        } catch (Exception e) {
            return null;
        }
    }
}