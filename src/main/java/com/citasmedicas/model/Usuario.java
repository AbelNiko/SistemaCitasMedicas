package com.citasmedicas.model;

import java.time.LocalDateTime;

/**
 * ================================================================
 *                    MODELO - USUARIO
 * ================================================================
 *
 * Representa a un usuario registrado dentro del sistema
 * de gestión y agendamiento de citas médicas.
 *
 * Esta clase corresponde a la tabla "usuarios"
 * de la base de datos.
 *
 * Cada usuario posee un rol que determina los permisos
 * y funcionalidades disponibles dentro de la aplicación.
 *
 * La contraseña nunca se almacena como texto plano.
 * Únicamente se maneja mediante su hash.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public class Usuario {

    private Integer idUsuario;
    private Rol rol;

    private String nombres;
    private String apellidos;
    private String cedula;
    private String correo;
    private String passwordHash;
    private String telefono;

    private boolean estado;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    /**
     * Constructor vacío.
     */
    public Usuario() {
    }

    /**
     * Constructor principal.
     *
     * @param idUsuario identificador del usuario.
     * @param rol rol asignado.
     * @param nombres nombres del usuario.
     * @param apellidos apellidos del usuario.
     * @param cedula número de cédula.
     * @param correo correo electrónico.
     * @param passwordHash contraseña almacenada mediante hash.
     * @param telefono teléfono del usuario.
     * @param estado estado activo o inactivo.
     * @param fechaCreacion fecha de creación.
     * @param fechaActualizacion fecha de última actualización.
     */
    public Usuario(
            Integer idUsuario,
            Rol rol,
            String nombres,
            String apellidos,
            String cedula,
            String correo,
            String passwordHash,
            String telefono,
            boolean estado,
            LocalDateTime fechaCreacion,
            LocalDateTime fechaActualizacion
    ) {
        this.idUsuario = idUsuario;
        this.rol = rol;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.cedula = cedula;
        this.correo = correo;
        this.passwordHash = passwordHash;
        this.telefono = telefono;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
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

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(
            LocalDateTime fechaActualizacion
    ) {
        this.fechaActualizacion = fechaActualizacion;
    }

    /**
     * Devuelve el nombre completo del usuario.
     *
     * @return nombres y apellidos del usuario.
     */
    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    @Override
    public String toString() {
        return getNombreCompleto();
    }
}