package com.citasmedicas.app;

import com.citasmedicas.model.Establecimiento;
import com.citasmedicas.service.EstablecimientoService;
import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;

import java.util.List;

/**
 * ================================================================
 *              PRUEBA DE ESTABLECIMIENTOS
 * ================================================================
 *
 * Comprueba la consulta de establecimientos activos
 * almacenados en MySQL.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public class PruebaEstablecimientos {

    public static void main(String[] args) {

        try {

            EstablecimientoService servicio =
                    new EstablecimientoService();

            List<Establecimiento> establecimientos =
                    servicio.listarDisponibles();

            System.out.println(
                    "=============================================="
            );

            System.out.println(
                    " ESTABLECIMIENTOS DISPONIBLES"
            );

            System.out.println(
                    "=============================================="
            );

            for (Establecimiento establecimiento
                    : establecimientos) {

                System.out.println(
                        establecimiento.getIdEstablecimiento()
                        + " - "
                        + establecimiento.getNombre()
                        + " | "
                        + establecimiento.getCiudad()
                );
            }

            System.out.println(
                    "----------------------------------------------"
            );

            System.out.println(
                    "Total: "
                    + establecimientos.size()
            );

        } catch (Exception e) {

            System.err.println(
                    "Error consultando establecimientos: "
                    + e.getMessage()
            );

            e.printStackTrace();

        } finally {

            AbandonedConnectionCleanupThread
                    .checkedShutdown();
        }
    }
}