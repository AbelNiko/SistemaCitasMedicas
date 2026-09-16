package com.citasmedicas.app;

import com.citasmedicas.service.HorarioMedicoService;
import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * ================================================================
 *                PRUEBA DE DISPONIBILIDAD REAL
 * ================================================================
 *
 * Comprueba que MediAppoint genere los bloques correspondientes
 * al horario de atención del médico y excluya aquellos que se
 * encuentran ocupados por citas activas.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public class PruebaDisponibilidad {

    public static void main(String[] args) {

        try {

            HorarioMedicoService servicio =
                    new HorarioMedicoService();

            int idMedico = 1;
            int idEstablecimiento = 1;

            LocalDate fecha =
                    LocalDate.of(
                            2026,
                            9,
                            21
                    );

            /*
             * Todos los bloques posibles según el horario
             * configurado para el médico.
             */
            List<LocalTime> bloquesTotales =
                    servicio.generarBloques(
                            idMedico,
                            idEstablecimiento,
                            fecha
                    );

            /*
             * Bloques realmente disponibles después de
             * consultar las citas existentes.
             */
            List<LocalTime> bloquesDisponibles =
                    servicio.obtenerBloquesDisponibles(
                            idMedico,
                            idEstablecimiento,
                            fecha
                    );

            System.out.println(
                    "=============================================="
            );

            System.out.println(
                    " DISPONIBILIDAD REAL - MEDIAPPOINT"
            );

            System.out.println(
                    "=============================================="
            );

            System.out.println(
                    "Fecha: " + fecha
            );

            System.out.println(
                    "Día: " + fecha.getDayOfWeek()
            );

            System.out.println(
                    "Médico ID: " + idMedico
            );

            System.out.println(
                    "Establecimiento ID: "
                    + idEstablecimiento
            );

            System.out.println(
                    "----------------------------------------------"
            );

            for (LocalTime bloque : bloquesTotales) {

                boolean disponible =
                        bloquesDisponibles.contains(
                                bloque
                        );

                System.out.println(
                        bloque
                                + " -> "
                                + (
                                    disponible
                                            ? "DISPONIBLE"
                                            : "OCUPADO"
                                )
                );
            }

            System.out.println(
                    "----------------------------------------------"
            );

            System.out.println(
                    "Bloques totales: "
                            + bloquesTotales.size()
            );

            System.out.println(
                    "Bloques disponibles: "
                            + bloquesDisponibles.size()
            );

        } catch (Exception e) {

            System.err.println(
                    "Error durante la prueba: "
                            + e.getMessage()
            );

            e.printStackTrace();

        } finally {

            AbandonedConnectionCleanupThread
                    .checkedShutdown();
        }
    }
}
