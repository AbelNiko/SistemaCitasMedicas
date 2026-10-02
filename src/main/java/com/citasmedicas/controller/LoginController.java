package com.citasmedicas.controller;

import com.citasmedicas.model.Usuario;

import com.citasmedicas.service.AutenticacionService;

import com.citasmedicas.util.EjecutorTareas;

import javafx.concurrent.Task;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * ================================================================
 *                CONTROLADOR - INICIO DE SESIÓN
 * ================================================================
 *
 * Gestiona la autenticación de usuarios de MediAppoint.
 *
 * La consulta a MySQL y la verificación BCrypt se ejecutan
 * fuera del JavaFX Application Thread para evitar bloquear
 * la interfaz mientras Aiven responde.
 *
 * Después de una autenticación correcta, el sistema dirige
 * al usuario al portal correspondiente según su rol.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 2.0
 */
public class LoginController {

    /*
     * ============================================================
     * CAMPOS
     * ============================================================
     */

    @FXML
    private TextField txtCorreo;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Label lblMensaje;

    /*
     * ============================================================
     * BOTONES
     * ============================================================
     */

    @FXML
    private Button btnIniciarSesion;

    @FXML
    private Button btnRecuperarPassword;

    @FXML
    private Button btnRegistrarse;

    /*
     * ============================================================
     * SERVICIOS
     * ============================================================
     */

    private final AutenticacionService autenticacionService;

    /**
     * Constructor principal.
     */
    public LoginController() {

        autenticacionService =
                new AutenticacionService();
    }

    /**
     * Configuración inicial de la pantalla.
     */
    @FXML
    private void initialize() {

        /*
         * Permite iniciar sesión pulsando ENTER
         * desde el campo de contraseña.
         */
        txtPassword.setOnAction(
                event -> iniciarSesion()
        );
    }

    /**
     * ============================================================
     * INICIO DE SESIÓN
     * ============================================================
     *
     * Ejecuta la autenticación fuera del hilo gráfico.
     */
    @FXML
    private void iniciarSesion() {

        /*
         * Evita iniciar más de una autenticación
         * simultáneamente.
         */
        if (
                btnIniciarSesion.isDisable()
        ) {

            return;
        }

        limpiarMensaje();

        /*
         * Copiamos los valores antes de iniciar
         * el proceso en segundo plano.
         *
         * Nunca debemos acceder a controles JavaFX
         * directamente desde el worker.
         */
        String correo =
                txtCorreo
                        .getText();

        String password =
                txtPassword
                        .getText();

        /*
         * Hacemos una validación visual mínima.
         *
         * AutenticacionService seguirá realizando
         * la validación definitiva.
         */
        if (
                correo == null
                        || correo.isBlank()
        ) {

            mostrarError(
                    "Ingresa tu correo electrónico."
            );

            txtCorreo.requestFocus();

            return;
        }

        if (
                password == null
                        || password.isBlank()
        ) {

            mostrarError(
                    "Ingresa tu contraseña."
            );

            txtPassword.requestFocus();

            return;
        }

        establecerAutenticacionEnProceso(
                true
        );

        mostrarInformacion(
                "Verificando credenciales..."
        );

        /*
         * ========================================================
         * TAREA DE AUTENTICACIÓN
         * ========================================================
         */

        Task<Usuario> tarea =
                new Task<>() {

                    @Override
                    protected Usuario call()
                            throws Exception {

                        return autenticacionService
                                .autenticar(
                                        correo,
                                        password
                                );
                    }
                };

        /*
         * ========================================================
         * AUTENTICACIÓN CORRECTA
         * ========================================================
         */

        tarea.setOnSucceeded(
                evento -> {

                    Usuario usuario =
                            tarea.getValue();

                    if (usuario == null) {

                        establecerAutenticacionEnProceso(
                                false
                        );

                        txtPassword.clear();

                        mostrarError(
                                "Correo o contraseña incorrectos."
                        );

                        txtPassword.requestFocus();

                        return;
                    }

                    /*
                     * Limpiamos la contraseña tan pronto como
                     * deja de ser necesaria en la interfaz.
                     */
                    txtPassword.clear();

                    try {

                        /*
                         * Los callbacks de Task se ejecutan en
                         * JavaFX Application Thread, por lo que
                         * aquí sí podemos modificar la escena.
                         */
                        abrirPantallaSegunRol(
                                usuario
                        );

                    } catch (IOException e) {

                        establecerAutenticacionEnProceso(
                                false
                        );

                        mostrarError(
                                "No fue posible cargar la pantalla principal."
                        );

                        System.err.println(
                                "Error cargando pantalla principal: "
                                        + e.getMessage()
                        );

                    } catch (IllegalArgumentException e) {

                        establecerAutenticacionEnProceso(
                                false
                        );

                        mostrarError(
                                obtenerMensajeError(
                                        e,
                                        "El usuario no tiene un rol válido."
                                )
                        );

                    } catch (Exception e) {

                        establecerAutenticacionEnProceso(
                                false
                        );

                        mostrarError(
                                "No fue posible abrir la sesión."
                        );

                        System.err.println(
                                "Error después de la autenticación: "
                                        + e.getMessage()
                        );
                    }
                }
        );

        /*
         * ========================================================
         * ERROR DE AUTENTICACIÓN
         * ========================================================
         */

        tarea.setOnFailed(
                evento -> {

                    establecerAutenticacionEnProceso(
                            false
                    );

                    txtPassword.clear();

                    procesarErrorAutenticacion(
                            tarea.getException()
                    );

                    txtPassword.requestFocus();
                }
        );

        /*
         * Utilizamos el ejecutor compartido de MediAppoint.
         *
         * Ya no creamos un Thread nuevo manualmente.
         */
        EjecutorTareas.ejecutar(
                tarea
        );
    }

