package com.becas.exceluploader.validation;

public class ValidationError {

    private int fila;
    private String campo;
    private String mensaje;

    public ValidationError(int fila, String campo, String mensaje) {
        this.fila = fila;
        this.campo = campo;
        this.mensaje = mensaje;
    }

    @Override
    public String toString() {
        return "Fila " + fila + " | " + campo + ": " + mensaje;
    }
}