package com.citasmedicas.app;

import com.citasmedicas.service.HorarioMedicoService;
import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * ================================================================
 *                  PRUEBA DE HORARIOS
 * ================================================================
 *
 * Comprueba la generación de bloques de atención a partir
 * del horario configurado para un médico.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public class PruebaHorarios {

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

            List<LocalTime> bloques =
                    servicio.generarBloques(
                            idMedico,
                            idEstablecimiento,
                            fecha
                    );

            System.out.println(
                    "=============================================="
            );

            System.out.println(
                    " HORARIOS GENERADOS"
            );

            System.out.println(
                    "=============================================="
            );

            System.out.println(
                    "Fecha: " + fecha
            );

            System.out.println(
                    "Día: "
                    + fecha.getDayOfWeek()
            );

            System.out.println(
                    "Médico ID: "
                    + idMedico
            );

            System.out.println(
                    "Establecimiento ID: "
                    + idEstablecimiento
            );

            System.out.println(
                    "----------------------------------------------"
            );

            for (LocalTime bloque : bloques) {

                System.out.println(
                        "Disponible: "
                        + bloque
                );
            }

            System.out.println(
                    "----------------------------------------------"
            );

            System.out.println(
                    "Total de bloques: "
                    + bloques.size()
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