package com.citasmedicas.model;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * ================================================================
 *                  MODELO - HORARIO MÉDICO
 * ================================================================
 *
 * Representa el horario de atención configurado para un médico
 * dentro de un establecimiento de salud.
 *
 * Convención utilizada:
 * 1 = lunes
 * 2 = martes
 * 3 = miércoles
 * 4 = jueves
 * 5 = viernes
 * 6 = sábado
 * 7 = domingo
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public class HorarioMedico {

    private int idHorario;
    private int idMedico;
    private int idEstablecimiento;
    private int diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private int duracionCitaMinutos;
    private boolean estado;
    private LocalDateTime fechaCreacion;

    public HorarioMedico() {
    }

    public int getIdHorario() {
        return idHorario;
    }

    public void setIdHorario(int idHorario) {
        this.idHorario = idHorario;
    }

    public int getIdMedico() {
        return idMedico;
    }

    public void setIdMedico(int idMedico) {
        this.idMedico = idMedico;
    }

    public int getIdEstablecimiento() {
        return idEstablecimiento;
    }

    public void setIdEstablecimiento(
            int idEstablecimiento
    ) {
        this.idEstablecimiento = idEstablecimiento;
    }

    public int getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(int diaSemana) {
        this.diaSemana = diaSemana;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

    public int getDuracionCitaMinutos() {
        return duracionCitaMinutos;
    }

    public void setDuracionCitaMinutos(
            int duracionCitaMinutos
    ) {
        this.duracionCitaMinutos = duracionCitaMinutos;
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
}