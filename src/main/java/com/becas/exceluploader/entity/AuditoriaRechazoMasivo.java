package com.becas.exceluploader.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria_rechazo_masivo")
public class AuditoriaRechazoMasivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String usuario;

    @Column(name = "nombre_archivo")
    private String nombreArchivo;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    private String estado;

    @Column(columnDefinition = "TEXT")
    private String mensaje;

    @Column(name = "total_registros_procesados")
    private Integer totalRegistrosProcesados;

    // GETTERS Y SETTERS

    public Long getId() {
        return id;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public Integer getTotalRegistrosProcesados() {
        return totalRegistrosProcesados;
    }

    public void setTotalRegistrosProcesados(Integer totalRegistrosProcesados) {
        this.totalRegistrosProcesados = totalRegistrosProcesados;
    }
}