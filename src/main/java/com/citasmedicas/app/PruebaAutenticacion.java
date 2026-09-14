package com.citasmedicas.app;

import com.citasmedicas.model.Usuario;
import com.citasmedicas.service.AutenticacionService;
import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;

/**
 * ================================================================
 *                PRUEBA DE AUTENTICACIÓN
 * ================================================================
 *
 * Clase temporal utilizada para verificar el proceso
 * de autenticación de usuarios.
 *
 * Se comprueban dos escenarios:
 *
 * 1. Credenciales correctas.
 * 2. Contraseña incorrecta.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public class PruebaAutenticacion {

    /**
     * Punto de entrada de la prueba.
     *
     * @param args argumentos de línea de comandos.
     */
    public static void main(String[] args) {

        try {

            probarCredencialesCorrectas();

            System.out.println();

            probarPasswordIncorrecto();

        } finally {

            cerrarHiloMySQL();
        }
    }

    /**
     * Comprueba que un usuario pueda autenticarse
     * con credenciales válidas.
     */
    private static void probarCredencialesCorrectas() {

        System.out.println(
                "=============================================="
        );

        System.out.println(
                " PRUEBA DE AUTENTICACIÓN CORRECTA"
        );

        System.out.println(
                "=============================================="
        );

        try {

            AutenticacionService servicio =
                    new AutenticacionService();

            Usuario usuario =
                    servicio.autenticar(
                            "ana.torres@prueba.local",
                            "Prueba123*"
                    );

            if (usuario != null) {

                System.out.println(
                        "Autenticación correcta."
                );

                System.out.println(
                        "Usuario: "
                        + usuario.getNombreCompleto()
                );

                System.out.println(
                        "Correo: "
                        + usuario.getCorreo()
                );

                System.out.println(
                        "Rol: "
                        + usuario.getRol().getNombre()
                );

            } else {

                System.err.println(
                        "Error: las credenciales válidas fueron rechazadas."
                );
            }

        } catch (Exception e) {

            System.err.println(
                    "Error durante la autenticación: "
                    + e.getMessage()
            );
        }
    }

    /**
     * Comprueba que el sistema rechace una
     * contraseña incorrecta.
     */
    private static void probarPasswordIncorrecto() {

        System.out.println(
                "=============================================="
        );

        System.out.println(
                " PRUEBA DE CONTRASEÑA INCORRECTA"
        );

        System.out.println(
                "=============================================="
        );

        try {

            AutenticacionService servicio =
                    new AutenticacionService();

            Usuario usuario =
                    servicio.autenticar(
                            "ana.torres@prueba.local",
                            "PasswordIncorrecto"
                    );

            if (usuario == null) {

                System.out.println(
                        "Validación correcta: acceso rechazado."
                );

            } else {

                System.err.println(
                        "Error: se permitió el acceso con una contraseña incorrecta."
                );
            }

        } catch (Exception e) {

            System.err.println(
                    "Error durante la prueba: "
                    + e.getMessage()
            );
        }
    }

    /**
     * Finaliza el hilo interno utilizado por
     * MySQL Connector/J durante las pruebas.
     */
    private static void cerrarHiloMySQL() {

        AbandonedConnectionCleanupThread
                .checkedShutdown();
    }
}