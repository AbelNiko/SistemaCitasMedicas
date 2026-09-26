package com.citasmedicas.controller;

import com.citasmedicas.dao.MedicoDAO;
import com.citasmedicas.model.Cita;
import com.citasmedicas.model.Medico;
import com.citasmedicas.model.Usuario;
import com.citasmedicas.service.CitaService;
import com.citasmedicas.util.SesionUtil;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * ================================================================
 *                 CONTROLADOR - DASHBOARD MÉDICO
 * ================================================================
 *
 * Gestiona la pantalla principal del médico autenticado.
 *
 * Permite:
 * - consultar las citas asignadas;
 * - visualizar un resumen de la agenda;
 * - buscar citas;
 * - filtrar por estado y fecha;
 * - confirmar citas programadas;
 * - registrar observaciones de atención;
 * - marcar citas confirmadas como atendidas;
 * - registrar la no asistencia del paciente;
 * - actualizar la agenda;
 * - consultar el perfil profesional;
 * - cerrar la sesión.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.5
 */
public class DashboardMedicoController {

    @FXML
    private Label lblNombreMedico;

    @FXML
    private Label lblCedulaProfesional;

    @FXML
    private Label lblCantidadCitas;

    @FXML
    private Label lblTotalCitas;

    @FXML
    private Label lblProgramadas;

    @FXML
    private Label lblCanceladas;

    @FXML
    private Label lblMensaje;

    @FXML
    private VBox contenedorCitas;

    @FXML
    private TextField txtBuscar;

    @FXML
    private ComboBox<String> cmbEstado;

    @FXML
    private ComboBox<String> cmbFecha;

    private final MedicoDAO medicoDAO;
    private final CitaService citaService;

    private Usuario usuarioActual;
    private Medico medicoActual;

    private List<Cita> citasMedico =
            new ArrayList<>();

    private final DateTimeFormatter formatoFecha =
            DateTimeFormatter.ofPattern(
                    "dd MMM yyyy",
                    new Locale("es", "EC")
            );

    private final DateTimeFormatter formatoHora =
            DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Constructor principal.
     */
    public DashboardMedicoController() {

        this.medicoDAO =
                new MedicoDAO();

        this.citaService =
                new CitaService();
    }

    /**
     * Inicializa los filtros de la agenda.
     */
    @FXML
    private void initialize() {

        configurarFiltros();
    }

    /**
     * Configura los controles utilizados para filtrar.
     */
    private void configurarFiltros() {

        cmbEstado.getItems().setAll(
                "Todos",
                "PROGRAMADA",
                "CONFIRMADA",
                "ATENDIDA",
                "CANCELADA",
                "NO_ASISTIO"
        );

        cmbEstado.setValue("Todos");

        cmbFecha.getItems().setAll(
                "Todas",
                "Hoy",
                "Próximas"
        );

        cmbFecha.setValue("Todas");

        txtBuscar.textProperty().addListener(
                (observable, anterior, nuevo) ->
                        aplicarFiltros()
        );

        cmbEstado.setOnAction(
                event -> aplicarFiltros()
        );

        cmbFecha.setOnAction(
                event -> aplicarFiltros()
        );
    }

    /**
     * Recibe el usuario médico autenticado.
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
                    "No fue posible identificar al usuario."
            );

            return;
        }

        lblNombreMedico.setText(
                usuarioActual.getNombreCompleto()
        );

        cargarPerfilMedico();
    }

    /**
 * Obtiene el perfil médico en segundo plano
 * para no bloquear la interfaz JavaFX.
 */
private void cargarPerfilMedico() {

    if (
            usuarioActual == null
                    || usuarioActual.getIdUsuario() == null
    ) {

        mostrarMensaje(
                "No existe una sesión médica válida."
        );

        return;
    }

    mostrarMensaje(
            "Cargando información médica..."
    );

    Task<Medico> tarea =
            new Task<>() {

                @Override
                protected Medico call()
                        throws Exception {

                    return medicoDAO.buscarPorIdUsuario(
                            usuarioActual.getIdUsuario()
                    );
                }
            };

    tarea.setOnSucceeded(
            evento -> {

                medicoActual =
                        tarea.getValue();

                if (medicoActual == null) {

                    mostrarMensaje(
                            "No se encontró un perfil médico asociado a este usuario."
                    );

                    return;
                }

                lblCedulaProfesional.setText(
                        medicoActual.getCedulaProfesional()
                );

                cargarAgenda();
            }
    );

    tarea.setOnFailed(
            evento -> {

                Throwable error =
                        tarea.getException();

                mostrarMensaje(
                        "No fue posible cargar el perfil médico."
                );

                if (error != null) {

                    System.err.println(
                            "Error al cargar perfil médico: "
                                    + error.getMessage()
                    );

                    error.printStackTrace();
                }
            }
    );

    ejecutarTarea(
            tarea,
            "cargar-perfil-medico"
    );
}

