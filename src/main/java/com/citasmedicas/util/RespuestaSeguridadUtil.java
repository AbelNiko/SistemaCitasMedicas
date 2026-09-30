package com.citasmedicas.util;

import java.text.Normalizer;
import java.util.Locale;

/**
 * ================================================================
 *             UTILIDAD - RESPUESTA DE SEGURIDAD
 * ================================================================
 *
 * Gestiona la validación, normalización y protección
 * de las respuestas de seguridad.
 *
 * La respuesta nunca se almacena en texto plano.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public final class RespuestaSeguridadUtil {

    private static final int LONGITUD_MINIMA = 3;
    private static final int LONGITUD_MAXIMA = 100;

    private RespuestaSeguridadUtil() {
    }

    /**
     * Valida la respuesta durante el registro.
     */
    public static void validarParaRegistro(
            String respuesta
    ) {

        if (
                respuesta == null
                        || respuesta.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "La respuesta de seguridad es obligatoria."
            );
        }

        String original =
                respuesta.trim();

        if (
                original.length()
                        < LONGITUD_MINIMA
        ) {

            throw new IllegalArgumentException(
                    "La respuesta de seguridad debe contener "
                            + "al menos 3 caracteres."
            );
        }

        if (
                original.length()
                        > LONGITUD_MAXIMA
        ) {

            throw new IllegalArgumentException(
                    "La respuesta de seguridad no puede superar "
                            + "los 100 caracteres."
            );
        }

        String normalizada =
                normalizar(
                        original
                );

        if (!original.equals(normalizada)) {

            throw new IllegalArgumentException(
                    "Por favor, ingrese la respuesta "
                            + "sin mayúsculas ni tildes."
            );
        }
    }

    /**
     * Convierte una respuesta a minúsculas,
     * elimina tildes y espacios innecesarios.
     */
    public static String normalizar(
            String respuesta
    ) {

        if (respuesta == null) {
            return "";
        }

        String sinTildes =
                Normalizer.normalize(
                                respuesta,
                                Normalizer.Form.NFD
                        )
                        .replaceAll(
                                "\\p{M}+",
                                ""
                        );

        return sinTildes
                .toLowerCase(
                        Locale.ROOT
                )
                .trim()
                .replaceAll(
                        "\\s+",
                        " "
                );
    }

    /**
     * Genera el hash BCrypt que será almacenado.
     */
    public static String generarHash(
            String respuesta
    ) {

        validarParaRegistro(
                respuesta
        );

        return PasswordUtil.generarHash(
                normalizar(
                        respuesta
                )
        );
    }

    /**
     * Verifica posteriormente la respuesta durante
     * la recuperación de contraseña.
     */
    public static boolean verificar(
            String respuestaIngresada,
            String hashGuardado
    ) {

        if (
                respuestaIngresada == null
                        || respuestaIngresada.isBlank()
                        || hashGuardado == null
                        || hashGuardado.isBlank()
        ) {

            return false;
        }

        return PasswordUtil.verificar(
                normalizar(
                        respuestaIngresada
                ),
                hashGuardado
        );
    }
}