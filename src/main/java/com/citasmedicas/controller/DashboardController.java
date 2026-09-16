package com.citasmedicas.controller;

import com.citasmedicas.dao.PacienteDAO;
import com.citasmedicas.model.Cita;
import com.citasmedicas.model.Paciente;
import com.citasmedicas.model.Usuario;
import com.citasmedicas.service.CitaService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * ================================================================
 *              CONTROLADOR - PANTALLA PRINCIPAL
 * ================================================================
 *
 * Gestiona el panel principal del paciente.
 *
 * Muestra la información del usuario autenticado,
 * su próxima cita y el número de citas programadas.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public class DashboardController {

    @FXML
    private Label lblBienvenida;

    @FXML
    private Label lblNombreUsuario;

    @FXML
    private Label lblRolUsuario;

    @FXML
    private Label lblProximaCita;

    @FXML
    private Label lblDetalleProximaCita;

    @FXML
    private Label lblCantidadCitas;

    private Usuario usuarioActual;

    private final PacienteDAO pacienteDAO;
    private final CitaService citaService;

    private final DateTimeFormatter formatoFecha =
            DateTimeFormatter.ofPattern(
                    "dd/MM/yyyy"
            );

    private final DateTimeFormatter formatoHora =
            DateTimeFormatter.ofPattern(
                    "HH:mm"
            );

    public DashboardController() {

        this.pacienteDAO =
                new PacienteDAO();

        this.citaService =
                new CitaService();
    }

    /**
     * Recibe el usuario autenticado.
     */
    public void setUsuario(Usuario usuario) {

        this.usuarioActual =
                usuario;

        actualizarInformacionUsuario();

        cargarResumenCitas();
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

        if (usuarioActual.getRol() != null) {

            lblRolUsuario.setText(
                    usuarioActual
                            .getRol()
                            .getNombre()
            );

        } else {

            lblRolUsuario.setText(
                    "SIN ROL"
            );
        }

        lblBienvenida.setText(
                "Bienvenido, "
                + usuarioActual.getNombres()
        );
    }

    /**
     * Consulta MySQL y actualiza las tarjetas
     * del Dashboard.
     */
    private void cargarResumenCitas() {

        if (usuarioActual == null) {
            return;
        }

        try {

            Paciente paciente =
                    pacienteDAO.buscarPorIdUsuario(
                            usuarioActual
                                    .getIdUsuario()
                    );

            if (paciente == null) {

                lblProximaCita.setText(
                        "Sin información"
                );

                lblDetalleProximaCita.setText(
                        "No existe un perfil de paciente."
                );

                lblCantidadCitas.setText(
                        "0"
                );

                return;
            }

            long cantidad =
                    citaService
                            .contarCitasProgramadas(
                                    paciente.getIdPaciente()
                            );

            lblCantidadCitas.setText(
                    String.valueOf(cantidad)
            );

            Optional<Cita> proxima =
                    citaService.obtenerProximaCita(
                            paciente.getIdPaciente()
                    );

            if (proxima.isPresent()) {

                Cita cita =
                        proxima.get();

                lblProximaCita.setText(
                        cita.getFechaCita()
                                .format(formatoFecha)
                        + " · "
                        + cita.getHoraInicio()
                                .format(formatoHora)
                );

                lblDetalleProximaCita.setText(
                        cita.getNombreEspecialidad()
                        + " · "
                        + cita.getNombreMedico()
                );

            } else {

                lblProximaCita.setText(
                        "Sin citas próximas"
                );

                lblDetalleProximaCita.setText(
                        "Agenda una nueva cita médica"
                );
            }

        } catch (Exception e) {

            lblProximaCita.setText(
                    "No disponible"
            );

            lblDetalleProximaCita.setText(
                    "No fue posible consultar tus citas."
            );

            lblCantidadCitas.setText(
                    "-"
            );

            System.err.println(
                    "Error al cargar resumen de citas: "
                    + e.getMessage()
            );

            e.printStackTrace();
        }
    }

    /**
     * Abre la pantalla para agendar una cita.
     */
    @FXML
    private void abrirAgendarCita() {

        if (usuarioActual == null) {
            return;
        }

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/agendar-cita.fxml"
                            )
                    );

            Scene scene =
                    new Scene(
                            loader.load()
                    );

            AgendarCitaController controller =
                    loader.getController();

            controller.setUsuario(
                    usuarioActual
            );

            Stage stage =
                    (Stage) lblNombreUsuario
                            .getScene()
                            .getWindow();

            stage.setScene(scene);

            stage.setTitle(
                    "MediAppoint - Agendar cita"
            );

            stage.setMinWidth(950);
            stage.setMinHeight(620);

            stage.centerOnScreen();

        } catch (IOException e) {

            System.err.println(
                    "Error al cargar la pantalla "
                    + "de agendamiento: "
                    + e.getMessage()
            );

            e.printStackTrace();
        }
    }
    /**
 * Abre la pantalla que muestra las citas
 * del paciente autenticado.
 */
@FXML
private void abrirMisCitas() {

    if (usuarioActual == null) {
        return;
    }

    try {

        FXMLLoader loader =
                new FXMLLoader(
                        getClass().getResource(
                                "/fxml/mis-citas.fxml"
                        )
                );

        Scene scene =
                new Scene(
                        loader.load()
                );

        MisCitasController controller =
                loader.getController();

        controller.setUsuario(
                usuarioActual
        );

        Stage stage =
                (Stage) lblNombreUsuario
                        .getScene()
                        .getWindow();

        stage.setScene(scene);

        stage.setTitle(
                "MediAppoint - Mis citas"
        );

        stage.setMinWidth(950);
        stage.setMinHeight(620);

        stage.centerOnScreen();

    } catch (IOException e) {

        System.err.println(
                "Error al abrir Mis citas: "
                + e.getMessage()
        );

        e.printStackTrace();
    }
}
}