    /**
 * Recupera desde MySQL las citas asignadas
 * al médico sin bloquear la interfaz.
 */
@FXML
private void cargarAgenda() {

    if (medicoActual == null) {
        return;
    }

    mostrarMensaje(
            "Actualizando agenda..."
    );

    int idMedico =
            medicoActual.getIdMedico();

    Task<List<Cita>> tarea =
            new Task<>() {

                @Override
                protected List<Cita> call()
                        throws Exception {

                    return citaService.listarPorMedico(
                            idMedico
                    );
                }
            };

    tarea.setOnSucceeded(
            evento -> {

                List<Cita> resultado =
                        tarea.getValue();

                citasMedico =
                        resultado == null
                                ? new ArrayList<>()
                                : new ArrayList<>(
                                        resultado
                                );

                actualizarResumen();

                aplicarFiltros();
            }
    );

    tarea.setOnFailed(
            evento -> {

                Throwable error =
                        tarea.getException();

                mostrarMensaje(
                        "No fue posible cargar la agenda médica."
                );

                if (error != null) {

                    System.err.println(
                            "Error al cargar agenda médica: "
                                    + error.getMessage()
                    );

                    error.printStackTrace();
                }
            }
    );

    ejecutarTarea(
            tarea,
            "cargar-agenda-medico"
    );
}

    /**
     * Actualiza las tarjetas estadísticas de la agenda.
     */
    private void actualizarResumen() {

        long total =
                citasMedico.size();

        long programadas =
                citasMedico.stream()
                        .filter(
                                cita ->
                                        "PROGRAMADA".equalsIgnoreCase(
                                                cita.getEstado()
                                        )
                        )
                        .count();

        long canceladas =
                citasMedico.stream()
                        .filter(
                                cita ->
                                        "CANCELADA".equalsIgnoreCase(
                                                cita.getEstado()
                                        )
                        )
                        .count();

        lblTotalCitas.setText(
                String.valueOf(total)
        );

        lblProgramadas.setText(
                String.valueOf(programadas)
        );

        lblCanceladas.setText(
                String.valueOf(canceladas)
        );
    }

    /**
     * Aplica simultáneamente los filtros.
     */
    private void aplicarFiltros() {

        if (contenedorCitas == null) {
            return;
        }

        String texto =
                txtBuscar.getText() == null
                        ? ""
                        : txtBuscar.getText()
                                .trim()
                                .toLowerCase(Locale.ROOT);

        String estadoSeleccionado =
                cmbEstado.getValue();

        String fechaSeleccionada =
                cmbFecha.getValue();

        LocalDate hoy =
                LocalDate.now();

        List<Cita> citasFiltradas =
                citasMedico.stream()
                        .filter(
                                cita ->
                                        coincideTexto(
                                                cita,
                                                texto
                                        )
                        )
                        .filter(
                                cita ->
                                        coincideEstado(
                                                cita,
                                                estadoSeleccionado
                                        )
                        )
                        .filter(
                                cita ->
                                        coincideFecha(
                                                cita,
                                                fechaSeleccionada,
                                                hoy
                                        )
                        )
                        .toList();

        mostrarCitas(
                citasFiltradas
        );
    }

    /**
     * Comprueba el filtro de búsqueda.
     */
    private boolean coincideTexto(
            Cita cita,
            String texto
    ) {

        if (texto == null || texto.isBlank()) {
            return true;
        }

        return contieneTexto(
                cita.getNombrePaciente(),
                texto
        )
                || contieneTexto(
                        cita.getNombreEspecialidad(),
                        texto
                )
                || contieneTexto(
                        cita.getNombreEstablecimiento(),
                        texto
                )
                || contieneTexto(
                        cita.getMotivoConsulta(),
                        texto
                )
                || contieneTexto(
                        cita.getObservacion(),
                        texto
                );
    }

