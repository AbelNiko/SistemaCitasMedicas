package com.citasmedicas.controller;

import com.citasmedicas.model.Paciente;
import com.citasmedicas.service.RegistroPacienteService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * ================================================================
 *             CONTROLADOR - REGISTRO DE PACIENTE
 * ================================================================
 *
 * Gestiona la pantalla utilizada para crear nuevas cuentas
 * de pacientes.
 *
 * El registro público está limitado al rol PACIENTE.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public class RegistroController {

    @FXML
    private TextField txtNombres;

    @FXML
    private TextField txtApellidos;

    @FXML
    private TextField txtCedula;

    @FXML
    private DatePicker dpFechaNacimiento;

    @FXML
    private ComboBox<String> cmbSexo;

    @FXML
    private TextField txtDireccion;

    @FXML
    private TextField txtTelefono;

    @FXML
    private TextField txtCorreo;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private PasswordField txtConfirmarPassword;

    @FXML
    private TextField txtContactoEmergencia;

    @FXML
    private TextField txtTelefonoEmergencia;

    @FXML
    private Label lblMensaje;

    private final RegistroPacienteService registroService;

    /**
     * Constructor principal.
     */
    public RegistroController() {

        registroService =
                new RegistroPacienteService();
    }

    /**
     * Configura los controles cuando se carga la vista.
     */
    @FXML
    private void initialize() {

        cmbSexo.getItems().addAll(
                "Femenino",
                "Masculino",
                "Otro",
                "Prefiero no indicar"
        );
    }

    /**
     * Procesa el formulario de registro.
     */
    @FXML
    private void registrarPaciente() {

        lblMensaje.setText("");

        try {

            Paciente paciente =
                    registroService.registrar(
                            txtNombres.getText(),
                            txtApellidos.getText(),
                            txtCedula.getText(),
                            txtCorreo.getText(),
                            txtTelefono.getText(),
                            txtPassword.getText(),
                            txtConfirmarPassword.getText(),
                            dpFechaNacimiento.getValue(),
                            txtDireccion.getText(),
                            cmbSexo.getValue(),
                            txtContactoEmergencia.getText(),
                            txtTelefonoEmergencia.getText()
                    );

            lblMensaje.setStyle(
                    "-fx-text-fill: #37d67a;"
            );

            lblMensaje.setText(
                    "Cuenta creada correctamente. "
                    + "Bienvenido, "
                    + paciente.getUsuario().getNombreCompleto()
                    + "."
            );

            limpiarFormulario();

        } catch (IllegalArgumentException e) {

            mostrarError(
                    e.getMessage()
            );

        } catch (Exception e) {

            mostrarError(
                    "No fue posible crear la cuenta."
            );

            System.err.println(
                    "Error durante el registro: "
                    + e.getMessage()
            );
        }
    }

    /**
     * Regresa a la pantalla de inicio de sesión.
     */
    @FXML
    private void volverLogin() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/login.fxml"
                            )
                    );

            Parent root =
                    loader.load();

            Stage stage =
                    (Stage) lblMensaje
                            .getScene()
                            .getWindow();

            Scene scene =
                    new Scene(root);

            stage.setScene(scene);
            stage.setTitle("MediAppoint - Iniciar sesión");
            stage.centerOnScreen();

        } catch (IOException e) {

            mostrarError(
                    "No fue posible regresar al inicio de sesión."
            );

            System.err.println(
                    "Error cargando login.fxml: "
                    + e.getMessage()
            );
        }
    }

    /**
     * Limpia los datos del formulario después
     * de un registro exitoso.
     */
    private void limpiarFormulario() {

        txtNombres.clear();
        txtApellidos.clear();
        txtCedula.clear();
        dpFechaNacimiento.setValue(null);
        cmbSexo.setValue(null);
        txtDireccion.clear();
        txtTelefono.clear();
        txtCorreo.clear();
        txtPassword.clear();
        txtConfirmarPassword.clear();
        txtContactoEmergencia.clear();
        txtTelefonoEmergencia.clear();
    }

    /**
     * Presenta un mensaje de error.
     *
     * @param mensaje mensaje que será mostrado.
     */
    private void mostrarError(
            String mensaje
    ) {

        lblMensaje.setStyle(
                "-fx-text-fill: #ff6b6b;"
        );

        lblMensaje.setText(
                mensaje
        );
    }
}