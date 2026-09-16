package com.citasmedicas.app;

import com.citasmedicas.model.Especialidad;
import com.citasmedicas.service.EspecialidadService;
import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;

import java.util.List;

/**
 * ================================================================
 *               PRUEBA DE ESPECIALIDADES
 * ================================================================
 *
 * Comprueba la consulta de especialidades activas desde MySQL.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public class PruebaEspecialidades {

    public static void main(String[] args) {

        try {

            EspecialidadService servicio =
                    new EspecialidadService();

            List<Especialidad> especialidades =
                    servicio.listarDisponibles();

            System.out.println(
                    "=============================================="
            );

            System.out.println(
                    " ESPECIALIDADES DISPONIBLES"
            );

            System.out.println(
                    "=============================================="
            );

            for (Especialidad especialidad
                    : especialidades) {

                System.out.println(
                        especialidad.getIdEspecialidad()
                        + " - "
                        + especialidad.getNombre()
                );
            }

            System.out.println(
                    "----------------------------------------------"
            );

            System.out.println(
                    "Total: "
                    + especialidades.size()
            );

        } catch (Exception e) {

            System.err.println(
                    "Error consultando especialidades: "
                    + e.getMessage()
            );

            e.printStackTrace();

        } finally {

            AbandonedConnectionCleanupThread
                    .checkedShutdown();
        }
    }
}