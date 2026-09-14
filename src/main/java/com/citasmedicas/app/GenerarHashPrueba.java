package com.citasmedicas.app;

import com.citasmedicas.util.PasswordUtil;

/**
 * ================================================================
 *               GENERADOR TEMPORAL DE HASH
 * ================================================================
 *
 * Utilidad utilizada únicamente durante el desarrollo
 * para generar hashes BCrypt de prueba.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public class GenerarHashPrueba {

    public static void main(String[] args) {

        String passwordPrueba =
                "ATorres123";

        String hash =
                PasswordUtil.generarHash(
                        passwordPrueba
                );

        System.out.println(
                "Hash BCrypt generado:"
        );

        System.out.println(hash);
    }
}