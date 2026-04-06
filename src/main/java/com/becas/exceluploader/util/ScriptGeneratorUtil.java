package com.becas.exceluploader.util;

import org.apache.poi.ss.usermodel.*;
import java.time.LocalDate;

public class ScriptGeneratorUtil {

    public static String getCellString(Cell cell) {
        if (cell == null) return "";

        try {
            switch (cell.getCellType()) {

                case STRING:
                    return cell.getStringCellValue().trim();

                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        LocalDate fecha = cell.getLocalDateTimeCellValue().toLocalDate();
                        return fecha.toString(); // formato YYYY-MM-DD
                    } else {
                        double val = cell.getNumericCellValue();
                        return (val == (long) val)
                                ? String.valueOf((long) val)
                                : String.valueOf(val);
                    }

                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());

                case FORMULA:
                    try {
                        FormulaEvaluator evaluator = cell.getSheet()
                                .getWorkbook()
                                .getCreationHelper()
                                .createFormulaEvaluator();

                        CellValue cellValue = evaluator.evaluate(cell);

                        switch (cellValue.getCellType()) {
                            case STRING:
                                return cellValue.getStringValue().trim();
                            case NUMERIC:
                                return String.valueOf(cellValue.getNumberValue());
                            case BOOLEAN:
                                return String.valueOf(cellValue.getBooleanValue());
                            default:
                                return "";
                        }

                    } catch (Exception e) {
                        return cell.getCellFormula(); // fallback
                    }

                case BLANK:
                case ERROR:
                default:
                    return "";
            }
        } catch (Exception e) {
            return "";
        }
    }

    public static Long getCellLong(Cell cell) {
        if (cell == null) return 0L;

        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return (long) cell.getNumericCellValue();
            }

            String valor = getCellString(cell);
            if (valor.isBlank()) return 0L;

            return Long.parseLong(valor);

        } catch (Exception e) {
            return 0L;
        }
    }

    public static Double getCellDouble(Cell cell) {
        if (cell == null) return 0.0;

        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return cell.getNumericCellValue();
            }

            String valor = getCellString(cell);
            if (valor.isBlank()) return 0.0;

            return Double.parseDouble(valor);

        } catch (Exception e) {
            return 0.0;
        }
    }
}