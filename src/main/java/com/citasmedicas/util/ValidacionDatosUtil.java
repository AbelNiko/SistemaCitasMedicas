package com.citasmedicas.util;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * ================================================================
 *                 UTIL - VALIDACIÓN DE DATOS
 * ================================================================
 *
 * Centraliza las reglas comunes de validación y normalización
 * utilizadas por los formularios de MediAppoint.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public final class ValidacionDatosUtil {

    private static final Pattern PATRON_NOMBRE_PERSONA =
            Pattern.compile(
                    "^[\\p{L}]+(?: [\\p{L}]+)*$"
            );

    private static final Pattern PATRON_CORREO =
            Pattern.compile(
                    "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
            );

    private ValidacionDatosUtil() {
    }

    /**
     * Normaliza espacios al inicio, final e interior.
     */
    public static String normalizarEspacios(
            String valor
    ) {

        if (valor == null) {
            return "";
        }

        return valor
                .trim()
                .replaceAll(
                        "\\s+",
                        " "
                );
    }

    /**
     * Normaliza nombres y apellidos.
     */
    public static String normalizarNombre(
            String valor
    ) {

        return normalizarEspacios(
                valor
        );
    }

    /**
     * Normaliza un correo electrónico.
     */
    public static String normalizarCorreo(
            String correo
    ) {

        return normalizarEspacios(
                correo
        )
                .toLowerCase(
                        Locale.ROOT
                );
    }

    /**
     * Valida nombres de personas.
     *
     * Se permiten únicamente letras Unicode y espacios.
     * Esto permite correctamente caracteres como:
     *
     * á, é, í, ó, ú, ü, ñ
     */
    public static void validarNombrePersona(
            String valor,
            String nombreCampo,
            int longitudMaxima
    ) {

        String normalizado =
                normalizarNombre(
                        valor
                );

        if (normalizado.isEmpty()) {

            throw new IllegalArgumentException(
                    nombreCampo
                            + " es obligatorio."
            );
        }

        if (
                normalizado.length()
                        > longitudMaxima
        ) {

            throw new IllegalArgumentException(
                    nombreCampo
                            + " no puede superar los "
                            + longitudMaxima
                            + " caracteres."
            );
        }

        if (
                !PATRON_NOMBRE_PERSONA
                        .matcher(
                                normalizado
                        )
                        .matches()
        ) {

            throw new IllegalArgumentException(
                    nombreCampo
                            + " solo puede contener letras y espacios."
            );
        }
    }

    /**
     * Valida de forma opcional el nombre de una persona.
     */
    public static void validarNombrePersonaOpcional(
            String valor,
            String nombreCampo,
            int longitudMaxima
    ) {

        String normalizado =
                normalizarNombre(
                        valor
                );

        if (normalizado.isEmpty()) {
            return;
        }

        validarNombrePersona(
                normalizado,
                nombreCampo,
                longitudMaxima
        );
    }

    /**
     * Valida una cédula con formato de 10 dígitos.
     */
    public static void validarCedula(
            String cedula
    ) {

        if (
                cedula == null
                        || cedula.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "La cédula es obligatoria."
            );
        }

        if (
                !cedula
                        .trim()
                        .matches("\\d{10}")
        ) {

            throw new IllegalArgumentException(
                    "La cédula debe contener exactamente 10 dígitos."
            );
        }
    }

    /**
     * Valida correo electrónico.
     */
    public static void validarCorreo(
            String correo
    ) {

        String normalizado =
                normalizarCorreo(
                        correo
                );

        if (normalizado.isEmpty()) {

            throw new IllegalArgumentException(
                    "El correo electrónico es obligatorio."
            );
        }

        if (normalizado.length() > 150) {

            throw new IllegalArgumentException(
                    "El correo electrónico no puede superar "
                            + "los 150 caracteres."
            );
        }

        if (
                !PATRON_CORREO
                        .matcher(
                                normalizado
                        )
                        .matches()
        ) {

            throw new IllegalArgumentException(
                    "El formato del correo electrónico no es válido."
            );
        }
    }

    /**
     * Valida teléfonos opcionales.
     */
    public static void validarTelefonoOpcional(
            String telefono,
            String nombreCampo
    ) {

        if (
                telefono == null
                        || telefono.isBlank()
        ) {

            return;
        }

        String normalizado =
                telefono.trim();

        if (
                !normalizado.matches(
                        "\\d{7,10}"
                )
        ) {

            throw new IllegalArgumentException(
                    nombreCampo
                            + " debe contener entre 7 y 10 dígitos."
            );
        }
    }
}