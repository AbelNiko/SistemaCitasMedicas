package com.citasmedicas.controller;

import com.citasmedicas.model.Usuario;
import com.citasmedicas.service.AutenticacionService;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * ================================================================
 *                CONTROLADOR - INICIO DE SESIÓN
 * ================================================================
 *
 * Controlador encargado de gestionar los eventos de la
 * pantalla de inicio de sesión.
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

                return;
            }

            lblMensaje.setText(
                    "Bienvenido, "
                    + usuario.getNombreCompleto()
            );

            System.out.println(
                    "Usuario autenticado: "
                    + usuario.getNombreCompleto()
            );

            System.out.println(
                    "Rol: "
                    + usuario.getRol().getNombre()
            );

        } catch (IllegalArgumentException e) {

            lblMensaje.setText(
                    e.getMessage()
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
}