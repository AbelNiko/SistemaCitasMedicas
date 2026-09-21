package com.citasmedicas.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * ================================================================
 *            PRUEBA DE VISTA - REGISTRO PACIENTE
 * ================================================================
 *
 * Clase temporal utilizada para comprobar que la vista
 * registro.fxml puede cargarse correctamente mediante JavaFX.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public class PruebaRegistroView extends Application {

    @Override
    public void start(Stage stage) {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/registro.fxml"
                            )
                    );

            Parent root =
                    loader.load();

            Scene scene =
                    new Scene(
                            root,
                            600,
                            720
                    );

            stage.setTitle(
                    "MediAppoint - Registro de paciente"
            );

            stage.setScene(scene);

            stage.setMinWidth(550);
            stage.setMinHeight(650);

            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {

            System.err.println(
                    "No fue posible cargar registro.fxml."
            );

            System.err.println(
                    "Detalle: " + e.getMessage()
            );

            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        launch(args);
    }
}