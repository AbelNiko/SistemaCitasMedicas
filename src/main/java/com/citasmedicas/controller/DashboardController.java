package com.citasmedicas.controller;

import com.citasmedicas.model.Usuario;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * ================================================================
 *             CONTROLADOR - PANTALLA PRINCIPAL
 * ================================================================
 *
 * Controlador de la pantalla principal del sistema.
 *
 * Recibe la información del usuario autenticado y muestra
 * su nombre y rol dentro de la aplicación.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public class DashboardController {

    @FXML
    private Label lblNombreUsuario;

    @FXML
    private Label lblRolUsuario;

    private Usuario usuarioActual;

    /**
     * Recibe el usuario autenticado desde LoginController.
     *
     * @param usuario usuario que inició sesión.
     */
    public void setUsuario(Usuario usuario) {

        this.usuarioActual = usuario;

        actualizarInformacionUsuario();
    }

    /**
     * Actualiza los datos visibles del usuario.
     */
    private void actualizarInformacionUsuario() {

        if (usuarioActual == null) {
            return;
        }

        lblNombreUsuario.setText(
                usuarioActual.getNombreCompleto()
        );

        lblRolUsuario.setText(
                usuarioActual.getRol().getNombre()
        );
    }
}