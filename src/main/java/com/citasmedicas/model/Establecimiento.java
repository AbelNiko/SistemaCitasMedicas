package com.citasmedicas.model;

import java.time.LocalDateTime;

/**
 * ================================================================
 *                 MODELO - ESTABLECIMIENTO
 * ================================================================
 *
 * Representa un establecimiento de salud registrado en
 * MediAppoint.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public class Establecimiento {

    private int idEstablecimiento;
    private String nombre;
    private String direccion;
    private String telefono;
    private String ciudad;
    private boolean estado;
    private LocalDateTime fechaCreacion;

    public Establecimiento() {
    }

    public Establecimiento(
            int idEstablecimiento,
            String nombre,
            String direccion,
            String telefono,
            String ciudad,
            boolean estado,
            LocalDateTime fechaCreacion
    ) {
        this.idEstablecimiento = idEstablecimiento;
        this.nombre = nombre;
        this.direccion = direccion;
        this.telefono = telefono;
        this.ciudad = ciudad;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
    }

    public int getIdEstablecimiento() {
        return idEstablecimiento;
    }

    public void setIdEstablecimiento(
            int idEstablecimiento
    ) {
        this.idEstablecimiento = idEstablecimiento;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
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
     * Permite mostrar directamente el nombre del establecimiento
     * dentro de controles JavaFX como ComboBox.
     */
    @Override
    public String toString() {
        return nombre;
    }
}