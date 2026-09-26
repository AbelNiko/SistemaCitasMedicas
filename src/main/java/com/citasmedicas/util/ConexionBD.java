package com.citasmedicas.util;

import java.io.IOException;
import java.io.InputStream;

import java.nio.file.Files;
import java.nio.file.Path;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import java.util.Locale;
import java.util.Properties;

/**
 * ================================================================
 *             SISTEMA DE GESTIÓN DE CITAS MÉDICAS
 * ================================================================
 *
 * Administra la conexión con MySQL.
 *
 * La configuración puede obtenerse desde:
 *
 * 1. Archivo externo definido mediante:
 *    -Dmediappoint.config=ruta/database.properties
 *
 * 2. Variables de entorno:
 *
 * DB_HOST
 * DB_PORT
 * DB_NAME
 * DB_USER
 * DB_PASSWORD
 * DB_SSL_MODE
 *
 * Esto permite utilizar variables de entorno durante el
 * desarrollo y un archivo externo en la versión ejecutable.
 *
 * Las credenciales no se almacenan directamente en el
 * código fuente.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.2
 */
public final class ConexionBD {

    /* ============================================================
                       CONFIGURACIÓN EXTERNA
       ============================================================ */

    private static final Properties CONFIGURACION =
            cargarConfiguracionExterna();

    /* ============================================================
                          DATOS DE CONEXIÓN
       ============================================================ */

    private static final String HOST =
            obtenerValorObligatorio(
                    "DB_HOST"
            );

    private static final String PUERTO =
            obtenerValorObligatorio(
                    "DB_PORT"
            );

    private static final String BASE_DATOS =
            obtenerValorObligatorio(
                    "DB_NAME"
            );

    private static final String USUARIO =
            obtenerValorObligatorio(
                    "DB_USER"
            );

    private static final String PASSWORD =
            obtenerValorObligatorio(
                    "DB_PASSWORD"
            );

    private static final String SSL_MODE =
            obtenerValorOpcional(
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
                        OBTENCIÓN DE CONEXIÓN
       ============================================================ */

    /**
     * Obtiene una conexión activa con MySQL.
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
                      CONSTRUCCIÓN DE LA URL
       ============================================================ */

    /**
     * Construye la URL JDBC de conexión.
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

                + "&connectTimeout=10000"

                + "&socketTimeout=20000";
    }

    /* ============================================================
                     ARCHIVO DE CONFIGURACIÓN
       ============================================================ */

    /**
     * Carga el archivo externo indicado mediante
     * la propiedad mediappoint.config.
     *
     * Si no se define la propiedad, se utilizarán
     * las variables de entorno.
     */
    private static Properties cargarConfiguracionExterna() {

        Properties propiedades =
                new Properties();

        String rutaConfiguracion =
                System.getProperty(
                        "mediappoint.config"
                );

        if (
                rutaConfiguracion == null
                        || rutaConfiguracion.isBlank()
        ) {

            return propiedades;
        }

        Path archivo =
                Path.of(
                        rutaConfiguracion.trim()
                )
                        .toAbsolutePath()
                        .normalize();

        if (!Files.isRegularFile(archivo)) {

            throw new IllegalStateException(
                    "No se encontró el archivo de configuración: "
                            + archivo
            );
        }

        try (
                InputStream entrada =
                        Files.newInputStream(
                                archivo
                        )
        ) {

            propiedades.load(
                    entrada
            );

            return propiedades;

        } catch (IOException e) {

            throw new IllegalStateException(
                    "No fue posible leer la configuración "
                            + "de la base de datos.",
                    e
            );
        }
    }

    /* ============================================================
                         OBTENCIÓN DE VALORES
       ============================================================ */

    /**
     * Obtiene un valor obligatorio.
     *
     * Se consulta primero el archivo externo.
     * Si no existe allí, se consulta la variable
     * de entorno correspondiente.
     */
    private static String obtenerValorObligatorio(
            String nombre
    ) {

        String valor =
                obtenerValor(
                        nombre
                );

        if (
                valor == null
                        || valor.isBlank()
        ) {

            throw new IllegalStateException(
                    "La configuración "
                            + nombre
                            + " no está definida."
            );
        }

        /*
         * En contraseñas no eliminamos espacios
         * automáticamente porque forman parte
         * potencial del valor.
         */
        if ("DB_PASSWORD".equals(nombre)) {

            return valor;
        }

        return valor.trim();
    }

    /**
     * Obtiene un valor opcional.
     */
    private static String obtenerValorOpcional(
            String nombre,
            String valorPredeterminado
    ) {

        String valor =
                obtenerValor(
                        nombre
                );

        if (
                valor == null
                        || valor.isBlank()
        ) {

            return valorPredeterminado;
        }

        return valor;
    }

    /**
     * Busca primero en el archivo externo y posteriormente
     * en las variables de entorno.
     */
    private static String obtenerValor(
            String nombre
    ) {

        String valorArchivo =
                CONFIGURACION.getProperty(
                        nombre
                );

        if (
                valorArchivo != null
                        && !valorArchivo.isBlank()
        ) {

            return valorArchivo;
        }

        return System.getenv(
                nombre
        );
    }

    /* ============================================================
                         VALIDACIÓN SSL
       ============================================================ */

    /**
     * Valida el modo SSL admitido por MySQL Connector/J.
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
                    "El modo SSL configurado no es válido: "
                            + sslMode
            );
        }
    }
}