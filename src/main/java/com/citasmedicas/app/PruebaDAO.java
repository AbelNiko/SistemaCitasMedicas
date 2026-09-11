package com.citasmedicas.app;

import com.citasmedicas.dao.RolDAO;
import com.citasmedicas.dao.UsuarioDAO;
import com.citasmedicas.model.Rol;
import com.citasmedicas.model.Usuario;

import java.util.List;

/**
 * ================================================================
 *                   PRUEBA DE CAPA DAO
 * ================================================================
 *
 * Clase temporal utilizada para comprobar las consultas
 * realizadas mediante RolDAO y UsuarioDAO.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public class PruebaDAO {

    public static void main(String[] args) {

        probarRoles();

        System.out.println();

        probarUsuario();
    }

    private static void probarRoles() {

        System.out.println(
                "=============================================="
        );

        System.out.println(
                " PRUEBA DAO - ROLES"
        );

        System.out.println(
                "=============================================="
        );

        try {

            RolDAO rolDAO = new RolDAO();

            List<Rol> roles =
                    rolDAO.listarTodos();

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

    private static void probarUsuario() {

        System.out.println(
                "=============================================="
        );

        System.out.println(
                " PRUEBA DAO - USUARIO"
        );

        System.out.println(
                "=============================================="
        );

        try {

            UsuarioDAO usuarioDAO =
                    new UsuarioDAO();

            Usuario usuario =
                    usuarioDAO.buscarPorCorreo(
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
}