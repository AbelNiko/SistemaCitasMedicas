package com.citasmedicas.app;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * ================================================================
 *             SISTEMA DE GESTIÓN DE CITAS MÉDICAS
 * ================================================================
 *
 * Clase principal de la aplicación.
 *
 * Su responsabilidad es iniciar JavaFX y mostrar
 * la ventana principal del sistema.
 *
 * En futuras etapas esta clase cargará las vistas
 * desarrolladas mediante archivos FXML.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public class MainApp extends Application {

    /**
     * Ancho inicial de la ventana.
     */
    private static final double ANCHO_VENTANA = 1000;

    /**
     * Alto inicial de la ventana.
     */
    private static final double ALTO_VENTANA = 650;

    /**
     * Método ejecutado automáticamente por JavaFX
     * cuando inicia la aplicación.
     *
     * @param stage ventana principal del sistema.
     */
    @Override
    public void start(Stage stage) {

        // ========================================================
        // TÍTULO PRINCIPAL
        // ========================================================

        Label lblTitulo = new Label(
                "Sistema de Gestión y Agendamiento de Citas Médicas"
        );

        // ========================================================
        // CONTENEDOR PRINCIPAL
        // ========================================================

        VBox contenedorPrincipal = new VBox();

        contenedorPrincipal.setAlignment(Pos.CENTER);
        contenedorPrincipal.setSpacing(20);

        contenedorPrincipal.getChildren().add(lblTitulo);

        // ========================================================
        // ESCENA PRINCIPAL
        // ========================================================

        Scene escenaPrincipal = new Scene(
                contenedorPrincipal,
                ANCHO_VENTANA,
                ALTO_VENTANA
        );

        // ========================================================
        // CONFIGURACIÓN DE LA VENTANA
        // ========================================================

        stage.setTitle(
                "Sistema de Citas Médicas"
        );

        stage.setScene(
                escenaPrincipal
        );

        stage.setMinWidth(800);
        stage.setMinHeight(500);

        stage.show();
    }

    /**
     * Punto de entrada tradicional de la aplicación.
     *
     * @param args argumentos recibidos por línea de comandos.
     */
    public static void main(String[] args) {

        launch(args);
    }
}