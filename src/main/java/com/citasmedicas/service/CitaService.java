package com.citasmedicas.service;

import com.citasmedicas.dao.CitaDAO;
import com.citasmedicas.model.Cita;
import com.citasmedicas.util.ConexionBD;

import java.util.List;
import java.util.Optional;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * ================================================================
 *                       SERVICE - CITA
 * ================================================================
 *
 * Gestiona las reglas de negocio relacionadas con las
 * citas médicas.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public class CitaService {

    private final CitaDAO citaDAO;

    public CitaService() {
        this.citaDAO = new CitaDAO();
    }

    /**
     * Determina si un bloque se encuentra disponible.
     */
    public boolean estaDisponible(
            int idMedico,
            int idEstablecimiento,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin
    ) throws SQLException {

        validarHorario(
                idMedico,
                idEstablecimiento,
                fecha,
                horaInicio,
                horaFin
        );

        return !citaDAO.existeSolapamiento(
                idMedico,
                idEstablecimiento,
                fecha,
                horaInicio,
                horaFin
        );
    }

    /**
     * Registra una nueva cita médica.
     *
     * Antes de insertar la cita se vuelve a comprobar
     * que el horario continúe disponible.
     */
    public Cita agendarCita(
            int idPaciente,
            int idMedico,
            int idEspecialidad,
            int idEstablecimiento,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin,
            String motivoConsulta
    ) throws SQLException {

        if (idPaciente <= 0) {
            throw new IllegalArgumentException(
                    "El paciente no es válido."
            );
        }

        if (idEspecialidad <= 0) {
            throw new IllegalArgumentException(
                    "La especialidad seleccionada no es válida."
            );
        }

        validarHorario(
                idMedico,
                idEstablecimiento,
                fecha,
                horaInicio,
                horaFin
        );

        String motivo =
                motivoConsulta == null
                        ? ""
                        : motivoConsulta.trim();

        if (motivo.isEmpty()) {
            throw new IllegalArgumentException(
                    "Debe ingresar el motivo de la consulta."
            );
        }

        if (motivo.length() > 255) {
            throw new IllegalArgumentException(
                    "El motivo de consulta no puede superar los 255 caracteres."
            );
        }

        Cita cita = new Cita();

        cita.setIdPaciente(idPaciente);
        cita.setIdMedico(idMedico);
        cita.setIdEspecialidad(idEspecialidad);
        cita.setIdEstablecimiento(idEstablecimiento);

        cita.setFechaCita(fecha);
        cita.setHoraInicio(horaInicio);
        cita.setHoraFin(horaFin);

        cita.setEstado("PROGRAMADA");
        cita.setMotivoConsulta(motivo);

        try (Connection conexion =
                     ConexionBD.obtenerConexion()) {

            boolean autoCommitOriginal =
                    conexion.getAutoCommit();

            try {

                conexion.setAutoCommit(false);

                boolean ocupado =
                        citaDAO.existeSolapamiento(
                                conexion,
                                idMedico,
                                idEstablecimiento,
                                fecha,
                                horaInicio,
                                horaFin
                        );

                if (ocupado) {

                    conexion.rollback();

                    throw new IllegalArgumentException(
                            "El horario seleccionado ya no se encuentra disponible."
                    );
                }

                citaDAO.insertar(
                        conexion,
                        cita
                );

                conexion.commit();

                return cita;

            } catch (SQLException
                     | IllegalArgumentException e) {

                try {
                    conexion.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }

                throw e;

            } finally {

                try {
                    conexion.setAutoCommit(
                            autoCommitOriginal
                    );
                } catch (SQLException e) {
                    System.err.println(
                            "No fue posible restaurar AutoCommit: "
                                    + e.getMessage()
                    );
                }
            }
        }
    }

    /**
     * Valida los datos básicos de un intervalo.
     */
    private void validarHorario(
            int idMedico,
            int idEstablecimiento,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin
    ) {

        if (idMedico <= 0) {
            throw new IllegalArgumentException(
                    "El médico seleccionado no es válido."
            );
        }

        if (idEstablecimiento <= 0) {
            throw new IllegalArgumentException(
                    "El establecimiento seleccionado no es válido."
            );
        }

        if (fecha == null) {
            throw new IllegalArgumentException(
                    "Debe seleccionar una fecha."
            );
        }

        if (fecha.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "No se puede agendar una cita en una fecha anterior."
            );
        }

        if (horaInicio == null || horaFin == null) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un horario."
            );
        }

        if (!horaInicio.isBefore(horaFin)) {
            throw new IllegalArgumentException(
                    "El horario seleccionado no es válido."
            );
        }
    }
    /**
 * Obtiene todas las citas asociadas a un paciente.
 */
public List<Cita> listarPorPaciente(
        int idPaciente
) throws SQLException {

    if (idPaciente <= 0) {

        throw new IllegalArgumentException(
                "El paciente no es válido."
        );
    }

    return citaDAO.listarPorPaciente(
            idPaciente
    );
}

/**
 * Cuenta las citas activas y futuras del paciente.
 */
public long contarCitasProgramadas(
        int idPaciente
) throws SQLException {

    LocalDate hoy =
            LocalDate.now();

    return listarPorPaciente(idPaciente)
            .stream()
            .filter(
                    cita ->
                            (
                                "PROGRAMADA".equalsIgnoreCase(
                                        cita.getEstado()
                                )
                                || "CONFIRMADA".equalsIgnoreCase(
                                        cita.getEstado()
                                )
                            )
                            && !cita
                                    .getFechaCita()
                                    .isBefore(hoy)
            )
            .count();
}

/**
 * Obtiene la próxima cita activa del paciente.
 */
public Optional<Cita> obtenerProximaCita(
        int idPaciente
) throws SQLException {

    LocalDate hoy =
            LocalDate.now();

    LocalTime ahora =
            LocalTime.now();

    return listarPorPaciente(idPaciente)
            .stream()
            .filter(
                    cita ->
                            "PROGRAMADA".equalsIgnoreCase(
                                    cita.getEstado()
                            )
                            || "CONFIRMADA".equalsIgnoreCase(
                                    cita.getEstado()
                            )
            )
            .filter(cita -> {

                if (
                        cita.getFechaCita()
                                .isAfter(hoy)
                ) {
                    return true;
                }

                if (
                        cita.getFechaCita()
                                .isEqual(hoy)
                ) {

                    return !cita
                            .getHoraInicio()
                            .isBefore(ahora);
                }

                return false;
            })
            .findFirst();
}
/**
 * Cancela una cita médica del paciente.
 */
public void cancelarCita(
        int idCita,
        int idPaciente,
        String motivoCancelacion
) throws SQLException {

    if (idCita <= 0) {

        throw new IllegalArgumentException(
                "La cita seleccionada no es válida."
        );
    }

    if (idPaciente <= 0) {

        throw new IllegalArgumentException(
                "El paciente no es válido."
        );
    }

    String motivo =
            motivoCancelacion == null
                    ? ""
                    : motivoCancelacion.trim();

    if (motivo.isEmpty()) {

        throw new IllegalArgumentException(
                "Debe ingresar el motivo de cancelación."
        );
    }

    if (motivo.length() > 255) {

        throw new IllegalArgumentException(
                "El motivo de cancelación no puede superar "
                + "los 255 caracteres."
        );
    }

    boolean cancelada =
            citaDAO.cancelarCita(
                    idCita,
                    idPaciente,
                    motivo
            );

    if (!cancelada) {

        throw new IllegalArgumentException(
                "La cita no pudo ser cancelada. "
                + "Es posible que su estado haya cambiado."
        );
    }
}
}