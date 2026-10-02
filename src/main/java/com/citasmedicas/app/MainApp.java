package com.citasmedicas.app;

import com.citasmedicas.util.EjecutorTareas;
import com.citasmedicas.util.ConexionBD;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * ================================================================
 *             SISTEMA DE GESTIÓN DE CITAS MÉDICAS
 * ================================================================
 *
 * Clase principal de la aplicación JavaFX.
 *
 * Su responsabilidad es iniciar la aplicación y cargar
 * la pantalla inicial de autenticación.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public class MainApp extends Application {

    @Override
    public void start(Stage stage)
            throws IOException {

        FXMLLoader loader =
                new FXMLLoader(
                        MainApp.class.getResource(
                                "/fxml/login.fxml"
                        )
                );

        Scene scene =
                new Scene(
                        loader.load()
                );

        stage.setTitle(
                "Sistema de Gestión de Citas Médicas"
        );

        stage.setScene(scene);

        stage.setResizable(false);

        stage.centerOnScreen();

        stage.show();
    }

    /**
 * Libera los recursos compartidos cuando
 * MediAppoint termina.
 */
/**
 * Libera los recursos compartidos cuando
 * MediAppoint termina.
 */
@Override
public void stop() {

    /*
     * Primero dejamos de aceptar nuevas tareas.
     */
    EjecutorTareas.cerrar();

    /*
     * Después liberamos las conexiones JDBC.
     */
    ConexionBD.cerrarPool();
}

    public static void main(String[] args) {
        launch(args);
    }
}