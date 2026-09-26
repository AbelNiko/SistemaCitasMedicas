package com.citasmedicas.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Locale;

/**
 * ================================================================
 *             SISTEMA DE GESTIÓN DE CITAS MÉDICAS
 * ================================================================
 *
 * Clase responsable de administrar la conexión con MySQL.
 *
 * Las credenciales no se almacenan directamente en el código.
 * Se obtienen mediante variables de entorno.
 *
 * Variables obligatorias:
 *
 * DB_HOST
 * DB_PORT
 * DB_NAME
 * DB_USER
 * DB_PASSWORD
 *
 * Variable opcional:
 *
 * DB_SSL_MODE
 *
 * Valores admitidos:
 * DISABLED
 * PREFERRED
 * REQUIRED
 * VERIFY_CA
 * VERIFY_IDENTITY
 *
 * Para la base local puede utilizarse DISABLED.
 * Para Aiven se utiliza REQUIRED.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.1
 */
public final class ConexionBD {

    /* ============================================================
                         VARIABLES DE ENTORNO
       ============================================================ */

    private static final String HOST =
            obtenerVariable(
                    "DB_HOST"
            );

    private static final String PUERTO =
            obtenerVariable(
                    "DB_PORT"
            );

    private static final String BASE_DATOS =
            obtenerVariable(
                    "DB_NAME"
            );

    private static final String USUARIO =
            obtenerVariable(
                    "DB_USER"
            );

    private static final String PASSWORD =
            obtenerVariable(
                    "DB_PASSWORD"
            );

    /*
     * DISABLED se mantiene como valor predeterminado
     * para conservar compatibilidad con el servidor
     * MySQL local utilizado durante el desarrollo.
     */
    private static final String SSL_MODE =
            obtenerVariableOpcional(
                    "DB_SSL_MODE",
                    "DISABLED"
            )
                    .trim()
                    .toUpperCase(
                            Locale.ROOT
                    );

    /* ============================================================
                              URL JDBC
       ============================================================ */

    private static final String URL =
            construirUrl();

    /**
     * Constructor privado.
     */
    private ConexionBD() {
    }

    /* ============================================================
                             CONEXIÓN
       ============================================================ */

    /**
     * Establece una conexión con MySQL.
     *
     * @return conexión activa.
     * @throws SQLException si ocurre un error.
     */
    public static Connection obtenerConexion()
            throws SQLException {

        return DriverManager.getConnection(
                URL,
                USUARIO,
                PASSWORD
        );
    }

    /* ============================================================
                         CONSTRUCCIÓN DE URL
       ============================================================ */

    /**
     * Construye la URL JDBC utilizando la configuración
     * definida mediante variables de entorno.
     *
     * @return URL JDBC preparada.
     */
    private static String construirUrl() {

        validarSslMode(
                SSL_MODE
        );

        return "jdbc:mysql://"
                + HOST
                + ":"
                + PUERTO
                + "/"
                + BASE_DATOS

                + "?sslMode="
                + SSL_MODE

                + "&serverTimezone=America/Guayaquil"

                + "&useUnicode=true"

                + "&characterEncoding=UTF-8"

                /*
                 * Máximo de 10 segundos para establecer
                 * una conexión con el servidor.
                 */
                + "&connectTimeout=10000"

                /*
                 * Máximo de 20 segundos esperando una
                 * respuesta de MySQL.
                 */
                + "&socketTimeout=20000";
    }

    /* ============================================================
                           VALIDACIONES
       ============================================================ */

    /**
     * Comprueba que el modo SSL sea válido.
     *
     * @param sslMode modo configurado.
     */
    private static void validarSslMode(
            String sslMode
    ) {

        boolean valido =
                switch (sslMode) {

                    case "DISABLED",
                         "PREFERRED",
                         "REQUIRED",
                         "VERIFY_CA",
                         "VERIFY_IDENTITY" -> true;

                    default -> false;
                };

        if (!valido) {

            throw new IllegalStateException(
                    "El valor configurado en DB_SSL_MODE "
                            + "no es válido: "
                            + sslMode
            );
        }
    }

    /* ============================================================
                       VARIABLES DE ENTORNO
       ============================================================ */

    /**
     * Obtiene una variable obligatoria.
     *
     * @param nombre nombre de la variable.
     * @return valor configurado.
     */
    private static String obtenerVariable(
            String nombre
    ) {

        String valor =
                System.getenv(
                        nombre
                );

        if (
                valor == null
                        || valor.isBlank()
        ) {

            throw new IllegalStateException(
                    "La variable de entorno "
                            + nombre
                            + " no está configurada."
            );
        }

        return valor.trim();
    }

    /**
     * Obtiene una variable opcional.
     *
     * @param nombre nombre de la variable.
     * @param valorPredeterminado valor usado si no existe.
     * @return valor configurado.
     */
    private static String obtenerVariableOpcional(
            String nombre,
            String valorPredeterminado
    ) {

        String valor =
                System.getenv(
                        nombre
                );

        if (
                valor == null
                        || valor.isBlank()
        ) {

            return valorPredeterminado;
        }

        return valor.trim();
    }
}