    /**
     * Comprueba el filtro de estado.
     */
    private boolean coincideEstado(
            Cita cita,
            String estadoSeleccionado
    ) {

        if (
                estadoSeleccionado == null
                        || "Todos".equalsIgnoreCase(
                                estadoSeleccionado
                        )
        ) {
            return true;
        }

        return estadoSeleccionado.equalsIgnoreCase(
                cita.getEstado()
        );
    }

    /**
     * Comprueba el filtro de fecha.
     */
    private boolean coincideFecha(
            Cita cita,
            String fechaSeleccionada,
            LocalDate hoy
    ) {

        if (
                fechaSeleccionada == null
                        || "Todas".equalsIgnoreCase(
                                fechaSeleccionada
                        )
        ) {
            return true;
        }

        if (cita.getFechaCita() == null) {
            return false;
        }

        if (
                "Hoy".equalsIgnoreCase(
                        fechaSeleccionada
                )
        ) {

            return cita.getFechaCita()
                    .isEqual(hoy);
        }

        if (
                "Próximas".equalsIgnoreCase(
                        fechaSeleccionada
                )
        ) {

            return !cita.getFechaCita()
                    .isBefore(hoy);
        }

        return true;
    }

    /**
     * Comprueba si un texto contiene el criterio buscado.
     */
    private boolean contieneTexto(
            String valor,
            String texto
    ) {

        return valor != null
                && valor.toLowerCase(Locale.ROOT)
                        .contains(texto);
    }

    /**
     * Limpia todos los filtros.
     */
    @FXML
    private void limpiarFiltros() {

        txtBuscar.clear();

        cmbEstado.setValue(
                "Todos"
        );

        cmbFecha.setValue(
                "Todas"
        );

        aplicarFiltros();
    }

    /**
     * Muestra las citas filtradas.
     */
    private void mostrarCitas(
            List<Cita> citas
    ) {

        contenedorCitas.getChildren().clear();

        limpiarMensaje();

        if (citas == null || citas.isEmpty()) {

            lblCantidadCitas.setText("0");

            mostrarMensaje(
                    "No existen citas que coincidan con los filtros seleccionados."
            );

            return;
        }

        lblCantidadCitas.setText(
                String.valueOf(
                        citas.size()
                )
        );

        for (Cita cita : citas) {

            contenedorCitas
                    .getChildren()
                    .add(
                            crearTarjetaCita(cita)
                    );
        }
    }

