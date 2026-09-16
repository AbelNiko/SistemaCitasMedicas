package com.citasmedicas.model;

import java.time.LocalDateTime;

/**
 * ================================================================
 *                   MODELO - ESPECIALIDAD
 * ================================================================
 *
 * Representa una especialidad médica registrada en el sistema.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public class Especialidad {

    private int idEspecialidad;
    private String nombre;
    private String descripcion;
    private boolean estado;
    private LocalDateTime fechaCreacion;

    public Especialidad() {
    }

    public Especialidad(
            int idEspecialidad,
            String nombre,
            String descripcion,
            boolean estado,
            LocalDateTime fechaCreacion
    ) {
        this.idEspecialidad = idEspecialidad;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
    }

    public int getIdEspecialidad() {
        return idEspecialidad;
    }

    public void setIdEspecialidad(int idEspecialidad) {
        this.idEspecialidad = idEspecialidad;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
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

    /**
     * Permite mostrar directamente el nombre de la especialidad
     * dentro de controles JavaFX como ComboBox.
     */
    @Override
    public String toString() {
        return nombre;
    }
}