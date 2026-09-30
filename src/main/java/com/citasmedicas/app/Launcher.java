package com.citasmedicas.app;

/**
 * ================================================================
 *                    LAUNCHER - MEDIAPPOINT
 * ================================================================
 *
 * Punto de entrada utilizado por la versión ejecutable
 * de MediAppoint.
 *
 * Esta clase no hereda de JavaFX Application.
 * Su única responsabilidad es iniciar MainApp.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public final class Launcher {

    /**
     * Constructor privado.
     */
    private Launcher() {
    }

    /**
     * Inicia la aplicación MediAppoint.
     *
     * @param args argumentos recibidos por el ejecutable.
     */
    public static void main(String[] args) {

        MainApp.main(args);
    }
}