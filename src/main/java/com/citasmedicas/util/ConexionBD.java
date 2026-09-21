package com.citasmedicas.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * ================================================================
 *             SISTEMA DE GESTIÓN DE CITAS MÉDICAS
 * ================================================================
 *
 * Clase responsable de administrar la conexión con la
 * base de datos MySQL.
 *
 * Las credenciales no se almacenan directamente en el código.
 * Se obtienen mediante variables de entorno del sistema operativo.
 *
 * Variables requeridas:
 *
 * DB_HOST
 * DB_PORT
 * DB_NAME
 * DB_USER
 * DB_PASSWORD
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public final class ConexionBD {

    // ============================================================
    // VARIABLES DE ENTORNO
    // ============================================================

    private static final String HOST =
            obtenerVariable("DB_HOST");

    private static final String PUERTO =
            obtenerVariable("DB_PORT");

    private static final String BASE_DATOS =
            obtenerVariable("DB_NAME");

    private static final String USUARIO =
            obtenerVariable("DB_USER");

    private static final String PASSWORD =
            obtenerVariable("DB_PASSWORD");

    // ============================================================
    // URL JDBC
    // ============================================================

    private static final String URL =
        "jdbc:mysql://"
        + HOST
        + ":"
        + PUERTO
        + "/"
        + BASE_DATOS
        + "?useSSL=false"
        + "&allowPublicKeyRetrieval=true"
        + "&serverTimezone=America/Guayaquil"
        + "&useUnicode=true"
        + "&characterEncoding=UTF-8";

    /**
     * Constructor privado.
     *
     * Impide crear instancias de esta clase,
     * ya que solamente contiene métodos estáticos.
     */
    private ConexionBD() {
    }

    /**
     * Establece una conexión con la base de datos.
     *
     * @return conexión activa con MySQL.
     * @throws SQLException si ocurre un error de conexión.
     */
    public static Connection obtenerConexion()
            throws SQLException {

        return DriverManager.getConnection(
                URL,
                USUARIO,
                PASSWORD
        );
    }

    /**
     * Obtiene una variable de entorno obligatoria.
     *
     * Si la variable no existe o está vacía,
     * la aplicación finaliza mostrando un mensaje claro.
     *
     * @param nombre nombre de la variable.
     * @return valor de la variable.
     */
    private static String obtenerVariable(String nombre) {

        String valor = System.getenv(nombre);

        if (valor == null || valor.isBlank()) {

            throw new IllegalStateException(
                    "La variable de entorno "
                    + nombre
                    + " no está configurada."
            );
        }

        return valor;
    }
}