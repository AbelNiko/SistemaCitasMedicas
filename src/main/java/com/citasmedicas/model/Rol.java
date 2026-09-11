package com.citasmedicas.model;

import java.time.LocalDateTime;

/**
 * ================================================================
 *                  MODELO - ROL DEL SISTEMA
 * ================================================================
 *
 * Representa un rol asignado a los usuarios del sistema.
 *
 * Ejemplos:
 * - PACIENTE
 * - MEDICO
 * - ADMINISTRADOR
 * - PERSONAL_ADMINISTRATIVO
 * - COORDINADOR_MEDICO
 *
 * Esta clase corresponde a la tabla "roles"
 * de la base de datos.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public class Rol {

    private Integer idRol;
    private String nombre;
    private String descripcion;
    private boolean estado;
    private LocalDateTime fechaCreacion;

    /**
     * Constructor vacío.
     */
    public Rol() {
    }

    /**
     * Constructor principal.
     *
     * @param idRol identificador del rol.
     * @param nombre nombre del rol.
     * @param descripcion descripción del rol.
     * @param estado estado activo o inactivo.
     * @param fechaCreacion fecha de creación.
     */
    public Rol(
            Integer idRol,
            String nombre,
            String descripcion,
            boolean estado,
            LocalDateTime fechaCreacion
    ) {
        this.idRol = idRol;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
    }

    public Integer getIdRol() {
        return idRol;
    }

    public void setIdRol(Integer idRol) {
        this.idRol = idRol;
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

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    @Override
    public String toString() {
        return nombre;
    }
}