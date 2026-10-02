package com.citasmedicas.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;

import java.nio.file.Files;
import java.nio.file.Path;

import java.sql.Connection;
import java.sql.SQLException;

import java.util.Locale;
import java.util.Properties;

/**
 * ================================================================
 *             SISTEMA DE GESTIÓN DE CITAS MÉDICAS
 * ================================================================
 *
 * Administra las conexiones JDBC utilizadas por MediAppoint.
 *
 * La aplicación utiliza HikariCP para mantener un pequeño pool
 * de conexiones reutilizables hacia MySQL.
 *
 * Esto evita abrir una conexión física nueva con Aiven para
 * cada consulta realizada por los DAO y Services.
 *
 * La configuración puede obtenerse desde:
 *
 * 1. Archivo externo:
 *
 *    -Dmediappoint.config=ruta/database.properties
 *
 * 2. Variables de entorno:
 *
 *    DB_HOST
 *    DB_PORT
 *    DB_NAME
 *    DB_USER
 *    DB_PASSWORD
 *    DB_SSL_MODE
 *
 * Las credenciales nunca se almacenan directamente
 * en el código fuente.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 2.0
 */
public final class ConexionBD {

    /*
     * ============================================================
     * CONFIGURACIÓN DEL POOL
     * ============================================================
     *
     * MediAppoint es una aplicación de escritorio.
     * No necesita decenas de conexiones simultáneas.
     *
     * Un máximo pequeño evita consumo innecesario
     * de recursos tanto localmente como en Aiven.
     */

    private static final int MAXIMO_CONEXIONES =
            5;

    private static final int MINIMO_CONEXIONES_INACTIVAS =
            1;

    private static final long TIEMPO_ESPERA_CONEXION_MS =
            10_000L;

    private static final long TIEMPO_VALIDACION_MS =
            5_000L;

    private static final long TIEMPO_INACTIVIDAD_MS =
            120_000L;

    private static final long VIDA_MAXIMA_CONEXION_MS =
            600_000L;

    private static final long KEEP_ALIVE_MS =
            120_000L;

    /*
     * ============================================================
     * ESTADO DEL POOL
     * ============================================================
     */

    private static final Object BLOQUEO_POOL =
            new Object();

    /*
     * volatile permite que diferentes hilos vean correctamente
     * cuándo el pool ya fue creado.
     */
    private static volatile HikariDataSource dataSource;

    /**
     * La clase es exclusivamente utilitaria.
     */
    private ConexionBD() {
    }

    /*
     * ============================================================
     * OBTENCIÓN DE CONEXIONES
     * ============================================================
     */

    /**
     * Obtiene una conexión disponible desde el pool.
     *
     * Al utilizar try-with-resources sobre la conexión,
     * connection.close() no elimina la conexión física:
     * HikariCP la devuelve al pool para poder reutilizarla.
     *
     * @return conexión JDBC disponible.
     * @throws SQLException si no puede obtenerse una conexión.
     */
    public static Connection obtenerConexion()
            throws SQLException {

        return obtenerDataSource()
                .getConnection();
    }

    /**
     * Inicializa el pool únicamente cuando MediAppoint
     * realmente necesita acceder a MySQL.
     *
     * De esta forma no penalizamos innecesariamente
     * el arranque de la interfaz gráfica.
     */
    private static HikariDataSource obtenerDataSource() {

        HikariDataSource actual =
                dataSource;

        if (
                actual != null
                        && !actual.isClosed()
        ) {

            return actual;
        }

        synchronized (BLOQUEO_POOL) {

            actual =
                    dataSource;

            if (
                    actual == null
                            || actual.isClosed()
            ) {

                dataSource =
                        crearDataSource();

                actual =
                        dataSource;
            }
        }

        return actual;
    }

    /*
     * ============================================================
     * CREACIÓN DEL POOL
     * ============================================================
     */

    /**
     * Construye y configura HikariCP.
     */
    private static HikariDataSource crearDataSource() {

        Properties configuracion =
                cargarConfiguracionExterna();

        String host =
                obtenerValorObligatorio(
                        configuracion,
                        "DB_HOST"
                );

        String puerto =
                obtenerValorObligatorio(
                        configuracion,
                        "DB_PORT"
                );

        String baseDatos =
                obtenerValorObligatorio(
                        configuracion,
                        "DB_NAME"
                );

        String usuario =
                obtenerValorObligatorio(
                        configuracion,
                        "DB_USER"
                );

        String password =
                obtenerValorObligatorio(
                        configuracion,
                        "DB_PASSWORD"
                );

        String sslMode =
                obtenerValorOpcional(
                        configuracion,
                        "DB_SSL_MODE",
                        "DISABLED"
                )
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        );

        validarSslMode(
                sslMode
        );

        String url =
                construirUrl(
                        host,
                        puerto,
                        baseDatos,
                        sslMode
                );

        HikariConfig hikariConfig =
                new HikariConfig();

