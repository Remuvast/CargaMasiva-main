package com.becas.exceluploader.service;

public class ResultadoCarga {

    private String mensaje;
    private int totalProcesados;

    public ResultadoCarga(String mensaje, int totalProcesados) {
        this.mensaje = mensaje;
        this.totalProcesados = totalProcesados;
    }

    public String getMensaje() {
        return mensaje;
    }

    public int getTotalProcesados() {
        return totalProcesados;
    }
}