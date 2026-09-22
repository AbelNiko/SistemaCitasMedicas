package com.citasmedicas.controller;

import com.citasmedicas.dao.PacienteDAO;
import com.citasmedicas.model.Cita;
import com.citasmedicas.model.Paciente;
import com.citasmedicas.model.Usuario;
import com.citasmedicas.service.CitaService;
import com.citasmedicas.util.SesionUtil;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * ================================================================
 *              CONTROLLER - HISTORIAL DEL PACIENTE
 * ================================================================
 *
 * Gestiona la consulta del historial de atenciones médicas
 * pertenecientes al paciente autenticado.
 *
 * El historial muestra únicamente citas con estado ATENDIDA.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public class HistorialPacienteController {

    @FXML
    private Label lblNombreUsuario;

    @FXML
    private Label lblCantidadAtenciones;

    @FXML
    private Label lblMensaje;

    @FXML
    private VBox contenedorHistorial;

    private final PacienteDAO pacienteDAO;
    private final CitaService citaService;

    private Usuario usuarioActual;

    private final DateTimeFormatter formatoFecha =
            DateTimeFormatter.ofPattern(
                    "dd MMM yyyy",
                    new Locale("es", "EC")
            );

    private final DateTimeFormatter formatoHora =
            DateTimeFormatter.ofPattern(
                    "HH:mm"
            );

    /**
     * Constructor principal.
     */
    public HistorialPacienteController() {

        this.pacienteDAO =
                new PacienteDAO();

        this.citaService =
                new CitaService();
    }

    /**
     * Recibe el usuario autenticado y carga
     * automáticamente su historial de atenciones.
     *
     * @param usuario usuario autenticado.
     */
    public void setUsuario(
            Usuario usuario
    ) {

        this.usuarioActual =
                usuario;

        if (usuarioActual == null) {

            mostrarMensaje(
                    "No existe un usuario autenticado."
            );

            return;
        }

        lblNombreUsuario.setText(
                usuarioActual.getNombreCompleto()
        );

        cargarHistorial();
    }

    /**
     * Consulta el perfil del paciente autenticado
     * y recupera sus atenciones realizadas.
     */
    private void cargarHistorial() {

        contenedorHistorial
                .getChildren()
                .clear();

        limpiarMensaje();

        if (usuarioActual == null) {

            lblCantidadAtenciones.setText(
                    "0"
            );

            mostrarMensaje(
                    "No existe un usuario autenticado."
            );

            return;
        }

        try {

            Paciente paciente =
                    pacienteDAO.buscarPorIdUsuario(
                            usuarioActual.getIdUsuario()
                    );

            if (paciente == null) {

                lblCantidadAtenciones.setText(
                        "0"
                );

                mostrarMensaje(
                        "No se encontró el perfil del paciente."
                );

                return;
            }

            List<Cita> historial =
                    citaService.listarHistorialPaciente(
                            paciente.getIdPaciente()
                    );

            mostrarHistorial(
                    historial
            );

        } catch (SQLException e) {

            lblCantidadAtenciones.setText(
                    "0"
            );

            mostrarMensaje(
                    "No fue posible cargar el historial de atenciones."
            );

            System.err.println(
                    "Error al cargar historial del paciente: "
                            + e.getMessage()
            );
        }
    }

    /**
     * Muestra las atenciones recuperadas desde
     * la capa de servicio.
     *
     * @param historial atenciones realizadas.
     */
    private void mostrarHistorial(
            List<Cita> historial
    ) {

        contenedorHistorial
                .getChildren()
                .clear();

        limpiarMensaje();

        int cantidad =
                historial == null
                        ? 0
                        : historial.size();

        lblCantidadAtenciones.setText(
                String.valueOf(cantidad)
        );

        if (
                historial == null
                        || historial.isEmpty()
        ) {

            mostrarMensaje(
                    "Todavía no existen atenciones médicas registradas."
            );

            return;
        }

        for (Cita cita : historial) {

            contenedorHistorial
                    .getChildren()
                    .add(
                            crearTarjetaHistorial(
                                    cita
                            )
                    );
        }
    }

    /**
     * Construye la tarjeta visual correspondiente
     * a una atención médica realizada.
     *
     * @param cita cita atendida.
     * @return tarjeta visual.
     */
    private VBox crearTarjetaHistorial(
            Cita cita
    ) {

        VBox tarjeta =
                new VBox(14);

        tarjeta.getStyleClass().add(
                "history-card"
        );

        tarjeta.setPadding(
                new Insets(
                        18,
                        20,
                        18,
                        20
                )
        );

        /*
         * ========================================================
         * ENCABEZADO
         * ========================================================
         */

        HBox encabezado =
                new HBox(12);

        encabezado.setAlignment(
                Pos.CENTER_LEFT
        );

        VBox fechaHora =
                new VBox(3);

        Label lblFecha =
                new Label(
                        cita.getFechaCita() == null
                                ? "FECHA NO DISPONIBLE"
                                : cita.getFechaCita()
                                        .format(formatoFecha)
                                        .toUpperCase()
                );

        lblFecha.getStyleClass().add(
                "history-date"
        );

        Label lblHora =
                new Label(
                        obtenerHorario(
                                cita
                        )
                );

        lblHora.getStyleClass().add(
                "history-time"
        );

        fechaHora.getChildren().addAll(
                lblFecha,
                lblHora
        );

        Region espacio =
                new Region();

        HBox.setHgrow(
                espacio,
                Priority.ALWAYS
        );

        Label lblEstado =
                new Label(
                        "ATENDIDA"
                );

        lblEstado.getStyleClass().addAll(
                "appointment-status",
                "status-atendida"
        );

        encabezado.getChildren().addAll(
                fechaHora,
                espacio,
                lblEstado
        );

        /*
         * ========================================================
         * ESPECIALIDAD
         * ========================================================
         */

        Label lblEspecialidad =
                new Label(
                        valorSeguro(
                                cita.getNombreEspecialidad()
                        )
                );

        lblEspecialidad.getStyleClass().add(
                "history-specialty"
        );

        /*
         * ========================================================
         * MÉDICO Y ESTABLECIMIENTO
         * ========================================================
         */

        VBox bloqueMedico =
                crearBloqueInformacion(
                        "MÉDICO",
                        cita.getNombreMedico()
                );

        VBox bloqueEstablecimiento =
                crearBloqueInformacion(
                        "ESTABLECIMIENTO",
                        cita.getNombreEstablecimiento()
                );

        HBox informacion =
                new HBox(30);

        informacion.setAlignment(
                Pos.CENTER_LEFT
        );

        HBox.setHgrow(
                bloqueEstablecimiento,
                Priority.ALWAYS
        );

        informacion.getChildren().addAll(
                bloqueMedico,
                bloqueEstablecimiento
        );

        /*
         * ========================================================
         * MOTIVO DE CONSULTA
         * ========================================================
         */

        VBox bloqueMotivo =
                crearBloqueInformacion(
                        "MOTIVO DE CONSULTA",
                        cita.getMotivoConsulta()
                );

        /*
         * ========================================================
         * OBSERVACIÓN DE LA ATENCIÓN
         * ========================================================
         */

        VBox bloqueObservacion =
                crearBloqueInformacion(
                        "OBSERVACIÓN DE ATENCIÓN",
                        cita.getObservacion()
                );

        bloqueObservacion.getStyleClass().add(
                "history-observation"
        );

        tarjeta.getChildren().addAll(
                encabezado,
                lblEspecialidad,
                informacion,
                bloqueMotivo,
                bloqueObservacion
        );

        return tarjeta;
    }

    /**
     * Construye un bloque de información.
     */
    private VBox crearBloqueInformacion(
            String titulo,
            String valor
    ) {

        VBox bloque =
                new VBox(4);

        Label lblTitulo =
                new Label(
                        titulo
                );

        lblTitulo.getStyleClass().add(
                "history-info-label"
        );

        Label lblValor =
                new Label(
                        valorSeguro(
                                valor
                        )
                );

        lblValor.getStyleClass().add(
                "history-info-value"
        );

        lblValor.setWrapText(
                true
        );

        bloque.getChildren().addAll(
                lblTitulo,
                lblValor
        );

        return bloque;
    }

    /**
     * Construye el horario de la atención.
     */
    private String obtenerHorario(
            Cita cita
    ) {

        if (
                cita.getHoraInicio() == null
                        || cita.getHoraFin() == null
        ) {

            return "Horario no disponible";
        }

        return cita.getHoraInicio()
                .format(formatoHora)
                + " — "
                + cita.getHoraFin()
                        .format(formatoHora);
    }

    /**
     * Evita mostrar valores nulos o vacíos.
     */
    private String valorSeguro(
            String valor
    ) {

        if (
                valor == null
                        || valor.isBlank()
        ) {

            return "No especificado";
        }

        return valor;
    }

    /**
     * Regresa al panel principal del paciente.
     */
    @FXML
    private void abrirInicio() {

        cambiarPantalla(
                "/fxml/dashboard.fxml",
                "MediAppoint - Panel principal"
        );
    }

    /**
     * Abre la pantalla para agendar una cita.
     */
    @FXML
    private void abrirAgendarCita() {

        cambiarPantalla(
                "/fxml/agendar-cita.fxml",
                "MediAppoint - Agendar cita"
        );
    }

    /**
     * Abre la pantalla Mis citas.
     */
    @FXML
    private void abrirMisCitas() {

        cambiarPantalla(
                "/fxml/mis-citas.fxml",
                "MediAppoint - Mis citas"
        );
    }

    /**
     * Recarga el historial actual.
     */
    @FXML
    private void abrirHistorial() {

        cargarHistorial();
    }

    /**
     * Abre el perfil del paciente.
     */
    @FXML
    private void abrirPerfil() {

        cambiarPantalla(
                "/fxml/perfil.fxml",
                "MediAppoint - Mi perfil"
        );
    }

    /**
     * Gestiona la navegación entre las pantallas
     * del paciente conservando el usuario autenticado.
     *
     * @param ruta ruta del archivo FXML.
     * @param titulo título de la ventana.
     */
    private void cambiarPantalla(
            String ruta,
            String titulo
    ) {

        if (usuarioActual == null) {

            mostrarMensaje(
                    "No existe un usuario autenticado."
            );

            return;
        }

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    ruta
                            )
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

            } else if (
                    controller
                            instanceof PerfilController perfil
            ) {

                perfil.setUsuario(
                        usuarioActual
                );

            } else if (
                    controller
                            instanceof HistorialPacienteController historial
            ) {

                historial.setUsuario(
                        usuarioActual
                );
            }

            Stage stage =
                    (Stage) lblNombreUsuario
                            .getScene()
                            .getWindow();

            stage.setScene(
                    scene
            );

            stage.setTitle(
                    titulo
            );

            stage.setMinWidth(
                    950
            );

            stage.setMinHeight(
                    620
            );

            stage.centerOnScreen();

        } catch (IOException e) {

            mostrarMensaje(
                    "No fue posible abrir la pantalla seleccionada."
            );

            System.err.println(
                    "Error al cambiar de pantalla: "
                            + e.getMessage()
            );
        }
    }

    /**
     * Cierra la sesión actual.
     */
    @FXML
    private void cerrarSesion() {

        try {

            Stage stage =
                    (Stage) lblNombreUsuario
                            .getScene()
                            .getWindow();

            SesionUtil.cerrarSesion(
                    stage
            );

        } catch (Exception e) {

            mostrarMensaje(
                    "No fue posible cerrar la sesión."
            );

            System.err.println(
                    "Error al cerrar sesión: "
                            + e.getMessage()
            );
        }
    }

    /**
     * Muestra un mensaje informativo.
     */
    private void mostrarMensaje(
            String mensaje
    ) {

        if (lblMensaje != null) {

            lblMensaje.setText(
                    mensaje
            );

            lblMensaje.setVisible(
                    true
            );

            lblMensaje.setManaged(
                    true
            );
        }
    }

    /**
     * Oculta el mensaje actual.
     */
    private void limpiarMensaje() {

        if (lblMensaje != null) {

            lblMensaje.setText("");

            lblMensaje.setVisible(
                    false
            );

            lblMensaje.setManaged(
                    false
            );
        }
    }
}