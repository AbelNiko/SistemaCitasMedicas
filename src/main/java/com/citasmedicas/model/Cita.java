package com.citasmedicas.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * ================================================================
 *                         MODELO - CITA
 * ================================================================
 *
 * Representa una cita médica registrada en MediAppoint.
 *
 * Además de los identificadores relacionados, puede almacenar
 * información descriptiva obtenida mediante consultas JOIN para
 * mostrar las citas en la interfaz.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.1
 */
public class Cita {

    /*
     * ============================================================
     * IDENTIFICADORES
     * ============================================================
     */

    private int idCita;
    private int idPaciente;
    private int idMedico;
    private int idEspecialidad;
    private int idEstablecimiento;

    /*
     * ============================================================
     * FECHA Y HORARIO
     * ============================================================
     */

    private LocalDate fechaCita;
    private LocalTime horaInicio;
    private LocalTime horaFin;

    /*
     * ============================================================
     * INFORMACIÓN DE LA CITA
     * ============================================================
     */

    private String estado;
    private String motivoConsulta;
    private String observacion;
    private String motivoCancelacion;

    /*
     * ============================================================
     * AUDITORÍA
     * ============================================================
     */

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    /*
     * ============================================================
     * DATOS DESCRIPTIVOS
     * ============================================================
     *
     * Estos campos no representan nuevas columnas de la tabla
     * citas. Se utilizan para almacenar información obtenida
     * mediante consultas JOIN y facilitar su visualización
     * en la interfaz JavaFX.
     */

    private String nombreMedico;
    private String nombrePaciente;
    private String nombreEspecialidad;
    private String nombreEstablecimiento;

    /**
     * Constructor vacío.
     */
    public Cita() {
    }

    /*
     * ============================================================
     * ID CITA
     * ============================================================
     */

    public int getIdCita() {
        return idCita;
    }

    public void setIdCita(
            int idCita
    ) {
        this.idCita = idCita;
    }

    /*
     * ============================================================
     * ID PACIENTE
     * ============================================================
     */

    public int getIdPaciente() {
        return idPaciente;
    }

    public void setIdPaciente(
            int idPaciente
    ) {
        this.idPaciente = idPaciente;
    }

    /*
     * ============================================================
     * ID MÉDICO
     * ============================================================
     */

    public int getIdMedico() {
        return idMedico;
    }

    public void setIdMedico(
            int idMedico
    ) {
        this.idMedico = idMedico;
    }

    /*
     * ============================================================
     * ID ESPECIALIDAD
     * ============================================================
     */

    public int getIdEspecialidad() {
        return idEspecialidad;
    }

    public void setIdEspecialidad(
            int idEspecialidad
    ) {
        this.idEspecialidad = idEspecialidad;
    }

    /*
     * ============================================================
     * ID ESTABLECIMIENTO
     * ============================================================
     */

    public int getIdEstablecimiento() {
        return idEstablecimiento;
    }

    public void setIdEstablecimiento(
            int idEstablecimiento
    ) {
        this.idEstablecimiento = idEstablecimiento;
    }

    /*
     * ============================================================
     * FECHA
     * ============================================================
     */

    public LocalDate getFechaCita() {
        return fechaCita;
    }

    public void setFechaCita(
            LocalDate fechaCita
    ) {
        this.fechaCita = fechaCita;
    }

    /*
     * ============================================================
     * HORA DE INICIO
     * ============================================================
     */

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(
            LocalTime horaInicio
    ) {
        this.horaInicio = horaInicio;
    }

    /*
     * ============================================================
     * HORA DE FIN
     * ============================================================
     */

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(
            LocalTime horaFin
    ) {
        this.horaFin = horaFin;
    }

    /*
     * ============================================================
     * ESTADO
     * ============================================================
     */

    public String getEstado() {
        return estado;
    }

    public void setEstado(
            String estado
    ) {
        this.estado = estado;
    }

    /*
     * ============================================================
     * MOTIVO DE CONSULTA
     * ============================================================
     */

    public String getMotivoConsulta() {
        return motivoConsulta;
    }

    public void setMotivoConsulta(
            String motivoConsulta
    ) {
        this.motivoConsulta = motivoConsulta;
    }

    /*
     * ============================================================
     * OBSERVACIÓN
     * ============================================================
     */

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(
            String observacion
    ) {
        this.observacion = observacion;
    }

    /*
     * ============================================================
     * MOTIVO DE CANCELACIÓN
     * ============================================================
     */

    public String getMotivoCancelacion() {
        return motivoCancelacion;
    }

    public void setMotivoCancelacion(
            String motivoCancelacion
    ) {
        this.motivoCancelacion = motivoCancelacion;
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
        this.fechaCreacion = fechaCreacion;
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
        this.fechaActualizacion = fechaActualizacion;
    }

    /*
     * ============================================================
     * NOMBRE DEL MÉDICO
     * ============================================================
     */

    public String getNombreMedico() {
        return nombreMedico;
    }

    public void setNombreMedico(
            String nombreMedico
    ) {
        this.nombreMedico = nombreMedico;
    }

    /*
     * ============================================================
     * NOMBRE DEL PACIENTE
     * ============================================================
     */

    /**
     * Obtiene el nombre completo del paciente asociado a la cita.
     *
     * @return nombre completo del paciente.
     */
    public String getNombrePaciente() {
        return nombrePaciente;
    }

    /**
     * Establece el nombre completo del paciente asociado a la cita.
     *
     * @param nombrePaciente nombre completo del paciente.
     */
    public void setNombrePaciente(
            String nombrePaciente
    ) {
        this.nombrePaciente = nombrePaciente;
    }

    /*
     * ============================================================
     * NOMBRE DE LA ESPECIALIDAD
     * ============================================================
     */

    public String getNombreEspecialidad() {
        return nombreEspecialidad;
    }

    public void setNombreEspecialidad(
            String nombreEspecialidad
    ) {
        this.nombreEspecialidad = nombreEspecialidad;
    }

    /*
     * ============================================================
     * NOMBRE DEL ESTABLECIMIENTO
     * ============================================================
     */

    public String getNombreEstablecimiento() {
        return nombreEstablecimiento;
    }

    public void setNombreEstablecimiento(
            String nombreEstablecimiento
    ) {
        this.nombreEstablecimiento = nombreEstablecimiento;
    }
}