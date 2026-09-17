package com.citasmedicas.util;

import com.citasmedicas.util.SesionUtil;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * ================================================================
 *                    UTILIDAD - SESIÓN
 * ================================================================
 *
 * Centraliza el regreso a la pantalla de inicio de sesión.
 *
 * No almacena información del usuario ni contiene lógica
 * relacionada con la base de datos.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public final class SesionUtil {

    /**
     * Evita crear instancias de esta clase.
     */
    private SesionUtil() {
    }

    /**
     * Regresa a la pantalla de inicio de sesión.
     *
     * @param stage ventana principal de la aplicación.
     * @throws IOException si login.fxml no puede cargarse.
     */
    public static void cerrarSesion(
            Stage stage
    ) throws IOException {

        if (stage == null) {
            throw new IllegalArgumentException(
                    "La ventana de la aplicación no es válida."
            );
        }

        FXMLLoader loader =
                new FXMLLoader(
                        SesionUtil.class.getResource(
                                "/fxml/login.fxml"
                        )
                );

        Scene scene =
                new Scene(
                        loader.load()
                );

        stage.setScene(scene);

        stage.setTitle(
                "Sistema de Gestión de Citas Médicas"
        );

        stage.setMinWidth(950);
        stage.setMinHeight(620);

        stage.centerOnScreen();
    }
}