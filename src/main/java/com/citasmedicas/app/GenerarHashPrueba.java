package com.citasmedicas.app;

import com.citasmedicas.util.PasswordUtil;

/**
 * Utilidad temporal para generar un hash BCrypt de prueba.
 */
public class GenerarHashPrueba {

    public static void main(String[] args) {

        String passwordPrueba =
                "Prueba123*";

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