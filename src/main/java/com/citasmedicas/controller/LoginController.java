package com.citasmedicas.controller;

import com.citasmedicas.model.Usuario;
import com.citasmedicas.service.AutenticacionService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * ================================================================
 *                CONTROLADOR - INICIO DE SESIÓN
 * ================================================================
 *
 * Controlador encargado de gestionar los eventos de la
 * pantalla de inicio de sesión.
 *
 * Valida las credenciales mediante AutenticacionService
 * y, cuando el acceso es correcto, carga la pantalla principal.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public class LoginController {

    @FXML
    private TextField txtCorreo;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Label lblMensaje;

    private final AutenticacionService autenticacionService;

    /**
     * Constructor principal.
     */
    public LoginController() {

        this.autenticacionService =
                new AutenticacionService();
    }

    /**
     * Gestiona el intento de inicio de sesión.
     */
    @FXML
    private void iniciarSesion() {

        lblMensaje.setText("");

        try {

            String correo =
                    txtCorreo.getText();

            String password =
                    txtPassword.getText();

            Usuario usuario =
                    autenticacionService.autenticar(
                            correo,
                            password
                    );

            if (usuario == null) {

                lblMensaje.setText(
                        "Correo o contraseña incorrectos."
                );

                txtPassword.clear();

                return;
            }

            System.out.println(
                    "Usuario autenticado: "
                    + usuario.getNombreCompleto()
            );

            System.out.println(
                    "Rol: "
                    + usuario.getRol().getNombre()
            );

            abrirPantallaPrincipal(usuario);

        } catch (IllegalArgumentException e) {

            lblMensaje.setText(
                    e.getMessage()
            );

        } catch (IOException e) {

            lblMensaje.setText(
                    "No fue posible cargar la pantalla principal."
            );

            System.err.println(
                    "Error al cargar dashboard.fxml: "
                    + e.getMessage()
            );

        } catch (Exception e) {

            lblMensaje.setText(
                    "No fue posible iniciar sesión."
            );

            System.err.println(
                    "Error de autenticación: "
                    + e.getMessage()
            );
        }
    }

    /**
     * Abre la pantalla principal después de una
     * autenticación exitosa.
     *
     * @param usuario usuario autenticado.
     * @throws IOException si ocurre un error al cargar el FXML.
     */
    private void abrirPantallaPrincipal(
            Usuario usuario
    ) throws IOException {

        FXMLLoader loader =
                new FXMLLoader(
                        getClass().getResource(
                                "/fxml/dashboard.fxml"
                        )
                );

        Scene scene =
                new Scene(
                        loader.load()
                );

        DashboardController dashboardController =
                loader.getController();

        dashboardController.setUsuario(
                usuario
        );

        Stage stage =
                (Stage) txtCorreo
                        .getScene()
                        .getWindow();

        stage.setScene(scene);

        stage.setTitle(
                "Sistema de Gestión de Citas Médicas"
        );

        stage.setResizable(true);

        stage.setMinWidth(900);

        stage.setMinHeight(600);

        stage.centerOnScreen();
    }
    /**
 * Abre la pantalla de registro de pacientes.
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

        Scene scene =
                new Scene(
                        loader.load()
                );

        Stage stage =
                (Stage) txtCorreo
                        .getScene()
                        .getWindow();

        stage.setScene(scene);

        stage.setTitle(
                "MediAppoint - Registro de paciente"
        );

        stage.setWidth(600);
        stage.setHeight(720);

        stage.setMinWidth(550);
        stage.setMinHeight(650);

        stage.centerOnScreen();

    } catch (IOException e) {

        lblMensaje.setText(
                "No fue posible abrir el registro."
        );

        System.err.println(
                "Error al cargar registro.fxml: "
                + e.getMessage()
        );

        e.printStackTrace();
    }
}
}