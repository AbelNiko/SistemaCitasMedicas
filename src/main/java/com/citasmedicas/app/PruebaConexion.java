package com.citasmedicas.app;

import com.citasmedicas.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * ================================================================
 *             PRUEBA DE CONEXIÓN CON MYSQL
 * ================================================================
 *
 * Clase temporal utilizada para comprobar que la aplicación
 * Java puede establecer comunicación correctamente con MySQL.
 *
 * Esta clase será eliminada posteriormente cuando la conexión
 * quede integrada dentro de la arquitectura definitiva.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public class PruebaConexion {

    /**
     * Punto de entrada de la prueba.
     *
     * @param args argumentos de línea de comandos.
     */
    public static void main(String[] args) {

        System.out.println(
                "=============================================="
        );

        System.out.println(
                " PRUEBA DE CONEXIÓN - SISTEMA CITAS MÉDICAS"
        );

        System.out.println(
                "=============================================="
        );

        try (
                Connection conexion =
                        ConexionBD.obtenerConexion()
        ) {

            System.out.println(
                    "Conexión establecida correctamente con MySQL."
            );

            probarBaseDatos(conexion);

        } catch (Exception e) {

            System.err.println(
                    "No fue posible conectar con la base de datos."
            );

            System.err.println(
                    "Detalle: " + e.getMessage()
            );
        }
    }

    /**
     * Ejecuta una consulta de comprobación sobre la conexión.
     *
     * @param conexion conexión activa con MySQL.
     * @throws Exception si ocurre un error durante la consulta.
     */
    private static void probarBaseDatos(
            Connection conexion
    ) throws Exception {

        String sql =
                "SELECT DATABASE() AS base_datos, "
                + "VERSION() AS version_mysql, "
                + "CURRENT_USER() AS usuario_mysql";

        try (
                PreparedStatement sentencia =
                        conexion.prepareStatement(sql);

                ResultSet resultado =
                        sentencia.executeQuery()
        ) {

            if (resultado.next()) {

                System.out.println(
                        "Base de datos: "
                        + resultado.getString("base_datos")
                );

                System.out.println(
                        "Versión MySQL: "
                        + resultado.getString("version_mysql")
                );

                System.out.println(
                        "Usuario MySQL: "
                        + resultado.getString("usuario_mysql")
                );
            }
        }
    }
}