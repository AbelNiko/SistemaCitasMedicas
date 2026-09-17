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
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * ================================================================
 *                CONTROLLER - MIS CITAS
 * ================================================================
 *
 * Gestiona la consulta, navegación, reprogramación y cancelación
 * de las citas médicas pertenecientes al paciente autenticado.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.1
 */
public class MisCitasController {

    @FXML
    private Label lblNombreUsuario;

    @FXML
    private Label lblCantidadCitas;

    @FXML
    private Label lblMensaje;

    @FXML
    private VBox contenedorCitas;

    private final PacienteDAO pacienteDAO;
    private final CitaService citaService;

    private Usuario usuarioActual;

    private final DateTimeFormatter formatoFecha =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final DateTimeFormatter formatoHora =
            DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Constructor del controlador.
     */
    public MisCitasController() {

        this.pacienteDAO =
                new PacienteDAO();

        this.citaService =
                new CitaService();
    }

    /**
     * Recibe el usuario autenticado y carga
     * automáticamente sus citas.
     *
     * @param usuario usuario autenticado.
     */
    public void setUsuario(Usuario usuario) {

        this.usuarioActual =
                usuario;

        if (usuarioActual != null) {

            lblNombreUsuario.setText(
                    usuarioActual.getNombreCompleto()
            );

            cargarCitas();
        }
    }

