package com.citasmedicas.app;

import com.citasmedicas.model.Rol;
import com.citasmedicas.model.Usuario;
import com.citasmedicas.service.RolService;
import com.citasmedicas.service.UsuarioService;
import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;

import java.util.List;

/**
 * ================================================================
 *                PRUEBA DE CAPA SERVICE
 * ================================================================
 *
 * Clase temporal utilizada para verificar el funcionamiento
 * de las reglas de negocio de UsuarioService y RolService.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public class PruebaService {

    /**
     * Punto de entrada de la prueba.
     *
     * @param args argumentos de línea de comandos.
     */
    public static void main(String[] args) {

        try {

            probarRoles();

            System.out.println();

            probarUsuario();

            System.out.println();

            probarValidacionCorreo();

        } finally {

            cerrarHiloMySQL();
        }
    }

    /**
     * Prueba la consulta de roles mediante RolService.
     */
    private static void probarRoles() {

        System.out.println(
                "=============================================="
        );

        System.out.println(
                " PRUEBA SERVICE - ROLES"
        );

        System.out.println(
                "=============================================="
        );

        try {

            RolService rolService =
                    new RolService();

            List<Rol> roles =
                    rolService.listarTodos();

            for (Rol rol : roles) {

                System.out.println(
                        rol.getIdRol()
                        + " - "
                        + rol.getNombre()
                );
            }

        } catch (Exception e) {

            System.err.println(
                    "Error al consultar roles: "
                    + e.getMessage()
            );
        }
    }

    /**
     * Prueba la búsqueda de un usuario mediante UsuarioService.
     */
    private static void probarUsuario() {

        System.out.println(
                "=============================================="
        );

        System.out.println(
                " PRUEBA SERVICE - USUARIO"
        );

        System.out.println(
                "=============================================="
        );

        try {

            UsuarioService usuarioService =
                    new UsuarioService();

            Usuario usuario =
                    usuarioService.buscarPorCorreo(
                            "ana.torres@prueba.local"
                    );

            if (usuario != null) {

                System.out.println(
                        "Usuario encontrado:"
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

            } else {

                System.out.println(
                        "Usuario no encontrado."
                );
            }

        } catch (Exception e) {

            System.err.println(
                    "Error al consultar usuario: "
                    + e.getMessage()
            );
        }
    }

    /**
     * Comprueba que UsuarioService rechace
     * correctamente un correo inválido.
     */
    private static void probarValidacionCorreo() {

        System.out.println(
                "=============================================="
        );

        System.out.println(
                " PRUEBA DE VALIDACIÓN"
        );

        System.out.println(
                "=============================================="
        );

        try {

            UsuarioService usuarioService =
                    new UsuarioService();

            usuarioService.buscarPorCorreo(
                    "correo-invalido"
            );

            System.err.println(
                    "Error: la validación permitió un correo inválido."
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
     * Finaliza el hilo interno de limpieza creado
     * por MySQL Connector/J durante las pruebas.
     */
    private static void cerrarHiloMySQL() {

    AbandonedConnectionCleanupThread
            .checkedShutdown();
}
}