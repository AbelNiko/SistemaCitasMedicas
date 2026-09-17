package com.citasmedicas.service;

import com.citasmedicas.dao.HorarioMedicoDAO;
import com.citasmedicas.model.HorarioMedico;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ================================================================
 *                  SERVICE - HORARIO MÉDICO
 * ================================================================
 *
 * Gestiona las reglas de negocio relacionadas con los horarios
 * médicos, la generación de bloques de atención y la consulta
 * de disponibilidad real para una fecha determinada.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public class HorarioMedicoService {

    private final HorarioMedicoDAO horarioMedicoDAO;
    private final CitaService citaService;

    /**
     * Constructor del servicio.
     */
    public HorarioMedicoService() {

        this.horarioMedicoDAO =
                new HorarioMedicoDAO();

        this.citaService =
                new CitaService();
    }

    /**
     * Obtiene los horarios configurados para un médico
     * en una fecha determinada.
     *
     * La fecha seleccionada se convierte automáticamente
     * al número correspondiente al día de la semana:
     *
     * 1 = lunes
     * 2 = martes
     * 3 = miércoles
     * 4 = jueves
     * 5 = viernes
     * 6 = sábado
     * 7 = domingo
     *
     * @param idMedico identificador del médico.
     * @param idEstablecimiento identificador del establecimiento.
     * @param fecha fecha seleccionada.
     * @return lista de horarios configurados.
     * @throws SQLException si ocurre un error de acceso a datos.
     */
    public List<HorarioMedico> obtenerHorarios(
            int idMedico,
            int idEstablecimiento,
            LocalDate fecha
    ) throws SQLException {

        

        validarParametros(
                idMedico,
                idEstablecimiento,
                fecha
        );

        int diaSemana =
                fecha.getDayOfWeek().getValue();

        return horarioMedicoDAO
                .listarPorMedicoEstablecimientoYDia(
                        idMedico,
                        idEstablecimiento,
                        diaSemana
                );
    }

    /**
     * Genera todos los bloques posibles de atención según
     * los horarios configurados para el médico.
     *
     * Ejemplo:
     *
     * Horario:
     * 08:00 - 12:00
     *
     * Duración:
     * 30 minutos
     *
     * Resultado:
     * 08:00
     * 08:30
     * 09:00
     * 09:30
     * 10:00
     * 10:30
     * 11:00
     * 11:30
     *
     * Este método genera bloques posibles, pero todavía no
     * comprueba si alguno está ocupado por una cita.
     *
     * @param idMedico identificador del médico.
     * @param idEstablecimiento identificador del establecimiento.
     * @param fecha fecha seleccionada.
     * @return lista de bloques posibles.
     * @throws SQLException si ocurre un error de acceso a datos.
     */
    public List<LocalTime> generarBloques(
            int idMedico,
            int idEstablecimiento,
            LocalDate fecha
    ) throws SQLException {

        List<HorarioMedico> horarios =
                obtenerHorarios(
                        idMedico,
                        idEstablecimiento,
                        fecha
                );

        List<LocalTime> bloques =
                new ArrayList<>();

        for (HorarioMedico horario : horarios) {

            LocalTime horaActual =
                    horario.getHoraInicio();

            int duracion =
                    horario.getDuracionCitaMinutos();

            while (
                    !horaActual
                            .plusMinutes(duracion)
                            .isAfter(
                                    horario.getHoraFin()
                            )
            ) {

                bloques.add(horaActual);

                horaActual =
                        horaActual.plusMinutes(
                                duracion
                        );
            }
        }

        return bloques;
    }

    /**
     * Obtiene únicamente los bloques realmente disponibles
     * para el paciente.
     *
     * Primero consulta el horario laboral configurado para
     * el médico y genera los bloques correspondientes.
     *
     * Posteriormente comprueba cada bloque contra las citas
     * existentes en la base de datos.
     *
     * Las citas con estado PROGRAMADA o CONFIRMADA bloquean
     * el horario. Las citas canceladas no lo bloquean.
     *
     * @param idMedico identificador del médico.
     * @param idEstablecimiento identificador del establecimiento.
     * @param fecha fecha seleccionada.
     * @return lista de horarios realmente disponibles.
     * @throws SQLException si ocurre un error de acceso a datos.
     */
    public List<LocalTime> obtenerBloquesDisponibles(
            int idMedico,
            int idEstablecimiento,
            LocalDate fecha
    ) throws SQLException {

        List<HorarioMedico> horarios =
                obtenerHorarios(
                        idMedico,
                        idEstablecimiento,
                        fecha
                );

        List<LocalTime> disponibles =
                new ArrayList<>();

        for (HorarioMedico horario : horarios) {

            LocalTime horaActual =
                    horario.getHoraInicio();

            int duracion =
                    horario.getDuracionCitaMinutos();

            while (
                    !horaActual
                            .plusMinutes(duracion)
                            .isAfter(
                                    horario.getHoraFin()
                            )
            ) {

                LocalTime horaFinBloque =
                        horaActual.plusMinutes(
                                duracion
                        );

                boolean disponible =
                        citaService.estaDisponible(
                                idMedico,
                                idEstablecimiento,
                                fecha,
                                horaActual,
                                horaFinBloque
                        );

                if (disponible) {
                    disponibles.add(
                            horaActual
                    );
                }

                horaActual =
                        horaFinBloque;
            }
        }

        return disponibles;
    }

    /**
 * Obtiene los bloques disponibles para reprogramar una cita.
 *
 * Funciona de forma similar a obtenerBloquesDisponibles(),
 * pero excluye la cita que actualmente está siendo modificada.
 *
 * De esta manera, la cita no se detecta a sí misma como
 * un horario ocupado.
 */
public List<LocalTime> obtenerBloquesDisponiblesParaReprogramacion(
        int idCitaExcluir,
        int idMedico,
        int idEstablecimiento,
        LocalDate fecha
) throws SQLException {

    validarParametros(
            idMedico,
            idEstablecimiento,
            fecha
    );

    if (idCitaExcluir <= 0) {

        throw new IllegalArgumentException(
                "La cita seleccionada no es válida."
        );
    }

    List<HorarioMedico> horarios =
            obtenerHorarios(
                    idMedico,
                    idEstablecimiento,
                    fecha
            );

    List<LocalTime> disponibles =
            new ArrayList<>();

    for (HorarioMedico horario : horarios) {

        LocalTime horaActual =
                horario.getHoraInicio();

        int duracion =
                horario.getDuracionCitaMinutos();

        while (
                !horaActual
                        .plusMinutes(duracion)
                        .isAfter(
                                horario.getHoraFin()
                        )
        ) {

            LocalTime horaFinBloque =
                    horaActual.plusMinutes(
                            duracion
                    );

            boolean disponible =
                    citaService
                            .estaDisponibleParaReprogramacion(
                                    idCitaExcluir,
                                    idMedico,
                                    idEstablecimiento,
                                    fecha,
                                    horaActual,
                                    horaFinBloque
                            );

            if (disponible) {

                disponibles.add(
                        horaActual
                );
            }

            horaActual =
                    horaFinBloque;
        }
    }

    return disponibles;
}

    /**
     * Valida los parámetros necesarios para consultar
     * los horarios médicos.
     *
     * @param idMedico identificador del médico.
     * @param idEstablecimiento identificador del establecimiento.
     * @param fecha fecha seleccionada.
     */
    private void validarParametros(
            int idMedico,
            int idEstablecimiento,
            LocalDate fecha
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
    }
}