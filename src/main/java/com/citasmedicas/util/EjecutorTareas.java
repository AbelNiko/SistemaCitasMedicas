package com.citasmedicas.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * ================================================================
 *                 UTIL - EJECUTOR DE TAREAS
 * ================================================================
 *
 * Administra los hilos utilizados por MediAppoint para operaciones
 * que no deben ejecutarse directamente sobre el JavaFX
 * Application Thread.
 *
 * Principalmente:
 *
 * - consultas a MySQL;
 * - operaciones de servicios;
 * - procesos de autenticación;
 * - carga de citas;
 * - actualización de información.
 *
 * Se utiliza un número reducido de hilos porque HikariCP mantiene
 * también un número controlado de conexiones disponibles.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public final class EjecutorTareas {

    private static final int NUMERO_HILOS =
            4;

    private static final AtomicInteger CONTADOR_HILOS =
            new AtomicInteger(
                    1
            );

    /**
     * Todos los hilos se crean como daemon.
     *
     * Esto evita que un hilo secundario impida cerrar
     * completamente MediAppoint.
     */
    private static final ThreadFactory FABRICA_HILOS =
            tarea -> {

                Thread hilo =
                        new Thread(
                                tarea
                        );

                hilo.setName(
                        "mediappoint-worker-"
                                + CONTADOR_HILOS
                                        .getAndIncrement()
                );

                hilo.setDaemon(
                        true
                );

                return hilo;
            };

    /**
     * Pool compartido por la aplicación.
     */
    private static final ExecutorService EJECUTOR =
            Executors.newFixedThreadPool(
                    NUMERO_HILOS,
                    FABRICA_HILOS
            );

    /**
     * Clase utilitaria.
     */
    private EjecutorTareas() {
    }

    /**
     * Ejecuta una tarea en segundo plano.
     *
     * Puede recibir directamente un javafx.concurrent.Task
     * porque Task implementa Runnable.
     */
    public static void ejecutar(
            Runnable tarea
    ) {

        if (tarea == null) {

            throw new IllegalArgumentException(
                    "La tarea no puede ser nula."
            );
        }

        EJECUTOR.execute(
                tarea
        );
    }

    /**
     * Cierra el ejecutor al terminar MediAppoint.
     */
    public static void cerrar() {

        EJECUTOR.shutdown();
    }
}