    /**
     * Consulta las citas pertenecientes al
     * paciente autenticado.
     */
    private void cargarCitas() {

        contenedorCitas
                .getChildren()
                .clear();

        limpiarMensaje();

        if (usuarioActual == null) {

            mostrarError(
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

                mostrarError(
                        "No se encontró el perfil de paciente."
                );

                lblCantidadCitas.setText("0");

                return;
            }

            List<Cita> citas =
                    citaService.listarPorPaciente(
                            paciente.getIdPaciente()
                    );

            lblCantidadCitas.setText(
                    String.valueOf(citas.size())
            );

            if (citas.isEmpty()) {

                mostrarMensaje(
                        "Todavía no tienes citas médicas registradas."
                );

                return;
            }

            for (Cita cita : citas) {

                contenedorCitas
                        .getChildren()
                        .add(
                                crearTarjetaCita(cita)
                        );
            }

        } catch (Exception e) {

            mostrarError(
                    "No fue posible consultar tus citas."
            );

            System.err.println(
                    "Error al consultar citas: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }
    }

    /**
     * Construye visualmente una tarjeta
     * correspondiente a una cita.
     *
     * @param cita cita que será representada.
     * @return tarjeta JavaFX.
     */
    private VBox crearTarjetaCita(Cita cita) {

        VBox tarjeta =
                new VBox(10);

        tarjeta.getStyleClass().add(
                "dashboard-card"
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
         * FECHA, HORARIO Y ESTADO
         * ========================================================
         */

        HBox encabezado =
                new HBox(12);

        Label fecha =
                new Label(
                        cita.getFechaCita()
                                .format(formatoFecha)
                                + " · "
                                + cita.getHoraInicio()
                                .format(formatoHora)
                                + " - "
                                + cita.getHoraFin()
                                .format(formatoHora)
                );

        fecha.getStyleClass().add(
                "dashboard-section-title"
        );

        Region espacio =
                new Region();

        HBox.setHgrow(
                espacio,
                Priority.ALWAYS
        );

        Label estado =
                new Label(
                        cita.getEstado()
                );

        estado.getStyleClass().add(
                "card-accent"
        );

        encabezado.getChildren().addAll(
                fecha,
                espacio,
                estado
        );

        /*
         * ========================================================
         * ESPECIALIDAD
         * ========================================================
         */

        Label especialidad =
                new Label(
                        cita.getNombreEspecialidad()
                );

        especialidad.getStyleClass().add(
                "card-value"
        );

        /*
         * ========================================================
         * MÉDICO
         * ========================================================
         */

        Label medico =
                new Label(
                        "Médico: "
                                + cita.getNombreMedico()
                );

        medico.getStyleClass().add(
                "card-description"
        );

        /*
         * ========================================================
         * ESTABLECIMIENTO
         * ========================================================
         */

        Label establecimiento =
                new Label(
                        "Establecimiento: "
                                + cita.getNombreEstablecimiento()
                );

        establecimiento.setWrapText(true);

        establecimiento.getStyleClass().add(
                "card-description"
        );

        /*
         * ========================================================
         * MOTIVO DE CONSULTA
         * ========================================================
         */

        String motivoTexto =
                cita.getMotivoConsulta() == null
                        || cita.getMotivoConsulta().isBlank()
                        ? "Sin motivo especificado"
                        : cita.getMotivoConsulta();

        Label motivo =
                new Label(
                        "Motivo: "
                                + motivoTexto
                );

        motivo.setWrapText(true);

        motivo.getStyleClass().add(
                "card-description"
        );

        tarjeta.getChildren().addAll(
                encabezado,
                especialidad,
                medico,
                establecimiento,
                motivo
        );

        /*
         * ========================================================
         * MOTIVO DE CANCELACIÓN
         * ========================================================
         */

        if (
                "CANCELADA".equalsIgnoreCase(
                        cita.getEstado()
                )
                        && cita.getMotivoCancelacion() != null
                        && !cita.getMotivoCancelacion().isBlank()
        ) {

            Label motivoCancelacion =
                    new Label(
                            "Motivo de cancelación: "
                                    + cita.getMotivoCancelacion()
                    );

            motivoCancelacion.setWrapText(true);

            motivoCancelacion.getStyleClass().add(
                    "card-description"
            );

            tarjeta.getChildren().add(
                    motivoCancelacion
            );
        }

        /*
         * ========================================================
         * ACCIONES DE LA CITA
         * ========================================================
         *
         * Únicamente las citas PROGRAMADAS o CONFIRMADAS
         * pueden reprogramarse o cancelarse.
         */

        boolean puedeGestionar =
                "PROGRAMADA".equalsIgnoreCase(
                        cita.getEstado()
                )
                        || "CONFIRMADA".equalsIgnoreCase(
                        cita.getEstado()
                );

        if (puedeGestionar) {

            HBox acciones =
                    new HBox(12);

            /*
             * BOTÓN REPROGRAMAR
             */

            Button btnReprogramar =
                    new Button(
                            "Reprogramar"
                    );

            btnReprogramar.getStyleClass().add(
                    "primary-button"
            );

            btnReprogramar.setOnAction(
                    event -> abrirReprogramacion(cita)
            );

            /*
             * BOTÓN CANCELAR
             */

            Button btnCancelar =
                    new Button(
                            "Cancelar cita"
                    );

            btnCancelar.getStyleClass().add(
                    "cancel-appointment-button"
            );

            btnCancelar.setOnAction(
                    event -> cancelarCita(cita)
            );

            acciones.getChildren().addAll(
                    btnReprogramar,
                    btnCancelar
            );

            tarjeta.getChildren().add(
                    acciones
            );
        }

        return tarjeta;
    }

    /**
     * Abre la pantalla de reprogramación enviando
     * el usuario autenticado y la cita seleccionada.
     *
     * @param cita cita que se desea reprogramar.
     */
    private void abrirReprogramacion(
            Cita cita
    ) {

        if (usuarioActual == null || cita == null) {
            return;
        }

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/reprogramar-cita.fxml"
                            )
                    );

            Scene scene =
                    new Scene(
                            loader.load()
                    );

            ReprogramarCitaController controller =
                    loader.getController();

            controller.configurar(
                    usuarioActual,
                    cita
            );

            cambiarEscena(
                    scene,
                    "MediAppoint - Reprogramar cita"
            );

        } catch (IOException e) {

            mostrarError(
                    "No fue posible abrir la reprogramación de la cita."
            );

            System.err.println(
                    "Error al abrir Reprogramar cita: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }
    }

    /**
     * Solicita confirmación y un motivo antes
     * de cancelar una cita médica.
     *
     * @param cita cita que se desea cancelar.
     */
    private void cancelarCita(Cita cita) {

        if (cita == null) {
            return;
        }

        /*
         * ========================================================
         * PRIMERA CONFIRMACIÓN
         * ========================================================
         */

        Alert confirmacion =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        confirmacion.setTitle(
                "Cancelar cita"
        );

        confirmacion.setHeaderText(
                "¿Deseas cancelar esta cita médica?"
        );

        confirmacion.setContentText(
                cita.getFechaCita()
                        .format(formatoFecha)
                        + " a las "
                        + cita.getHoraInicio()
                        .format(formatoHora)
                        + "\n"
                        + cita.getNombreEspecialidad()
                        + " - "
                        + cita.getNombreMedico()
        );

        Optional<ButtonType> respuesta =
                confirmacion.showAndWait();

        if (
                respuesta.isEmpty()
                        || respuesta.get() != ButtonType.OK
        ) {

            return;
        }

        /*
         * ========================================================
         * MOTIVO DE CANCELACIÓN
         * ========================================================
         */

        TextInputDialog dialogoMotivo =
                new TextInputDialog();

        dialogoMotivo.setTitle(
                "Motivo de cancelación"
        );

        dialogoMotivo.setHeaderText(
                "Indica el motivo de la cancelación"
        );

        dialogoMotivo.setContentText(
                "Motivo:"
        );

        Optional<String> resultado =
                dialogoMotivo.showAndWait();

        if (resultado.isEmpty()) {
            return;
        }

        String motivo =
                resultado
                        .get()
                        .trim();

        if (motivo.isEmpty()) {

            mostrarError(
                    "Debe ingresar un motivo de cancelación."
            );

            return;
        }

        if (motivo.length() > 255) {

            mostrarError(
                    "El motivo de cancelación no puede "
                            + "superar los 255 caracteres."
            );

            return;
        }

        /*
         * ========================================================
         * ACTUALIZACIÓN EN MYSQL
         * ========================================================
         */

        try {

            Paciente paciente =
                    pacienteDAO.buscarPorIdUsuario(
                            usuarioActual.getIdUsuario()
                    );

            if (paciente == null) {

                mostrarError(
                        "No se encontró el perfil del paciente."
                );

                return;
            }

            citaService.cancelarCita(
                    cita.getIdCita(),
                    paciente.getIdPaciente(),
                    motivo
            );

            cargarCitas();

            mostrarExito(
                    "La cita fue cancelada correctamente."
            );

        } catch (IllegalArgumentException e) {

            mostrarError(
                    e.getMessage()
            );

        } catch (Exception e) {

            mostrarError(
                    "No fue posible cancelar la cita."
            );

            System.err.println(
                    "Error al cancelar cita: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }
    }

    /**
     * Muestra un mensaje informativo.
     */
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

    /**
     * Muestra un mensaje de error.
     */
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

    /**
     * Muestra un mensaje de operación exitosa.
     */
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

    /**
     * Limpia los mensajes mostrados.
     */
    private void limpiarMensaje() {

        lblMensaje.setText("");
    }

    /**
     * Regresa al Dashboard conservando
     * el usuario autenticado.
     */
    @FXML
    private void volverDashboard() {

        if (usuarioActual == null) {
            return;
        }

        try {

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

            DashboardController controller =
                    loader.getController();

            controller.setUsuario(
                    usuarioActual
            );

            cambiarEscena(
                    scene,
                    "MediAppoint - Panel principal"
            );

        } catch (IOException e) {

            mostrarError(
                    "No fue posible regresar al panel principal."
            );

            e.printStackTrace();
        }
    }

    /**
     * Abre la pantalla de agendamiento conservando
     * el usuario autenticado.
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

            cambiarEscena(
                    scene,
                    "MediAppoint - Agendar cita"
            );

        } catch (IOException e) {

            mostrarError(
                    "No fue posible abrir el agendamiento."
            );

            e.printStackTrace();
        }
    }

    /**
     * Recarga la pantalla actual de Mis citas.
     */
    @FXML
    private void abrirMisCitas() {

        cargarCitas();
    }

    /**
     * Abre la pantalla Mi perfil conservando
     * el usuario actualmente autenticado.
     */
    @FXML
    private void abrirPerfil() {

        if (usuarioActual == null) {
            return;
        }

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/perfil.fxml"
                            )
                    );

            Scene scene =
                    new Scene(
                            loader.load()
                    );

            PerfilController controller =
                    loader.getController();

            controller.setUsuario(
                    usuarioActual
            );

            cambiarEscena(
                    scene,
                    "MediAppoint - Mi perfil"
            );

        } catch (IOException e) {

            mostrarError(
                    "No fue posible abrir Mi perfil."
            );

            System.err.println(
                    "Error al abrir Mi perfil: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }
    }

    /**
     * Cierra la sesión del usuario autenticado
     * y regresa a la pantalla de inicio de sesión.
     */
    @FXML
    private void cerrarSesion() {

        try {

            Stage stage =
                    (Stage) lblNombreUsuario
                            .getScene()
                            .getWindow();

            usuarioActual = null;

            SesionUtil.cerrarSesion(
                    stage
            );

        } catch (IOException e) {

            System.err.println(
                    "Error al cerrar sesión desde Mis citas: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }
    }

    /**
     * Cambia de pantalla conservando
     * la ventana principal.
     *
     * @param scene nueva escena.
     * @param titulo título de la ventana.
     */
    private void cambiarEscena(
            Scene scene,
            String titulo
    ) {

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

        stage.setMinWidth(950);
        stage.setMinHeight(620);
        stage.centerOnScreen();
    }
}