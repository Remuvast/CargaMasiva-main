package com.becas.exceluploader.service;

import java.io.InputStream;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.transaction.annotation.Transactional;

@Service
public class RechazoMasivoService {

        private static class RegistroRechazo {

            String cedula;
            String numeroTramite;
            String codigoHistorial;
            String codigoResultado;
            String criterioTecnico;
            String comentario;

            public RegistroRechazo(
                    String cedula,
                    String numeroTramite,
                    String codigoHistorial,
                    String codigoResultado,
                    String criterioTecnico,
                    String comentario
            ) {
                this.cedula = cedula;
                this.numeroTramite = numeroTramite;
                this.codigoHistorial = codigoHistorial;
                this.codigoResultado = codigoResultado;
                this.criterioTecnico = criterioTecnico;
                this.comentario = comentario;
            }
        }

    private final JdbcTemplate jdbcTemplate;

    public RechazoMasivoService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public String procesarExcel(MultipartFile archivo) throws Exception {

        StringBuilder resultado = new StringBuilder();

        int procesados = 0;
        int errores = 0;

        java.util.List<RegistroRechazo> registrosValidos = new java.util.ArrayList<>();

        try (InputStream is = archivo.getInputStream();
            Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);

            // =========================================
            // FASE 1 - VALIDACIONES
            // =========================================

            for (int i = 6; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);

                if (row == null) {
                    continue;
                }

                boolean filaVacia = true;

                for (int c = 0; c <= 9; c++) {

                    Cell cell = row.getCell(c);

                    if (cell != null &&
                        cell.getCellType() != CellType.BLANK &&
                        !getValor(cell).isBlank()) {

                        filaVacia = false;
                        break;
                    }
                }

                if (filaVacia) {
                    continue;
                }

                String cedula = getValor(row.getCell(0));
                String numeroTramite = getValor(row.getCell(4));
                String codigoHistorial = getValor(row.getCell(5));
                String codigoResultado = getValor(row.getCell(7));
                String criterioTecnico = getValor(row.getCell(9));
                String comentario = getValor(row.getCell(12));

                boolean tieneErrores = false;

                // VALIDACIONES

                if (cedula.isBlank()) {
                    resultado.append("❌ Fila ")
                            .append(i + 1)
                            .append(" | Cédula: Es obligatoria\n");

                    tieneErrores = true;
                }

                if (numeroTramite.isBlank()) {
                    resultado.append("❌ Fila ")
                            .append(i + 1)
                            .append(" | Número trámite: Es obligatorio\n");

                    tieneErrores = true;
                }

                // VALIDAR HISTORIAL BECAS OBLIGATORIO
                if (codigoHistorial.isBlank()) {

                    resultado.append("❌ Fila ")
                            .append(i + 1)
                            .append(" | Historial Becas: Es obligatorio\n");

                    tieneErrores = true;
                }

                // VALIDAR HISTORIAL BECAS
                if (!codigoHistorial.isBlank()) {

                    try {

                        String valorNormalizado = codigoHistorial
                                .replace(",", ".")
                                .trim();

                        int historial = (int) Double.parseDouble(valorNormalizado);

                        // VALIDAR VALORES PERMITIDOS
                        if (historial != 970
                                && historial != 971
                                && historial != 972) {

                            resultado.append("❌ Fila ")
                                    .append(i + 1)
                                    .append(" | Historial Becas: Solo permite valores: 970, 971, 972\n");

                            tieneErrores = true;
                        }

                    } catch (Exception e) {

                        resultado.append("❌ Fila ")
                                .append(i + 1)
                                .append(" | Historial Becas: Debe ser numérico\n");

                        tieneErrores = true;
                    }
                }
                // FIN VALIDAR NUMERICO HISTORIAL

                // VALIDAR RESULTADO OBLIGATORIO
                if (codigoResultado.isBlank()) {

                    resultado.append("❌ Fila ")
                            .append(i + 1)
                            .append(" | Codigo Resultado: Es obligatorio\n");

                    tieneErrores = true;
                }

                // VALIDAR RESULTADO (solo A o N)
                if (!codigoResultado.isBlank()) {

                    if (!codigoResultado.equalsIgnoreCase("A")
                            && !codigoResultado.equalsIgnoreCase("N")) {

                        resultado.append("❌ Fila ")
                                .append(i + 1)
                                .append(" | Codigo Resultado: Solo permite valores: A o N\n");

                        tieneErrores = true;
                    }
                }

                // VALIDAR CRITERIO TECNICO
                if (criterioTecnico.isBlank()) {

                    resultado.append("❌ Fila ")
                            .append(i + 1)
                            .append(" | Criterio Técnico: Es obligatorio\n");

                    tieneErrores = true;
                }

                // VALIDAR COMENTARIO
                if (comentario.isBlank()) {

                    resultado.append("❌ Fila ")
                            .append(i + 1)
                            .append(" | Comentario: Es obligatorio\n");

                    tieneErrores = true;
                }

                // VALIDAR CEDULA
                if (!cedula.isBlank()) {

                    Integer existeCedula = jdbcTemplate.queryForObject(
                            """
                            SELECT COUNT(1)
                            FROM solicitantes
                            WHERE numero_identificacion = ?
                            """,
                            Integer.class,
                            cedula
                    );

                    if (existeCedula == 0) {

                        resultado.append("❌ Fila ")
                                .append(i + 1)
                                .append(" | Cédula: No existe en el sistema\n");

                        tieneErrores = true;
                    }
                }

                // VALIDAR TRAMITE
                if (!cedula.isBlank() && !numeroTramite.isBlank()) {

                    Integer existeTramite = jdbcTemplate.queryForObject(
                            """
                            SELECT COUNT(1)
                            FROM solicitudes so
                            JOIN solicitantes sl
                            ON sl.id = so.solicitantes_id
                            WHERE so.numero_tramite = ?
                            AND sl.numero_identificacion = ?
                            """,
                            Integer.class,
                            numeroTramite,
                            cedula
                    );

                    if (existeTramite == 0) {

                        resultado.append("❌ Fila ")
                                .append(i + 1)
                                .append(" | Número trámite: No existe o no pertenece a la cédula\n");

                        tieneErrores = true;
                    }
                }

                // SI HAY ERRORES
                if (tieneErrores) {
                    errores++;
                    continue;
                }

                registrosValidos.add(
                        new RegistroRechazo(
                                cedula,
                                numeroTramite,
                                codigoHistorial,
                                codigoResultado,
                                criterioTecnico,
                                comentario
                        )
                );

            }
        }

        resultado.append("\n📊 VALIDACIÓN:\n");

        if (errores > 0) {

            resultado.append("❌ Filas con errores encontrados: ")
                    .append(errores)
                    .append("\n");

            resultado.append("\n⛔ PROCESO CANCELADO: Existen errores. No se guardó nada. Favor corregir el archivo excel e intentarlo nuevamente");

            // =========================================
            // FASE 2 - EJECUCIÓN DE ACTUALIZACIONES
            // =========================================

            } else {

                resultado.append("✅ Validación exitosa sin errores\n");

                resultado.append("\n🚀 EJECUCIÓN:\n");

                // RECORRER REGISTROS VALIDADOS
                for (RegistroRechazo registro : registrosValidos) {

                    // UPDATE TABLA SOLICITUDES
                    ejecutarActualizacionSolicitud(
                            registro.cedula,
                            registro.numeroTramite,
                            registro.codigoHistorial,
                            registro.codigoResultado,
                            registro.criterioTecnico
                    );

                    // UPDATE TABLA TAREAS
                    ejecutarActualizacionTarea(
                            registro.cedula,
                            registro.numeroTramite,
                            registro.comentario
                    );

                    procesados++;
                }

                resultado.append("✔️ Procesados: ")
                        .append(procesados);
            }

        return resultado.toString();
    }

    private void ejecutarActualizacionSolicitud(
            String cedula,
            String numeroTramite,
            String codigoHistorial,
            String codigoResultado,
            String criterioTecnico
    ) {

        String sql = """
            UPDATE solicitudes so
            SET catalogos_historial_becas_id = ?,
                resultado = ?,
                criterio_tecnico = ?,
                presupuesto_beca = 0.00,
                estados_id = 24,
                estado = false,
                estados_devolucion_analista_id = 4
            FROM solicitantes sl
            WHERE sl.numero_identificacion = ?
              AND so.numero_tramite = ?
              AND sl.id = so.solicitantes_id
        """;

        jdbcTemplate.update(
                sql,
                (int) Double.parseDouble(
                        codigoHistorial
                                .replace(",", ".")
                                .trim()
                ),
                codigoResultado,
                criterioTecnico,
                cedula,
                numeroTramite
        );
    }

    private void ejecutarActualizacionTarea(
            String cedula,
            String numeroTramite,
            String comentario
    ) {

        String sql = """
            UPDATE tareas ta
            SET estado_tarea = 'T',
                fecha_atencion = NOW(),
                comentario = ?
            FROM solicitudes so
            JOIN solicitantes sl ON so.solicitantes_id = sl.id
            WHERE ta.solicitudes_id = so.id
              AND sl.numero_identificacion = ?
              AND so.numero_tramite = ?
        """;

        jdbcTemplate.update(
                sql,
                comentario,
                cedula,
                numeroTramite
        );
    }

    private String getValor(Cell cell) {
        
        if (cell == null) {
            return "";
        }

    DataFormatter formatter = new DataFormatter();

    return formatter.formatCellValue(cell).trim();
    }
}