package com.citasmedicas.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * ================================================================
 *                 UTILIDAD DE CONTRASEÑAS
 * ================================================================
 *
 * Genera y verifica contraseñas mediante BCrypt.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public final class PasswordUtil {

    private static final int COSTO_BCRYPT = 12;

    private PasswordUtil() {
    }

    public static String generarHash(String password) {

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException(
                    "La contraseña es obligatoria."
            );
        }

        return BCrypt.hashpw(
                password,
                BCrypt.gensalt(COSTO_BCRYPT)
        );
    }

    public static boolean verificar(
            String password,
            String hash
    ) {

        if (password == null
                || password.isBlank()
                || hash == null
                || hash.isBlank()) {

            return false;
        }

        try {

            return BCrypt.checkpw(
                    password,
                    hash
            );

        } catch (IllegalArgumentException e) {

            return false;
        }
    }
}