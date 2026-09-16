package com.citasmedicas.controller;

import com.citasmedicas.model.Paciente;
import com.citasmedicas.model.Usuario;
import com.citasmedicas.service.PerfilService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * ================================================================
 *                  CONTROLLER - MI PERFIL
 * ================================================================
 *
 * Gestiona la consulta y actualización de la información
 * personal del paciente autenticado.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public class PerfilController {

    @FXML
    private Label lblNombreUsuario;

    @FXML
    private Label lblMensaje;

    @FXML
    private TextField txtNombres;

    @FXML
    private TextField txtApellidos;

    @FXML
    private TextField txtCedula;

    @FXML
    private TextField txtCorreo;

    @FXML
    private TextField txtTelefono;

    @FXML
    private TextField txtDireccion;

    private Usuario usuarioActual;
    private Paciente pacienteActual;

    private final PerfilService perfilService;

    public PerfilController() {

        this.perfilService =
                new PerfilService();
    }

    /**
     * Recibe el usuario autenticado y carga
     * la información completa del perfil.
     */
    public void setUsuario(
            Usuario usuario
    ) {

        this.usuarioActual =
                usuario;

        cargarDatos();
    }

    /**
     * Consulta y muestra los datos actuales.
     */
    private void cargarDatos() {

        if (usuarioActual == null) {
            return;
        }

        limpiarMensaje();

        try {

            pacienteActual =
                    perfilService.obtenerPaciente(
                            usuarioActual.getIdUsuario()
                    );

            lblNombreUsuario.setText(
                    usuarioActual.getNombreCompleto()
            );

            txtNombres.setText(
                    valorSeguro(
                            usuarioActual.getNombres()
                    )
            );

            txtApellidos.setText(
                    valorSeguro(
                            usuarioActual.getApellidos()
                    )
            );

            txtCedula.setText(
                    valorSeguro(
                            usuarioActual.getCedula()
                    )
            );

            txtCorreo.setText(
                    valorSeguro(
                            usuarioActual.getCorreo()
                    )
            );

            txtTelefono.setText(
                    valorSeguro(
                            usuarioActual.getTelefono()
                    )
            );

            if (pacienteActual != null) {

                txtDireccion.setText(
                        valorSeguro(
                                pacienteActual.getDireccion()
                        )
                );

            } else {

                txtDireccion.setText("");

                mostrarError(
                        "No se encontró el perfil de paciente."
                );
            }

        } catch (Exception e) {

            mostrarError(
                    "No fue posible cargar la información del perfil."
            );

            e.printStackTrace();
        }
    }

    /**
     * Descarta los cambios realizados en pantalla
     * y vuelve a cargar la información actual.
     */
    @FXML
    private void recargarDatos() {

        cargarDatos();

        mostrarMensaje(
                "Los cambios fueron descartados."
        );
    }

    /**
     * Guarda los cambios del perfil.
     */
    @FXML
    private void guardarCambios() {

        if (
                usuarioActual == null
                || pacienteActual == null
        ) {

            mostrarError(
                    "No fue posible identificar el perfil."
            );

            return;
        }

        try {

            perfilService.actualizarPerfil(
                    usuarioActual,
                    pacienteActual.getIdPaciente(),
                    txtNombres.getText(),
                    txtApellidos.getText(),
                    txtCorreo.getText(),
                    txtTelefono.getText(),
                    txtDireccion.getText()
            );

            /*
             * Volvemos a consultar el paciente para
             * mantener sincronizados los datos.
             */
            pacienteActual =
                    perfilService.obtenerPaciente(
                            usuarioActual.getIdUsuario()
                    );

            /*
             * Actualizamos el nombre mostrado en
             * la cabecera inmediatamente.
             */
            lblNombreUsuario.setText(
                    usuarioActual.getNombreCompleto()
            );

            /*
             * Normalizamos visualmente los campos.
             */
            txtNombres.setText(
                    valorSeguro(
                            usuarioActual.getNombres()
                    )
            );

            txtApellidos.setText(
                    valorSeguro(
                            usuarioActual.getApellidos()
                    )
            );

            txtCorreo.setText(
                    valorSeguro(
                            usuarioActual.getCorreo()
                    )
            );

            txtTelefono.setText(
                    valorSeguro(
                            usuarioActual.getTelefono()
                    )
            );

            if (pacienteActual != null) {

                txtDireccion.setText(
                        valorSeguro(
                                pacienteActual.getDireccion()
                        )
                );
            }

            mostrarExito(
                    "Perfil actualizado correctamente."
            );

        } catch (IllegalArgumentException e) {

            mostrarError(
                    e.getMessage()
            );

        } catch (Exception e) {

            mostrarError(
                    "No fue posible actualizar el perfil."
            );

            System.err.println(
                    "Error al actualizar perfil: "
                    + e.getMessage()
            );

            e.printStackTrace();
        }
    }

    /**
     * Regresa al Dashboard.
     */
    @FXML
    private void volverDashboard() {

        cambiarPantalla(
                "/fxml/dashboard.fxml",
                "MediAppoint - Panel principal"
        );
    }

    /**
     * Abre Agendar cita.
     */
    @FXML
    private void abrirAgendarCita() {

        cambiarPantalla(
                "/fxml/agendar-cita.fxml",
                "MediAppoint - Agendar cita"
        );
    }

    /**
     * Abre Mis citas.
     */
    @FXML
    private void abrirMisCitas() {

        cambiarPantalla(
                "/fxml/mis-citas.fxml",
                "MediAppoint - Mis citas"
        );
    }

    /**
     * Gestiona la navegación conservando
     * el usuario autenticado.
     */
    private void cambiarPantalla(
            String ruta,
            String titulo
    ) {

        if (usuarioActual == null) {
            return;
        }

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(ruta)
                    );

            Scene scene =
                    new Scene(
                            loader.load()
                    );

            Object controller =
                    loader.getController();

            if (
                    controller
                    instanceof DashboardController dashboard
            ) {

                dashboard.setUsuario(
                        usuarioActual
                );

            } else if (
                    controller
                    instanceof AgendarCitaController agendar
            ) {

                agendar.setUsuario(
                        usuarioActual
                );

            } else if (
                    controller
                    instanceof MisCitasController misCitas
            ) {

                misCitas.setUsuario(
                        usuarioActual
                );
            }

            Stage stage =
                    (Stage) lblNombreUsuario
                            .getScene()
                            .getWindow();

            stage.setScene(scene);
            stage.setTitle(titulo);

            stage.setMinWidth(950);
            stage.setMinHeight(620);

            stage.centerOnScreen();

        } catch (IOException e) {

            mostrarError(
                    "No fue posible abrir la pantalla."
            );

            e.printStackTrace();
        }
    }

    private void mostrarMensaje(
            String mensaje
    ) {

        lblMensaje.setText(
                mensaje
        );

        lblMensaje.setStyle(
                "-fx-text-fill: #91A4B8;"
        );
    }

    private void mostrarError(
            String mensaje
    ) {

        lblMensaje.setText(
                mensaje
        );

        lblMensaje.setStyle(
                "-fx-text-fill: #FF7B7B;"
        );
    }

    private void mostrarExito(
            String mensaje
    ) {

        lblMensaje.setText(
                mensaje
        );

        lblMensaje.setStyle(
                "-fx-text-fill: #45D483;"
        );
    }

    private void limpiarMensaje() {

        lblMensaje.setText("");
    }

    /**
     * Evita mostrar valores null.
     */
    private String valorSeguro(
            String valor
    ) {

        return valor == null
                ? ""
                : valor;
    }
}