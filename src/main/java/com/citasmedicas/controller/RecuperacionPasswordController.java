package com.citasmedicas.controller;

import com.citasmedicas.model.PreguntaSeguridad;
import com.citasmedicas.model.Usuario;
import com.citasmedicas.service.RecuperacionPasswordService;

import javafx.concurrent.Task;

import javafx.fxml.FXML;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import javafx.scene.layout.VBox;

import javafx.stage.Stage;

/**
 * ================================================================
 *       CONTROLLER - RECUPERACIÓN DE CONTRASEÑA
 * ================================================================
 *
 * Gestiona las tres etapas del proceso:
 *
 * 1. Identificación de la cuenta.
 * 2. Verificación de la pregunta de seguridad.
 * 3. Creación de una nueva contraseña.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public class RecuperacionPasswordController {

    /*
     * ============================================================
     * ETAPA 1 - IDENTIFICACIÓN
     * ============================================================
     */

    @FXML
    private VBox panelIdentificacion;

    @FXML
    private TextField txtCorreoRecuperacion;

    @FXML
    private TextField txtCedulaRecuperacion;

    @FXML
    private Button btnContinuar;

    /*
     * ============================================================
     * ETAPA 2 - PREGUNTA
     * ============================================================
     */

    @FXML
    private VBox panelPregunta;

    @FXML
    private Label lblPreguntaSeguridad;

    @FXML
    private TextField txtRespuestaSeguridad;

    @FXML
    private Button btnVerificarRespuesta;

    /*
     * ============================================================
     * ETAPA 3 - NUEVA CONTRASEÑA
     * ============================================================
     */

    @FXML
    private VBox panelNuevaPassword;

    @FXML
    private PasswordField txtNuevaPassword;

    @FXML
    private PasswordField txtConfirmarNuevaPassword;

    @FXML
    private Button btnRestablecer;

    /*
     * ============================================================
     * MENSAJES
     * ============================================================
     */

    @FXML
    private Label lblMensaje;

    private final RecuperacionPasswordService recuperacionService;

    private Usuario usuarioRecuperacion;

    public RecuperacionPasswordController() {

        recuperacionService =
                new RecuperacionPasswordService();
    }

    /**
     * Estado inicial.
     */
    @FXML
    private void initialize() {

        mostrarPanel(
                panelIdentificacion
        );
    }

    /**
     * Verifica correo y cédula.
     */
    @FXML
    private void identificarCuenta() {

        limpiarMensaje();

        String correo =
                txtCorreoRecuperacion.getText();

        String cedula =
                txtCedulaRecuperacion.getText();

        btnContinuar.setDisable(
                true
        );

        btnContinuar.setText(
                "VERIFICANDO..."
        );

        Task<Usuario> tarea =
                new Task<>() {

                    @Override
                    protected Usuario call()
                            throws Exception {

                        return recuperacionService
                                .identificarCuenta(
                                        correo,
                                        cedula
                                );
                    }
                };

        tarea.setOnSucceeded(
                evento -> {

                    restaurarBotonContinuar();

                    usuarioRecuperacion =
                            tarea.getValue();

                    PreguntaSeguridad pregunta =
                            recuperacionService
                                    .obtenerPregunta(
                                            usuarioRecuperacion
                                    );

                    lblPreguntaSeguridad.setText(
                            pregunta.getTexto()
                    );

                    mostrarPanel(
                            panelPregunta
                    );

                    mostrarInformacion(
                            "Cuenta verificada. Responde la pregunta de seguridad."
                    );
                }
        );

        tarea.setOnFailed(
                evento -> {

                    restaurarBotonContinuar();

                    procesarError(
                            tarea.getException()
                    );
                }
        );

        ejecutarTarea(
                tarea,
                "identificar-cuenta-recuperacion"
        );
    }

    /**
     * Comprueba la respuesta.
     */
    @FXML
    private void verificarRespuesta() {

        limpiarMensaje();

        String respuesta =
                txtRespuestaSeguridad.getText();

        btnVerificarRespuesta.setDisable(
                true
        );

        btnVerificarRespuesta.setText(
                "VERIFICANDO..."
        );

        Task<Void> tarea =
                new Task<>() {

                    @Override
                    protected Void call()
                            throws Exception {

                        recuperacionService
                                .verificarRespuesta(
                                        usuarioRecuperacion,
                                        respuesta
                                );

                        return null;
                    }
                };

        tarea.setOnSucceeded(
                evento -> {

                    btnVerificarRespuesta.setDisable(
                            false
                    );

                    btnVerificarRespuesta.setText(
                            "VERIFICAR RESPUESTA"
                    );

                    txtRespuestaSeguridad.clear();

                    mostrarPanel(
                            panelNuevaPassword
                    );

                    mostrarExito(
                            "Identidad verificada. Ahora establece una nueva contraseña."
                    );
                }
        );

        tarea.setOnFailed(
                evento -> {

                    btnVerificarRespuesta.setDisable(
                            false
                    );

                    btnVerificarRespuesta.setText(
                            "VERIFICAR RESPUESTA"
                    );

                    txtRespuestaSeguridad.clear();

                    procesarError(
                            tarea.getException()
                    );
                }
        );

        ejecutarTarea(
                tarea,
                "verificar-respuesta-recuperacion"
        );
    }

    /**
     * Guarda la nueva contraseña.
     */
    @FXML
    private void restablecerPassword() {

        limpiarMensaje();

        String nuevaPassword =
                txtNuevaPassword.getText();

        String confirmarPassword =
                txtConfirmarNuevaPassword.getText();

        btnRestablecer.setDisable(
                true
        );

        btnRestablecer.setText(
                "RESTABLECIENDO..."
        );

        Task<Void> tarea =
                new Task<>() {

                    @Override
                    protected Void call()
                            throws Exception {

                        recuperacionService
                                .restablecerPassword(
                                        usuarioRecuperacion,
                                        nuevaPassword,
                                        confirmarPassword
                                );

                        return null;
                    }
                };

        tarea.setOnSucceeded(
                evento -> {

                    btnRestablecer.setDisable(
                            false
                    );

                    btnRestablecer.setText(
                            "RESTABLECER CONTRASEÑA"
                    );

                    txtNuevaPassword.clear();
                    txtConfirmarNuevaPassword.clear();

                    mostrarExito(
                            "Contraseña restablecida correctamente. "
                                    + "Ya puedes iniciar sesión."
                    );

                    /*
                     * Cerramos después de mostrar el resultado.
                     */
                    btnRestablecer.setDisable(
                            true
                    );
                }
        );

        tarea.setOnFailed(
                evento -> {

                    btnRestablecer.setDisable(
                            false
                    );

                    btnRestablecer.setText(
                            "RESTABLECER CONTRASEÑA"
                    );

                    procesarError(
                            tarea.getException()
                    );
                }
        );

        ejecutarTarea(
                tarea,
                "restablecer-password"
        );
    }

    /**
     * Muestra una sola etapa a la vez.
     */
    private void mostrarPanel(
            VBox panelVisible
    ) {

        VBox[] paneles = {
                panelIdentificacion,
                panelPregunta,
                panelNuevaPassword
        };

        for (VBox panel : paneles) {

            boolean visible =
                    panel == panelVisible;

            panel.setVisible(
                    visible
            );

            panel.setManaged(
                    visible
            );
        }
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

    private void restaurarBotonContinuar() {

        btnContinuar.setDisable(
                false
        );

        btnContinuar.setText(
                "CONTINUAR"
        );
    }

    private void procesarError(
            Throwable error
    ) {

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
                "No fue posible completar la recuperación."
        );

        if (error != null) {

            System.err.println(
                    "Error durante recuperación: "
                            + error.getMessage()
            );

            error.printStackTrace();
        }
    }

    private void ejecutarTarea(
            Task<?> tarea,
            String nombre
    ) {

        Thread hilo =
                new Thread(
                        tarea,
                        nombre
                );

        hilo.setDaemon(
                true
        );

        hilo.start();
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