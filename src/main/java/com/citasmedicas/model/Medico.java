package com.citasmedicas.model;

import java.time.LocalDateTime;

/**
 * ================================================================
 *                       MODELO - MÉDICO
 * ================================================================
 *
 * Representa el perfil profesional de un médico registrado
 * dentro de MediAppoint.
 *
 * Los datos personales y de autenticación pertenecen al objeto
 * Usuario asociado.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public class Medico {

    private int idMedico;
    private Usuario usuario;
    private String cedulaProfesional;
    private String observacion;
    private boolean estado;
    private LocalDateTime fechaCreacion;

    public Medico() {
    }

    public Medico(
            int idMedico,
            Usuario usuario,
            String cedulaProfesional,
            String observacion,
            boolean estado,
            LocalDateTime fechaCreacion
    ) {
        this.idMedico = idMedico;
        this.usuario = usuario;
        this.cedulaProfesional = cedulaProfesional;
        this.observacion = observacion;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
    }

    public int getIdMedico() {
        return idMedico;
    }

    public void setIdMedico(int idMedico) {
        this.idMedico = idMedico;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getCedulaProfesional() {
        return cedulaProfesional;
    }

    public void setCedulaProfesional(
            String cedulaProfesional
    ) {
        this.cedulaProfesional = cedulaProfesional;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(
            LocalDateTime fechaCreacion
    ) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getNombreCompleto() {

        if (usuario == null) {
            return "";
        }

        return usuario.getNombreCompleto();
    }

    /**
     * Permite mostrar directamente el nombre del médico
     * en controles JavaFX como ComboBox.
     */
    @Override
    public String toString() {
        return getNombreCompleto();
    }
}