    /**
     * Construye una tarjeta visual para una cita.
     */
    private VBox crearTarjetaCita(
            Cita cita
    ) {

        VBox tarjeta =
                new VBox(14);

        tarjeta.getStyleClass().add(
                "doctor-appointment-card"
        );

        tarjeta.setPadding(
                new Insets(18, 20, 18, 20)
        );

        /*
         * ========================================================
         * ENCABEZADO
         * ========================================================
         */

        HBox encabezado =
                new HBox(14);

        encabezado.setAlignment(
                Pos.CENTER_LEFT
        );

        VBox bloqueFecha =
                new VBox(2);

        Label lblFecha =
                new Label(
                        cita.getFechaCita()
                                .format(formatoFecha)
                                .toUpperCase()
                );

        lblFecha.getStyleClass().add(
                "doctor-date-label"
        );

        Label lblHora =
                new Label(
                        cita.getHoraInicio()
                                .format(formatoHora)
                                + "  —  "
                                + cita.getHoraFin()
                                        .format(formatoHora)
                );

        lblHora.getStyleClass().add(
                "doctor-time-label"
        );

        bloqueFecha.getChildren().addAll(
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
                        cita.getEstado()
                );

        lblEstado.getStyleClass().add(
                "doctor-appointment-status"
        );

        if (cita.getEstado() != null) {

            lblEstado.getStyleClass().add(
                    "status-"
                            + cita.getEstado()
                                    .toLowerCase(Locale.ROOT)
                                    .replace('_', '-')
            );
        }

        encabezado.getChildren().addAll(
                bloqueFecha,
                espacio,
                lblEstado
        );

        /*
         * ========================================================
         * PACIENTE
         * ========================================================
         */

        VBox bloquePaciente =
                new VBox(3);

        Label lblPaciente =
                new Label(
                        valorSeguro(
                                cita.getNombrePaciente()
                        )
                );

        lblPaciente.getStyleClass().add(
                "doctor-patient-name"
        );

        Label lblEspecialidad =
                new Label(
                        valorSeguro(
                                cita.getNombreEspecialidad()
                        )
                );

        lblEspecialidad.getStyleClass().add(
                "doctor-specialty"
        );

        bloquePaciente.getChildren().addAll(
                lblPaciente,
                lblEspecialidad
        );

        /*
         * ========================================================
         * INFORMACIÓN COMPLEMENTARIA
         * ========================================================
         */

        HBox informacion =
                new HBox(30);

        informacion.setAlignment(
                Pos.CENTER_LEFT
        );

        VBox bloqueLugar =
                crearBloqueInformacion(
                        "ESTABLECIMIENTO",
                        cita.getNombreEstablecimiento()
                );

        VBox bloqueMotivo =
                crearBloqueInformacion(
                        "MOTIVO DE CONSULTA",
                        cita.getMotivoConsulta()
                );

        HBox.setHgrow(
                bloqueMotivo,
                Priority.ALWAYS
        );

        informacion.getChildren().addAll(
                bloqueLugar,
                bloqueMotivo
        );

        tarjeta.getChildren().addAll(
                encabezado,
                bloquePaciente,
                informacion
        );

        /*
         * ========================================================
         * INFORMACIÓN DE CANCELACIÓN
         * ========================================================
         */

        if (
                "CANCELADA".equalsIgnoreCase(
                        cita.getEstado()
                )
                        && cita.getMotivoCancelacion() != null
                        && !cita.getMotivoCancelacion().isBlank()
        ) {

            VBox bloqueCancelacion =
                    crearBloqueInformacion(
                            "MOTIVO DE CANCELACIÓN",
                            cita.getMotivoCancelacion()
                    );

            bloqueCancelacion.getStyleClass().add(
                    "doctor-cancellation-box"
            );

            tarjeta.getChildren().add(
                    bloqueCancelacion
            );
        }

        /*
         * ========================================================
         * OBSERVACIÓN DE ATENCIÓN
         * ========================================================
         */

        if (
                "ATENDIDA".equalsIgnoreCase(
                        cita.getEstado()
                )
                        && cita.getObservacion() != null
                        && !cita.getObservacion().isBlank()
        ) {

            VBox bloqueObservacion =
                    crearBloqueInformacion(
                            "OBSERVACIÓN DE ATENCIÓN",
                            cita.getObservacion()
                    );

            bloqueObservacion.getStyleClass().add(
                    "doctor-observation-box"
            );

            tarjeta.getChildren().add(
                    bloqueObservacion
            );
        }

        /*
         * ========================================================
         * ACCIONES MÉDICAS
         * ========================================================
         */

        HBox acciones =
                crearAccionesCita(
                        cita
                );

        if (acciones != null) {

            tarjeta.getChildren().add(
                    acciones
            );
        }

        return tarjeta;
    }

