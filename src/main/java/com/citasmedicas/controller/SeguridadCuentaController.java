package com.citasmedicas.controller;

import com.citasmedicas.model.PreguntaSeguridad;
import com.citasmedicas.model.Usuario;
import com.citasmedicas.service.SeguridadCuentaService;

import javafx.concurrent.Task;

import javafx.fxml.FXML;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import javafx.stage.Stage;

/**
 * ================================================================
 *          CONTROLLER - SEGURIDAD DE LA CUENTA
 * ================================================================
 *
 * Permite configurar o cambiar la pregunta de seguridad
 * del usuario autenticado.
 *
 * Esta pantalla será reutilizada por pacientes y médicos.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public class SeguridadCuentaController {

    @FXML
    private Label lblEstadoSeguridad;

    @FXML
    private Label lblPreguntaActual;

    @FXML
    private ComboBox<PreguntaSeguridad> cmbPreguntaSeguridad;

    @FXML
    private TextField txtRespuestaSeguridad;

    @FXML
    private PasswordField txtPasswordActual;

    @FXML
    private Label lblMensaje;

    @FXML
    private Button btnGuardarSeguridad;

    private Usuario usuarioActual;

    private final SeguridadCuentaService seguridadService;

    public SeguridadCuentaController() {

        seguridadService =
                new SeguridadCuentaService();
    }

    /**
     * Configura las preguntas disponibles.
     */
    @FXML
    private void initialize() {

        cmbPreguntaSeguridad
                .getItems()
                .setAll(
                        PreguntaSeguridad.values()
                );
    }

    /**
     * Recibe el usuario cuya seguridad será gestionada.
     */
    public void setUsuario(
            Usuario usuario
    ) {

        this.usuarioActual =
                usuario;

        actualizarEstadoVisual();
    }

    /**
     * Actualiza la información visible según
     * la configuración actual del usuario.
     */
    private void actualizarEstadoVisual() {

        if (usuarioActual == null) {

            lblEstadoSeguridad.setText(
                    "Usuario no disponible"
            );

            return;
        }

        if (
                usuarioActual
                        .tienePreguntaSeguridadConfigurada()
        ) {

            lblEstadoSeguridad.setText(
                    "SEGURIDAD CONFIGURADA"
            );

            lblEstadoSeguridad.setStyle(
                    "-fx-text-fill: #45D483;"
            );

            PreguntaSeguridad pregunta =
                    PreguntaSeguridad.desdeCodigo(
                            usuarioActual.getPreguntaSeguridad()
                    );

            if (pregunta != null) {

                lblPreguntaActual.setText(
                        pregunta.getTexto()
                );

            } else {

                lblPreguntaActual.setText(
                        "Pregunta configurada"
                );
            }

            btnGuardarSeguridad.setText(
                    "ACTUALIZAR SEGURIDAD"
            );

        } else {

            lblEstadoSeguridad.setText(
                    "SEGURIDAD NO CONFIGURADA"
            );

            lblEstadoSeguridad.setStyle(
                    "-fx-text-fill: #FFC269;"
            );

            lblPreguntaActual.setText(
                    "Aún no has configurado una pregunta de seguridad."
            );

            btnGuardarSeguridad.setText(
                    "CONFIGURAR SEGURIDAD"
            );
        }
    }

    /**
     * Guarda la configuración seleccionada.
     */
    @FXML
    private void guardarSeguridad() {

        limpiarMensaje();

        if (usuarioActual == null) {

            mostrarError(
                    "No existe un usuario autenticado."
            );

            return;
        }

        String passwordActual =
                txtPasswordActual.getText();

        PreguntaSeguridad pregunta =
                cmbPreguntaSeguridad.getValue();

        String respuesta =
                txtRespuestaSeguridad.getText();

        btnGuardarSeguridad.setDisable(
                true
        );

        btnGuardarSeguridad.setText(
                "GUARDANDO..."
        );

        mostrarInformacion(
                "Verificando información..."
        );

        Task<Void> tarea =
                new Task<>() {

                    @Override
                    protected Void call()
                            throws Exception {

                        seguridadService
                                .configurarPreguntaSeguridad(
                                        usuarioActual,
                                        passwordActual,
                                        pregunta,
                                        respuesta
                                );

                        return null;
                    }
                };

        tarea.setOnSucceeded(
                evento -> {

                    btnGuardarSeguridad.setDisable(
                            false
                    );

                    txtPasswordActual.clear();
                    txtRespuestaSeguridad.clear();

                    cmbPreguntaSeguridad.setValue(
                            null
                    );

                    actualizarEstadoVisual();

                    mostrarExito(
                            "La seguridad de tu cuenta fue actualizada correctamente."
                    );
                }
        );

        tarea.setOnFailed(
                evento -> {

                    btnGuardarSeguridad.setDisable(
                            false
                    );

                    Throwable error =
                            tarea.getException();

                    actualizarEstadoVisual();

                    if (
                            error
                                    instanceof IllegalArgumentException
                    ) {

                        mostrarError(
                                error.getMessage()
                        );

                        return;
                    }

                    mostrarError(
                            "No fue posible actualizar la seguridad de la cuenta."
                    );

                    if (error != null) {

                        System.err.println(
                                "Error al actualizar seguridad: "
                                        + error.getMessage()
                        );

                        error.printStackTrace();
                    }
                }
        );

        Thread hilo =
                new Thread(
                        tarea,
                        "seguridad-cuenta"
                );

        hilo.setDaemon(
                true
        );

        hilo.start();
    }

    /**
     * Cierra la ventana.
     */
    @FXML
    private void cerrarVentana() {

        Stage stage =
                (Stage) lblMensaje
                        .getScene()
                        .getWindow();

        stage.close();
    }

    private void mostrarError(
            String mensaje
    ) {

        lblMensaje.setStyle(
                "-fx-text-fill: #FF7B7B;"
        );

        lblMensaje.setText(
                mensaje
        );
    }

    private void mostrarInformacion(
            String mensaje
    ) {

        lblMensaje.setStyle(
                "-fx-text-fill: #67BFFF;"
        );

        lblMensaje.setText(
                mensaje
        );
    }

    private void mostrarExito(
            String mensaje
    ) {

        lblMensaje.setStyle(
                "-fx-text-fill: #45D483;"
        );

        lblMensaje.setText(
                mensaje
        );
    }

    private void limpiarMensaje() {

        lblMensaje.setText(
                ""
        );
    }
}