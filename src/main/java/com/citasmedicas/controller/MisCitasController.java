package com.citasmedicas.controller;

import com.citasmedicas.dao.PacienteDAO;
import com.citasmedicas.model.Cita;
import com.citasmedicas.model.Paciente;
import com.citasmedicas.model.Usuario;
import com.citasmedicas.service.CitaService;

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
 * Gestiona la consulta y cancelación de las citas médicas
 * pertenecientes al paciente autenticado.
 *
 * Permite visualizar la información de cada cita y cancelar
 * aquellas que se encuentren en estado PROGRAMADA o CONFIRMADA.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
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

            /*
             * Obtenemos el perfil de paciente relacionado
             * con el usuario que inició sesión.
             */
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

            /*
             * Consultamos todas las citas del paciente.
             */
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

            /*
             * Construimos una tarjeta por cada cita.
             */
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

        /*
         * Añadimos inicialmente la información
         * principal de la cita.
         */
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
         *
         * Si la cita ya fue cancelada mostramos también
         * el motivo registrado.
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
         * BOTÓN CANCELAR
         * ========================================================
         *
         * Solamente las citas PROGRAMADAS o CONFIRMADAS
         * pueden ser canceladas.
         */

        boolean puedeCancelar =
                "PROGRAMADA".equalsIgnoreCase(
                        cita.getEstado()
                )
                || "CONFIRMADA".equalsIgnoreCase(
                        cita.getEstado()
                );

        if (puedeCancelar) {

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

            tarjeta.getChildren().add(
                    btnCancelar
            );
        }

        return tarjeta;
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

        /*
         * Si el paciente cierra la ventana o selecciona
         * Cancelar, no realizamos ninguna modificación.
         */
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

        /*
         * Si el usuario cierra el diálogo,
         * se cancela la operación.
         */
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

            /*
             * El Service valida que:
             *
             * - La cita sea válida.
             * - Pertenezca al paciente.
             * - Su estado permita cancelación.
             */
            citaService.cancelarCita(
                    cita.getIdCita(),
                    paciente.getIdPaciente(),
                    motivo
            );

            /*
             * Volvemos a consultar MySQL para actualizar
             * inmediatamente todas las tarjetas.
             */
            cargarCitas();

            /*
             * Este mensaje se coloca después de cargarCitas()
             * para evitar que limpiarMensaje() lo elimine.
             */
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