    /**
     * Construye las acciones disponibles según
     * el estado actual de la cita.
     */
    private HBox crearAccionesCita(
            Cita cita
    ) {

        if (
                cita == null
                        || cita.getEstado() == null
        ) {
            return null;
        }

        String estado =
                cita.getEstado()
                        .trim()
                        .toUpperCase(Locale.ROOT);

        HBox acciones =
                new HBox(10);

        acciones.setAlignment(
                Pos.CENTER_RIGHT
        );

        switch (estado) {

            case "PROGRAMADA" -> {

                Button btnConfirmar =
                        new Button(
                                "Confirmar cita"
                        );

                btnConfirmar
                        .getStyleClass()
                        .add(
                                "doctor-action-primary"
                        );

                btnConfirmar.setOnAction(
                        evento ->
                                cambiarEstadoCita(
                                        cita,
                                        "CONFIRMADA"
                                )
                );

                acciones.getChildren().add(
                        btnConfirmar
                );
            }

            case "CONFIRMADA" -> {

                Button btnAtendida =
                        new Button(
                                "Marcar atendida"
                        );

                btnAtendida
                        .getStyleClass()
                        .add(
                                "doctor-action-primary"
                        );

                /*
                 * Una cita no pasa directamente a ATENDIDA.
                 * Primero se solicita la observación médica.
                 */
                btnAtendida.setOnAction(
                        evento ->
                                abrirDialogoAtencion(
                                        cita
                                )
                );

                Button btnNoAsistio =
                        new Button(
                                "No asistió"
                        );

                btnNoAsistio
                        .getStyleClass()
                        .add(
                                "doctor-action-secondary"
                        );

                btnNoAsistio.setOnAction(
                        evento ->
                                cambiarEstadoCita(
                                        cita,
                                        "NO_ASISTIO"
                                )
                );

                acciones.getChildren().addAll(
                        btnAtendida,
                        btnNoAsistio
                );
            }

            default -> {

                /*
                 * ATENDIDA, CANCELADA y NO_ASISTIO
                 * son estados finales para las acciones
                 * disponibles en esta pantalla.
                 */
                return null;
            }
        }

        return acciones;
    }

    /**
     * Abre el formulario para registrar la observación
     * antes de finalizar una atención médica.
     *
     * @param cita cita que será atendida.
     */
    private void abrirDialogoAtencion(
            Cita cita
    ) {

        if (
                cita == null
                        || medicoActual == null
        ) {

            mostrarMensaje(
                    "No fue posible identificar la cita o el médico."
            );

            return;
        }

        Dialog<String> dialogo =
                new Dialog<>();

        dialogo.setTitle(
                "Finalizar atención"
        );

        dialogo.setHeaderText(
                "Registrar observación médica"
        );

        /*
         * Asocia el diálogo con la ventana principal.
         */
        if (
                lblNombreMedico != null
                        && lblNombreMedico.getScene() != null
        ) {

            dialogo.initOwner(
                    lblNombreMedico
                            .getScene()
                            .getWindow()
            );
        }

        ButtonType btnFinalizar =
                new ButtonType(
                        "Finalizar atención",
                        ButtonBar.ButtonData.OK_DONE
                );

        ButtonType btnCancelar =
                new ButtonType(
                        "Cancelar",
                        ButtonBar.ButtonData.CANCEL_CLOSE
                );

        dialogo.getDialogPane()
                .getButtonTypes()
                .addAll(
                        btnCancelar,
                        btnFinalizar
                );

        /*
         * ========================================================
         * CONTENIDO DEL DIÁLOGO
         * ========================================================
         */

        VBox contenido =
                new VBox(10);

        contenido.setPadding(
                new Insets(10)
        );

        contenido.setPrefWidth(440);

        Label lblPacienteTitulo =
                new Label(
                        "PACIENTE"
                );

        lblPacienteTitulo.getStyleClass().add(
                "doctor-dialog-label"
        );

        Label lblPaciente =
                new Label(
                        valorSeguro(
                                cita.getNombrePaciente()
                        )
                );

        lblPaciente.getStyleClass().add(
                "doctor-dialog-patient"
        );

        Label lblObservacion =
                new Label(
                        "OBSERVACIÓN DE LA ATENCIÓN"
                );

        lblObservacion.getStyleClass().add(
                "doctor-dialog-label"
        );

        TextArea txtObservacion =
                new TextArea();

        txtObservacion.setPromptText(
                "Registre las observaciones realizadas durante la atención..."
        );

        txtObservacion.setWrapText(
                true
        );

        txtObservacion.setPrefRowCount(
                6
        );

        txtObservacion.getStyleClass().add(
                "doctor-observation-area"
        );

        Label lblContador =
                new Label(
                        "0 / 500"
                );

        lblContador.getStyleClass().add(
                "doctor-character-counter"
        );

        /*
         * Limita físicamente la entrada a los
         * 500 caracteres admitidos por MySQL.
         */
        txtObservacion.textProperty().addListener(
                (observable, anterior, nuevo) -> {

                    if (
                            nuevo != null
                                    && nuevo.length() > 500
                    ) {

                        txtObservacion.setText(
                                nuevo.substring(
                                        0,
                                        500
                                )
                        );

                        txtObservacion.positionCaret(
                                500
                        );

                        return;
                    }

                    int cantidad =
                            nuevo == null
                                    ? 0
                                    : nuevo.length();

                    lblContador.setText(
                            cantidad
                                    + " / 500"
                    );
                }
        );

        Region espacioContador =
                new Region();

        HBox.setHgrow(
                espacioContador,
                Priority.ALWAYS
        );

        HBox filaContador =
                new HBox();

        filaContador.getChildren().addAll(
                espacioContador,
                lblContador
        );

        contenido.getChildren().addAll(
                lblPacienteTitulo,
                lblPaciente,
                lblObservacion,
                txtObservacion,
                filaContador
        );

        dialogo.getDialogPane()
                .setContent(
                        contenido
                );

        /*
         * ========================================================
         * VALIDACIÓN DEL BOTÓN FINALIZAR
         * ========================================================
         */

        Node botonFinalizar =
                dialogo.getDialogPane()
                        .lookupButton(
                                btnFinalizar
                        );

        botonFinalizar.setDisable(
                true
        );

        txtObservacion.textProperty().addListener(
                (observable, anterior, nuevo) ->
                        botonFinalizar.setDisable(
                                nuevo == null
                                        || nuevo.isBlank()
                        )
        );

        /*
         * El diálogo devuelve la observación solamente
         * cuando el usuario confirma la atención.
         */
        dialogo.setResultConverter(
                boton -> {

                    if (boton == btnFinalizar) {

                        return txtObservacion
                                .getText()
                                .trim();
                    }

                    return null;
                }
        );

        dialogo.showAndWait()
                .ifPresent(
                        observacion ->
                                finalizarAtencion(
                                        cita,
                                        observacion
                                )
                );
    }