    /**
     * Activa o desactiva los controles mientras
     * la autenticación está en proceso.
     */
    private void establecerAutenticacionEnProceso(
            boolean procesando
    ) {

        txtCorreo.setDisable(
                procesando
        );

        txtPassword.setDisable(
                procesando
        );

        btnIniciarSesion.setDisable(
                procesando
        );

        btnRecuperarPassword.setDisable(
                procesando
        );

        btnRegistrarse.setDisable(
                procesando
        );

        btnIniciarSesion.setText(
                procesando
                        ? "INICIANDO SESIÓN..."
                        : "INICIAR SESIÓN"
        );
    }

    /**
     * Centraliza los errores producidos durante
     * la autenticación.
     */
    private void procesarErrorAutenticacion(
            Throwable error
    ) {

        if (
                error
                        instanceof IllegalArgumentException
        ) {

            mostrarError(
                    obtenerMensajeError(
                            error,
                            "Revisa los datos ingresados."
                    )
            );

            return;
        }

        /*
         * No mostramos información técnica de MySQL
         * al usuario.
         */
        mostrarError(
                "No fue posible iniciar sesión. "
                        + "Inténtalo nuevamente."
        );

        if (error != null) {

            System.err.println(
                    "Error durante autenticación: "
                            + error.getMessage()
            );
        }
    }

    /*
     * ============================================================
     * REDIRECCIÓN SEGÚN ROL
     * ============================================================
     */

    /**
     * Determina qué portal debe abrirse después
     * de una autenticación correcta.
     */
    private void abrirPantallaSegunRol(
            Usuario usuario
    ) throws IOException {

        if (
                usuario == null
                        || usuario.getRol() == null
                        || usuario.getRol().getNombre() == null
        ) {

            throw new IllegalArgumentException(
                    "El usuario no tiene un rol válido."
            );
        }

        String nombreRol =
                usuario
                        .getRol()
                        .getNombre()
                        .trim();

        if (
                "MEDICO".equalsIgnoreCase(
                        nombreRol
                )
        ) {

            abrirDashboardMedico(
                    usuario
            );

            return;
        }

        if (
                "PACIENTE".equalsIgnoreCase(
                        nombreRol
                )
        ) {

            abrirDashboardPaciente(
                    usuario
            );

            return;
        }

        throw new IllegalArgumentException(
                "El rol del usuario todavía no tiene "
                        + "una pantalla disponible."
        );
    }

    /**
     * Abre el portal principal del paciente.
     */
    private void abrirDashboardPaciente(
            Usuario usuario
    ) throws IOException {

        FXMLLoader loader =
                new FXMLLoader(
                        getClass().getResource(
                                "/fxml/dashboard.fxml"
                        )
                );

        Parent root =
                loader.load();

        DashboardController controller =
                loader.getController();

        controller.setUsuario(
                usuario
        );

        Stage stage =
                obtenerStage();

        stage.setScene(
                new Scene(
                        root
                )
        );

        stage.setTitle(
                "MediAppoint - Portal del paciente"
        );

        stage.setResizable(
                true
        );

        stage.setMinWidth(
                900
        );

        stage.setMinHeight(
                600
        );

        stage.centerOnScreen();
    }

