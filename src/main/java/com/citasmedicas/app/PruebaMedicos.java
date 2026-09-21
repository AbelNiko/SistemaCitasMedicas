package com.citasmedicas.app;

import com.citasmedicas.model.Medico;
import com.citasmedicas.service.MedicoService;
import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;

import java.util.List;

/**
 * ================================================================
 *                    PRUEBA DE MÉDICOS
 * ================================================================
 *
 * Comprueba el filtrado de médicos por especialidad y
 * establecimiento.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public class PruebaMedicos {

    public static void main(String[] args) {

        MedicoService servicio =
                new MedicoService();

        try {

            // ====================================================
            // PRUEBA 1: COMBINACIÓN CON MÉDICO ASOCIADO
            // ====================================================

            int idMedicinaGeneral = 1;
            int idHospitalPublicoNorte = 1;

            System.out.println(
                    "=============================================="
            );
            System.out.println(
                    " PRUEBA 1 - MÉDICOS DISPONIBLES"
            );
            System.out.println(
                    "=============================================="
            );

            List<Medico> medicos =
                    servicio.listarDisponibles(
                            idMedicinaGeneral,
                            idHospitalPublicoNorte
                    );

            if (medicos.isEmpty()) {

                System.out.println(
                        "No se encontraron médicos."
                );

            } else {

                for (Medico medico : medicos) {

                    System.out.println(
                            "ID: "
                            + medico.getIdMedico()
                    );

                    System.out.println(
                            "Médico: "
                            + medico.getNombreCompleto()
                    );

                    System.out.println(
                            "Cédula profesional: "
                            + medico.getCedulaProfesional()
                    );

                    System.out.println(
                            "Estado: "
                            + (
                                medico.isEstado()
                                    ? "ACTIVO"
                                    : "INACTIVO"
                            )
                    );

                    System.out.println(
                            "----------------------------------------------"
                    );
                }
            }

            System.out.println(
                    "Total encontrados: "
                    + medicos.size()
            );

            // ====================================================
            // PRUEBA 2: COMBINACIÓN SIN MÉDICO ASOCIADO
            // ====================================================

            int idCardiologia = 2;

            System.out.println();
            System.out.println(
                    "=============================================="
            );
            System.out.println(
                    " PRUEBA 2 - FILTRO SIN RESULTADOS"
            );
            System.out.println(
                    "=============================================="
            );

            List<Medico> medicosCardiologia =
                    servicio.listarDisponibles(
                            idCardiologia,
                            idHospitalPublicoNorte
                    );

            if (medicosCardiologia.isEmpty()) {

                System.out.println(
                        "Correcto: no existen médicos asociados "
                        + "a esta combinación."
                );

            } else {

                for (Medico medico
                        : medicosCardiologia) {

                    System.out.println(
                            medico.getNombreCompleto()
                    );
                }
            }

            System.out.println(
                    "Total encontrados: "
                    + medicosCardiologia.size()
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