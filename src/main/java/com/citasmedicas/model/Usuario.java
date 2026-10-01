package com.citasmedicas.model;

import java.time.LocalDateTime;

/**
 * ================================================================
 *                       MODELO - USUARIO
 * ================================================================
 *
 * Representa a un usuario registrado dentro del Sistema
 * de Gestión y Agendamiento de Citas Médicas.
 *
 * Contiene los datos generales de identificación, autenticación,
 * contacto y seguridad de la cuenta.
 *
 * La contraseña y la respuesta de seguridad nunca deben
 * almacenarse en texto plano.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.1
 */
public class Usuario {

    /*
     * ============================================================
     * IDENTIFICACIÓN
     * ============================================================
     */

    private Integer idUsuario;

    private Rol rol;

    private String nombres;

    private String apellidos;

    private String cedula;

    private String correo;

    /*
     * ============================================================
     * AUTENTICACIÓN
     * ============================================================
     */

    private String passwordHash;

    /*
     * ============================================================
     * CONTACTO
     * ============================================================
     */

    private String telefono;

    /*
     * ============================================================
     * SEGURIDAD Y RECUPERACIÓN
     * ============================================================
     */

    /**
     * Código de la pregunta de seguridad.
     *
     * Ejemplo:
     * COMIDA_FAVORITA
     */
    private String preguntaSeguridad;

    /**
     * Hash BCrypt de la respuesta de seguridad.
     *
     * La respuesta original nunca se almacena.
     */
    private String respuestaSeguridadHash;

    /**
     * Cantidad de intentos fallidos consecutivos
     * durante una recuperación de contraseña.
     */
    private int intentosRecuperacion;

    /**
     * Fecha y hora hasta la cual la recuperación
     * permanece temporalmente bloqueada.
     */
    private LocalDateTime bloqueadoRecuperacionHasta;

    /*
     * ============================================================
     * ESTADO Y AUDITORÍA
     * ============================================================
     */

    private boolean estado;

    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaActualizacion;

    /**
     * Constructor vacío.
     */
    public Usuario() {
    }

    /**
     * Constructor principal compatible con la estructura
     * original del proyecto.
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

        this.preguntaSeguridad = null;
        this.respuestaSeguridadHash = null;
        this.intentosRecuperacion = 0;
        this.bloqueadoRecuperacionHasta = null;

        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
    }

    /*
     * ============================================================
     * ID USUARIO
     * ============================================================
     */

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(
            Integer idUsuario
    ) {

        this.idUsuario = idUsuario;
    }

    /*
     * ============================================================
     * ROL
     * ============================================================
     */

    public Rol getRol() {
        return rol;
    }

    public void setRol(
            Rol rol
    ) {

        this.rol = rol;
    }

    /*
     * ============================================================
     * NOMBRES
     * ============================================================
     */

    public String getNombres() {
        return nombres;
    }

    public void setNombres(
            String nombres
    ) {

        this.nombres = nombres;
    }

    /*
     * ============================================================
     * APELLIDOS
     * ============================================================
     */

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(
            String apellidos
    ) {

        this.apellidos = apellidos;
    }

    /*
     * ============================================================
     * CÉDULA
     * ============================================================
     */

    public String getCedula() {
        return cedula;
    }

    public void setCedula(
            String cedula
    ) {

        this.cedula = cedula;
    }

    /*
     * ============================================================
     * CORREO
     * ============================================================
     */

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(
            String correo
    ) {

        this.correo = correo;
    }

    /*
     * ============================================================
     * CONTRASEÑA
     * ============================================================
     */

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(
            String passwordHash
    ) {

        this.passwordHash = passwordHash;
    }

    /*
     * ============================================================
     * TELÉFONO
     * ============================================================
     */

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(
            String telefono
    ) {

        this.telefono = telefono;
    }

    /*
     * ============================================================
     * PREGUNTA DE SEGURIDAD
     * ============================================================
     */

    public String getPreguntaSeguridad() {
        return preguntaSeguridad;
    }

    public void setPreguntaSeguridad(
            String preguntaSeguridad
    ) {

        this.preguntaSeguridad =
                preguntaSeguridad;
    }

    /*
     * ============================================================
     * RESPUESTA DE SEGURIDAD
     * ============================================================
     */

    public String getRespuestaSeguridadHash() {
        return respuestaSeguridadHash;
    }

    public void setRespuestaSeguridadHash(
            String respuestaSeguridadHash
    ) {

        this.respuestaSeguridadHash =
                respuestaSeguridadHash;
    }

    /*
     * ============================================================
     * INTENTOS DE RECUPERACIÓN
     * ============================================================
     */

    public int getIntentosRecuperacion() {
        return intentosRecuperacion;
    }

    public void setIntentosRecuperacion(
            int intentosRecuperacion
    ) {

        this.intentosRecuperacion =
                intentosRecuperacion;
    }

    /*
     * ============================================================
     * BLOQUEO DE RECUPERACIÓN
     * ============================================================
     */

    public LocalDateTime getBloqueadoRecuperacionHasta() {
        return bloqueadoRecuperacionHasta;
    }

    public void setBloqueadoRecuperacionHasta(
            LocalDateTime bloqueadoRecuperacionHasta
    ) {

        this.bloqueadoRecuperacionHasta =
                bloqueadoRecuperacionHasta;
    }

    /*
     * ============================================================
     * ESTADO
     * ============================================================
     */

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(
            boolean estado
    ) {

        this.estado = estado;
    }

    /*
     * ============================================================
     * FECHA DE CREACIÓN
     * ============================================================
     */

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(
            LocalDateTime fechaCreacion
    ) {

        this.fechaCreacion =
                fechaCreacion;
    }

    /*
     * ============================================================
     * FECHA DE ACTUALIZACIÓN
     * ============================================================
     */

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(
            LocalDateTime fechaActualizacion
    ) {

        this.fechaActualizacion =
                fechaActualizacion;
    }

    /*
     * ============================================================
     * MÉTODOS AUXILIARES
     * ============================================================
     */

    /**
     * Devuelve nombres y apellidos del usuario.
     */
    public String getNombreCompleto() {

        String nombresSeguros =
                nombres == null
                        ? ""
                        : nombres.trim();

        String apellidosSeguros =
                apellidos == null
                        ? ""
                        : apellidos.trim();

        return (
                nombresSeguros
                        + " "
                        + apellidosSeguros
        ).trim();
    }

    /**
     * Determina si el usuario ya configuró correctamente
     * su pregunta de seguridad.
     */
    public boolean tienePreguntaSeguridadConfigurada() {

        return preguntaSeguridad != null
                && !preguntaSeguridad.isBlank()
                && respuestaSeguridadHash != null
                && !respuestaSeguridadHash.isBlank();
    }

    /**
     * Determina si existe actualmente un bloqueo temporal
     * para recuperar la contraseña.
     */
    public boolean estaRecuperacionBloqueada() {

        return bloqueadoRecuperacionHasta != null
                && bloqueadoRecuperacionHasta.isAfter(
                        LocalDateTime.now()
                );
    }

    @Override
    public String toString() {

        return getNombreCompleto();
    }
}