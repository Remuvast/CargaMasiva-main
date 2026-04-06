package com.becas.exceluploader.validation;

import com.becas.exceluploader.util.ScriptGeneratorUtil;
import org.apache.poi.ss.usermodel.Row;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

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

        String fechaInicioEstudios = ScriptGeneratorUtil.getCellString(fila.getCell(19));
        String fechaFinEstudios = ScriptGeneratorUtil.getCellString(fila.getCell(20));
        String duracionEstudios = ScriptGeneratorUtil.getCellString(fila.getCell(21));

        String fechaInicioFin = ScriptGeneratorUtil.getCellString(fila.getCell(22));
        String fechaFinFin = ScriptGeneratorUtil.getCellString(fila.getCell(23));
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

        validarNumero(errores, nivelEstudio, rowIndex, "Nivel de estudio");
        validarNumero(errores, campoDetallado, rowIndex, "Campo detallado");
        validarNumero(errores, carrera, rowIndex, "Carrera");
        validarNumero(errores, universidad, rowIndex, "Universidad");
        validarNumero(errores, pais, rowIndex, "País");
        validarNumero(errores, titulo, rowIndex, "Título");
        validarNumero(errores, idioma, rowIndex, "Idioma");

        validarTexto(errores, fechaInicioEstudios, rowIndex, "Fecha inicio estudios");
        validarTexto(errores, fechaFinEstudios, rowIndex, "Fecha fin estudios");
        validarTexto(errores, duracionEstudios, rowIndex, "Duración estudios");

        validarTexto(errores, fechaInicioFin, rowIndex, "Fecha inicio financiamiento");
        validarTexto(errores, fechaFinFin, rowIndex, "Fecha fin financiamiento");
        validarTexto(errores, duracionFin, rowIndex, "Duración financiamiento");

        validarTexto(errores, presupuesto, rowIndex, "Presupuesto");
        validarTexto(errores, rubro, rowIndex, "Rubro");

        // ================= VALIDACIÓN DE FECHAS =================

        Timestamp inicioEst = parseTimestamp(fechaInicioEstudios);
        Timestamp finEst = parseTimestamp(fechaFinEstudios);

        if (inicioEst != null && finEst != null) {
            if (!finEst.after(inicioEst)) {
                errores.add(new ValidationError(
                        rowIndex,
                        "Fechas estudios",
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
            errores.add(new ValidationError(fila, campo, "Es obligatorio o inválido"));
        }
    }

    private static Timestamp parseTimestamp(String fecha) {
        try {
            if (fecha == null || fecha.isBlank()) return null;
            fecha = fecha.trim().replaceAll("\\s+", " ");
            if (fecha.length() == 10) return Timestamp.valueOf(fecha + " 00:00:00");
            return Timestamp.valueOf(fecha);
        } catch (Exception e) {
            return null;
        }
    }
}