        /*
         * ========================================================
         * CONEXIÓN JDBC
         * ========================================================
         */

        hikariConfig.setJdbcUrl(
                url
        );

        hikariConfig.setUsername(
                usuario
        );

        hikariConfig.setPassword(
                password
        );

        hikariConfig.setDriverClassName(
                "com.mysql.cj.jdbc.Driver"
        );

        /*
         * ========================================================
         * TAMAÑO DEL POOL
         * ========================================================
         */

        hikariConfig.setMaximumPoolSize(
                MAXIMO_CONEXIONES
        );

        hikariConfig.setMinimumIdle(
                MINIMO_CONEXIONES_INACTIVAS
        );

        /*
         * ========================================================
         * TIEMPOS
         * ========================================================
         */

        hikariConfig.setConnectionTimeout(
                TIEMPO_ESPERA_CONEXION_MS
        );

        hikariConfig.setValidationTimeout(
                TIEMPO_VALIDACION_MS
        );

        hikariConfig.setIdleTimeout(
                TIEMPO_INACTIVIDAD_MS
        );

        hikariConfig.setMaxLifetime(
                VIDA_MAXIMA_CONEXION_MS
        );

        hikariConfig.setKeepaliveTime(
                KEEP_ALIVE_MS
        );

        /*
         * No obligamos a Hikari a conectarse mientras
         * se está construyendo el pool.
         *
         * Si temporalmente no existe conexión a Internet,
         * MediAppoint podrá abrir la interfaz y el error
         * se producirá únicamente al solicitar MySQL.
         */
        hikariConfig.setInitializationFailTimeout(
                -1
        );

        /*
         * ========================================================
         * COMPORTAMIENTO JDBC
         * ========================================================
         */

        hikariConfig.setAutoCommit(
                true
        );

        hikariConfig.setReadOnly(
                false
        );

        hikariConfig.setPoolName(
                "MediAppointPool"
        );

        return new HikariDataSource(
                hikariConfig
        );
    }

    /*
     * ============================================================
     * CONSTRUCCIÓN DE URL JDBC
     * ============================================================
     */

    /**
     * Construye la URL utilizada por MySQL Connector/J.
     */
    private static String construirUrl(
            String host,
            String puerto,
            String baseDatos,
            String sslMode
    ) {

        return "jdbc:mysql://"
                + host
                + ":"
                + puerto
                + "/"
                + baseDatos

                + "?sslMode="
                + sslMode

                + "&serverTimezone=America/Guayaquil"

                + "&useUnicode=true"

                + "&characterEncoding=UTF-8"

                + "&connectTimeout=10000"

                + "&socketTimeout=20000";
    }

    /*
     * ============================================================
     * ARCHIVO DE CONFIGURACIÓN
     * ============================================================
     */

    /**
     * Carga opcionalmente database.properties desde
     * la ubicación externa especificada mediante:
     *
     * -Dmediappoint.config=...
     *
     * Si no se proporciona archivo, la aplicación
     * utilizará las variables de entorno.
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

        if (
                !Files.isRegularFile(
                        archivo
                )
        ) {

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

    /*
     * ============================================================
     * OBTENCIÓN DE CONFIGURACIÓN
     * ============================================================
     */

    /**
     * Obtiene una configuración obligatoria.
     *
     * Orden:
     *
     * 1. database.properties
     * 2. variable de entorno
     */
    private static String obtenerValorObligatorio(
            Properties configuracion,
            String nombre
    ) {

        String valor =
                obtenerValor(
                        configuracion,
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
         * Una contraseña puede contener espacios,
         * por lo que no usamos trim() sobre ella.
         */
        if (
                "DB_PASSWORD".equals(
                        nombre
                )
        ) {

            return valor;
        }

        return valor.trim();
    }

    /**
     * Obtiene una configuración opcional.
     */
    private static String obtenerValorOpcional(
            Properties configuracion,
            String nombre,
            String valorPredeterminado
    ) {

        String valor =
                obtenerValor(
                        configuracion,
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
     * Busca primero en el archivo externo.
     * Si no existe allí, consulta el entorno.
     */
    private static String obtenerValor(
            Properties configuracion,
            String nombre
    ) {

        String valorArchivo =
                configuracion.getProperty(
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

    /*
     * ============================================================
     * VALIDACIÓN SSL
     * ============================================================
     */

    /**
     * Comprueba que DB_SSL_MODE corresponda a un valor
     * soportado por MySQL Connector/J.
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

    /*
     * ============================================================
     * CIERRE DEL POOL
     * ============================================================
     */

    /**
     * Libera todas las conexiones del pool al cerrar
     * MediAppoint.
     *
     * Es seguro llamar este método aunque el pool nunca
     * haya sido inicializado.
     */
    public static void cerrarPool() {

        synchronized (BLOQUEO_POOL) {

            if (
                    dataSource == null
                            || dataSource.isClosed()
            ) {

                return;
            }

            dataSource.close();

            dataSource =
                    null;
        }
    }
}