    /**
     * Abre el portal principal del médico.
     */
    private void abrirDashboardMedico(
            Usuario usuario
    ) throws IOException {

        FXMLLoader loader =
                new FXMLLoader(
                        getClass().getResource(
                                "/fxml/dashboard-medico.fxml"
                        )
                );

        Parent root =
                loader.load();

        DashboardMedicoController controller =
                loader.getController();

        controller.setUsuario(
                usuario
        );

        Stage stage =
                obtenerStage();

        stage.setScene(
                new Scene(
                        root
                )
        );

        stage.setTitle(
                "MediAppoint - Portal médico"
        );

        stage.setResizable(
                true
        );

        stage.setMinWidth(
                1000
        );

        stage.setMinHeight(
                650
        );

        stage.centerOnScreen();
    }

    /*
     * ============================================================
     * REGISTRO
     * ============================================================
     */

    /**
     * Abre la pantalla de creación de cuenta
     * para pacientes.
     */
    @FXML
    private void abrirRegistro() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/registro.fxml"
                            )
                    );

            Parent root =
                    loader.load();

            Stage stage =
                    obtenerStage();

            stage.setScene(
                    new Scene(
                            root
                    )
            );

            stage.setTitle(
                    "MediAppoint - Registro de paciente"
            );

            stage.setWidth(
                    600
            );

            stage.setHeight(
                    720
            );

            stage.setMinWidth(
                    550
            );

            stage.setMinHeight(
                    650
            );

            stage.centerOnScreen();

        } catch (IOException e) {

            mostrarError(
                    "No fue posible abrir el registro."
            );

            System.err.println(
                    "Error cargando registro.fxml: "
                            + e.getMessage()
            );
        }
    }

    /*
     * ============================================================
     * RECUPERACIÓN DE CONTRASEÑA
     * ============================================================
     */

    /**
     * Abre la recuperación de contraseña
     * en una ventana modal.
     */
    @FXML
    private void abrirRecuperacionPassword() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/recuperacion-password.fxml"
                            )
                    );

            Parent root =
                    loader.load();

            Stage ventanaRecuperacion =
                    new Stage();

            ventanaRecuperacion.setTitle(
                    "MediAppoint - Recuperar contraseña"
            );

            ventanaRecuperacion.setScene(
                    new Scene(
                            root
                    )
            );

            ventanaRecuperacion.setResizable(
                    false
            );

            ventanaRecuperacion.initOwner(
                    obtenerStage()
            );

            ventanaRecuperacion.initModality(
                    Modality.WINDOW_MODAL
            );

            ventanaRecuperacion.centerOnScreen();

            ventanaRecuperacion.showAndWait();

            /*
             * Por seguridad limpiamos cualquier contraseña
             * que hubiera quedado escrita en el login.
             */
            txtPassword.clear();

            limpiarMensaje();

        } catch (IOException e) {

            mostrarError(
                    "No fue posible abrir la recuperación "
                            + "de contraseña."
            );

            System.err.println(
                    "Error cargando recuperacion-password.fxml: "
                            + e.getMessage()
            );
        }
    }

    /*
     * ============================================================
     * UTILIDADES DE INTERFAZ
     * ============================================================
     */

    /**
     * Obtiene el Stage actual.
     */
    private Stage obtenerStage() {

        return (Stage)
                txtCorreo
                        .getScene()
                        .getWindow();
    }

    /**
     * Obtiene un mensaje seguro desde una excepción.
     */
    private String obtenerMensajeError(
            Throwable error,
            String predeterminado
    ) {

        if (
                error == null
                        || error.getMessage() == null
                        || error.getMessage().isBlank()
        ) {

            return predeterminado;
        }

        return error.getMessage();
    }

    /**
     * Muestra un mensaje de error.
     */
    private void mostrarError(
            String mensaje
    ) {

        lblMensaje.setStyle(
                "-fx-text-fill: #FF6B6B;"
        );

        lblMensaje.setText(
                mensaje
        );
    }

    /**
     * Muestra información temporal.
     */
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

    /**
     * Limpia mensajes anteriores.
     */
    private void limpiarMensaje() {

        lblMensaje.setText(
                ""
        );
    }
}