    /**
     * Finaliza una cita confirmada y registra
     * la observación médica correspondiente.
     *
     * @param cita cita atendida.
     * @param observacion observación registrada.
     */
    private void finalizarAtencion(
            Cita cita,
            String observacion
    ) {

        if (
                cita == null
                        || medicoActual == null
        ) {

            mostrarMensaje(
                    "No fue posible identificar la cita o el médico."
            );

            return;
        }

        try {

            citaService.finalizarAtencion(
                    cita.getIdCita(),
                    medicoActual.getIdMedico(),
                    observacion
            );

            /*
             * Recarga los datos desde MySQL para mostrar
             * inmediatamente el nuevo estado y observación.
             */
            cargarAgenda();

            mostrarMensaje(
                    "La atención fue finalizada correctamente."
            );

        } catch (IllegalArgumentException e) {

            /*
             * Si la cita cambió mientras estaba abierto
             * el diálogo, se actualiza la agenda.
             */
            cargarAgenda();

            mostrarMensaje(
                    e.getMessage()
            );

        } catch (SQLException e) {

            mostrarMensaje(
                    "No fue posible finalizar la atención."
            );

            System.err.println(
                    "Error al finalizar atención: "
                            + e.getMessage()
            );
        }
    }

    /**
     * Solicita al servicio un cambio de estado
     * que no requiere observación médica.
     */
    private void cambiarEstadoCita(
            Cita cita,
            String nuevoEstado
    ) {

        if (
                cita == null
                        || medicoActual == null
        ) {

            mostrarMensaje(
                    "No fue posible identificar la cita o el médico."
            );

            return;
        }

        try {

            citaService.cambiarEstadoPorMedico(
                    cita.getIdCita(),
                    medicoActual.getIdMedico(),
                    cita.getEstado(),
                    nuevoEstado
            );

            /*
             * Se vuelve a consultar MySQL para evitar
             * trabajar con información desactualizada.
             */
            cargarAgenda();

            mostrarMensaje(
                    obtenerMensajeCambioEstado(
                            nuevoEstado
                    )
            );

        } catch (IllegalArgumentException e) {

            /*
             * Si el estado fue modificado desde otra
             * operación, refrescamos la agenda.
             */
            cargarAgenda();

            mostrarMensaje(
                    e.getMessage()
            );

        } catch (SQLException e) {

            mostrarMensaje(
                    "No fue posible actualizar el estado de la cita."
            );

            System.err.println(
                    "Error al actualizar estado de cita: "
                            + e.getMessage()
            );
        }
    }

