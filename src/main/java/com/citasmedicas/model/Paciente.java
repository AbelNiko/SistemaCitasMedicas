package com.citasmedicas.model;

import java.time.LocalDate;

/**
 * ================================================================
 *                     MODELO - PACIENTE
 * ================================================================
 *
 * Representa la información específica de un paciente
 * dentro del Sistema de Gestión de Citas Médicas.
 *
 * La información general de acceso e identificación se almacena
 * en Usuario, mientras que esta clase contiene los datos propios
 * del perfil del paciente.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public class Paciente {

    private int idPaciente;
    private Usuario usuario;
    private LocalDate fechaNacimiento;
    private String direccion;
    private String sexo;
    private String contactoEmergencia;
    private String telefonoEmergencia;

    /**
     * Constructor vacío.
     */
    public Paciente() {
    }

    /**
     * Constructor principal.
     *
     * @param idPaciente identificador del paciente.
     * @param usuario usuario asociado.
     * @param fechaNacimiento fecha de nacimiento.
     * @param direccion dirección domiciliaria.
     * @param sexo sexo registrado.
     * @param contactoEmergencia contacto de emergencia.
     * @param telefonoEmergencia teléfono de emergencia.
     */
    public Paciente(
            int idPaciente,
            Usuario usuario,
            LocalDate fechaNacimiento,
            String direccion,
            String sexo,
            String contactoEmergencia,
            String telefonoEmergencia
    ) {

        this.idPaciente = idPaciente;
        this.usuario = usuario;
        this.fechaNacimiento = fechaNacimiento;
        this.direccion = direccion;
        this.sexo = sexo;
        this.contactoEmergencia = contactoEmergencia;
        this.telefonoEmergencia = telefonoEmergencia;
    }

    public int getIdPaciente() {
        return idPaciente;
    }

    public void setIdPaciente(int idPaciente) {
        this.idPaciente = idPaciente;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(
            LocalDate fechaNacimiento
    ) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public String getContactoEmergencia() {
        return contactoEmergencia;
    }

    public void setContactoEmergencia(
            String contactoEmergencia
    ) {
        this.contactoEmergencia =
                contactoEmergencia;
    }

    public String getTelefonoEmergencia() {
        return telefonoEmergencia;
    }

    public void setTelefonoEmergencia(
            String telefonoEmergencia
    ) {
        this.telefonoEmergencia =
                telefonoEmergencia;
    }
}