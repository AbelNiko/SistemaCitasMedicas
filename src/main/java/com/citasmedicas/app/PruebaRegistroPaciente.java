package com.citasmedicas.app;

import com.citasmedicas.model.Paciente;
import com.citasmedicas.model.Usuario;
import com.citasmedicas.service.AutenticacionService;
import com.citasmedicas.service.RegistroPacienteService;
import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;

import java.time.LocalDate;

/**
 * ================================================================
 *              PRUEBA DE REGISTRO DE PACIENTE
 * ================================================================
 *
 * Clase temporal utilizada para comprobar el proceso completo
 * de registro de una nueva cuenta de paciente.
 *
 * Se verifican los siguientes escenarios:
 *
 * 1. Registro correcto de un nuevo paciente.
 * 2. Autenticación utilizando la cuenta recién creada.
 * 3. Rechazo de un correo electrónico duplicado.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public class PruebaRegistroPaciente {

    private static final String CORREO_PRUEBA =
            "registro.paciente@prueba.local";

    private static final String PASSWORD_PRUEBA =
            "Paciente123*";

    public static void main(String[] args) {

        try {

            probarRegistro();

            System.out.println();

            probarAutenticacion();

            System.out.println();

            probarCorreoDuplicado();

        } finally {

            cerrarHiloMySQL();
        }
    }

    /**
     * Comprueba el registro de un nuevo paciente.
     */
    private static void probarRegistro() {

        System.out.println(
                "=============================================="
        );

        System.out.println(
                " PRUEBA DE REGISTRO DE PACIENTE"
        );

        System.out.println(
                "=============================================="
        );

        try {

            RegistroPacienteService servicio =
                    new RegistroPacienteService();

            Paciente paciente =
                    servicio.registrar(
                            "María",
                            "Gómez",
                            "0999999999",
                            CORREO_PRUEBA,
                            "0999999999",
                            PASSWORD_PRUEBA,
                            PASSWORD_PRUEBA,
                            LocalDate.of(1998, 5, 20),
                            "Guayaquil",
                            "Femenino",
                            "Carlos Gómez",
                            "0988888888"
                    );

            Usuario usuario =
                    paciente.getUsuario();

            System.out.println(
                    "Paciente registrado correctamente."
            );

            System.out.println(
                    "ID usuario: "
                    + usuario.getIdUsuario()
            );

            System.out.println(
                    "Nombre: "
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

            System.out.println(
                    "Contraseña almacenada mediante BCrypt: "
                    + (
                            usuario.getPasswordHash() != null
                            && usuario.getPasswordHash()
                                    .startsWith("$2")
                    )
            );

        } catch (IllegalArgumentException e) {

            System.err.println(
                    "Validación: "
                    + e.getMessage()
            );

        } catch (Exception e) {

            System.err.println(
                    "Error durante el registro: "
                    + e.getMessage()
            );
        }
    }

    /**
     * Comprueba que la cuenta recién registrada
     * pueda iniciar sesión.
     */
    private static void probarAutenticacion() {

        System.out.println(
                "=============================================="
        );

        System.out.println(
                " PRUEBA DE AUTENTICACIÓN DEL NUEVO PACIENTE"
        );

        System.out.println(
                "=============================================="
        );

        try {

            AutenticacionService servicio =
                    new AutenticacionService();

            Usuario usuario =
                    servicio.autenticar(
                            CORREO_PRUEBA,
                            PASSWORD_PRUEBA
                    );

            if (usuario != null) {

                System.out.println(
                        "Autenticación correcta."
                );

                System.out.println(
                        "Bienvenido, "
                        + usuario.getNombreCompleto()
                );

                System.out.println(
                        "Rol: "
                        + usuario.getRol().getNombre()
                );

            } else {

                System.err.println(
                        "Error: la nueva cuenta no pudo autenticarse."
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
     * Comprueba que el sistema impida registrar
     * nuevamente un correo ya existente.
     */
    private static void probarCorreoDuplicado() {

        System.out.println(
                "=============================================="
        );

        System.out.println(
                " PRUEBA DE CORREO DUPLICADO"
        );

        System.out.println(
                "=============================================="
        );

        try {

            RegistroPacienteService servicio =
                    new RegistroPacienteService();

            servicio.registrar(
                    "Otro",
                    "Paciente",
                    "0988888888",
                    CORREO_PRUEBA,
                    "0977777777",
                    "OtraClave123*",
                    "OtraClave123*",
                    LocalDate.of(2000, 1, 15),
                    "Guayaquil",
                    "Masculino",
                    null,
                    null
            );

            System.err.println(
                    "ERROR: el sistema permitió un correo duplicado."
            );

        } catch (IllegalArgumentException e) {

            System.out.println(
                    "Validación correcta: "
                    + e.getMessage()
            );

        } catch (Exception e) {

            System.err.println(
                    "Error inesperado: "
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