    /**
     * Devuelve un mensaje amigable después
     * de actualizar correctamente una cita.
     */
    private String obtenerMensajeCambioEstado(
            String nuevoEstado
    ) {

        if (nuevoEstado == null) {

            return "El estado de la cita fue actualizado.";
        }

        return switch (
                nuevoEstado.toUpperCase(Locale.ROOT)
        ) {

            case "CONFIRMADA" ->
                    "La cita fue confirmada correctamente.";

            case "NO_ASISTIO" ->
                    "La cita fue marcada como no asistida.";

            default ->
                    "El estado de la cita fue actualizado.";
        };
    }

    /**
     * Construye un bloque de información de una cita.
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
                "doctor-info-label"
        );

        Label lblValor =
                new Label(
                        valorSeguro(
                                valor
                        )
                );

        lblValor.getStyleClass().add(
                "doctor-info-value"
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
     * Evita mostrar valores nulos.
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
     * Actualiza la agenda desde MySQL.
     */
    @FXML
    private void actualizarAgenda() {

        cargarAgenda();
    }

    /**
 * Abre el historial de atenciones
 * del médico autenticado.
 */
@FXML
private void abrirHistorial() {

    if (usuarioActual == null) {

        mostrarMensaje(
                "No existe una sesión médica activa."
        );

        return;
    }

    try {

        FXMLLoader loader =
                new FXMLLoader(
                        getClass().getResource(
                                "/fxml/historial-atenciones-medico.fxml"
                        )
                );

        Parent root =
                loader.load();

        HistorialAtencionesMedicoController controlador =
                loader.getController();

        controlador.setUsuario(
                usuarioActual
        );

        Stage stage =
                (Stage) lblNombreMedico
                        .getScene()
                        .getWindow();

        stage.setScene(
                new Scene(root)
        );

        stage.setTitle(
                "MediAppoint - Historial de atenciones"
        );

        stage.setMinWidth(1050);
        stage.setMinHeight(680);
        stage.centerOnScreen();

    } catch (Exception excepcion) {

        mostrarMensaje(
                "No fue posible abrir el historial de atenciones."
        );

        System.err.println(
                "Error al abrir historial médico: "
                        + excepcion.getMessage()
        );

        excepcion.printStackTrace();
    }
}

    /**
     * Abre el perfil profesional del médico autenticado.
     */
    @FXML
    private void abrirPerfil() {

        if (usuarioActual == null) {

            mostrarMensaje(
                    "No existe una sesión médica activa."
            );

            return;
        }

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/perfil-medico.fxml"
                            )
                    );

            Parent root =
                    loader.load();

            PerfilMedicoController controlador =
                    loader.getController();

            controlador.setUsuario(
                    usuarioActual
            );

            Stage stage =
                    (Stage) lblNombreMedico
                            .getScene()
                            .getWindow();

            stage.setScene(
                    new Scene(root)
            );

            stage.setTitle(
                    "MediAppoint - Mi perfil"
            );

            stage.setMinWidth(1050);
            stage.setMinHeight(680);
            stage.centerOnScreen();

        } catch (Exception e) {

            mostrarMensaje(
                    "No fue posible abrir el perfil médico."
            );

            System.err.println(
                    "Error al abrir perfil médico: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }
    }

    /**
     * Cierra la sesión actual.
     */
    @FXML
    private void cerrarSesion() {

        try {

            Stage stage =
                    (Stage) lblNombreMedico
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
 * Ejecuta una tarea JavaFX en un hilo secundario.
 *
 * Las consultas a MySQL no deben ejecutarse en
 * el JavaFX Application Thread porque bloquearían
 * la interfaz gráfica.
 *
 * @param tarea tarea que será ejecutada.
 * @param nombreHilo nombre descriptivo del hilo.
 */
private void ejecutarTarea(
        Task<?> tarea,
        String nombreHilo
) {

    Thread hilo =
            new Thread(
                    tarea,
                    nombreHilo
            );

    hilo.setDaemon(
            true
    );

    hilo.start();
}

    /**
     * Muestra mensajes en pantalla.
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