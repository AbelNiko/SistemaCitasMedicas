package com.citasmedicas.controller;

import com.citasmedicas.dao.MedicoDAO;
import com.citasmedicas.model.Cita;
import com.citasmedicas.model.Medico;
import com.citasmedicas.model.Usuario;
import com.citasmedicas.service.CitaService;
import com.citasmedicas.util.SesionUtil;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
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
 * Permite consultar las citas asignadas, visualizar un resumen
 * de la agenda y aplicar filtros por texto, estado y fecha.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.2
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
     * Obtiene el perfil médico asociado al usuario.
     */
    private void cargarPerfilMedico() {

        try {

            medicoActual =
                    medicoDAO.buscarPorIdUsuario(
                            usuarioActual.getIdUsuario()
                    );

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

        } catch (SQLException e) {

            mostrarMensaje(
                    "No fue posible cargar el perfil médico."
            );

            System.err.println(
                    "Error al cargar perfil médico: "
                            + e.getMessage()
            );
        }
    }

    /**
     * Recupera desde MySQL las citas asignadas al médico.
     */
    @FXML
    private void cargarAgenda() {

        if (medicoActual == null) {
            return;
        }

        try {

            citasMedico =
                    new ArrayList<>(
                            citaService.listarPorMedico(
                                    medicoActual.getIdMedico()
                            )
                    );

            actualizarResumen();
            aplicarFiltros();

        } catch (SQLException e) {

            mostrarMensaje(
                    "No fue posible cargar la agenda médica."
            );

            System.err.println(
                    "Error al cargar agenda médica: "
                            + e.getMessage()
            );
        }
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
         * Encabezado de la cita.
         */
        HBox encabezado =
                new HBox(14);

        encabezado.setAlignment(
                javafx.geometry.Pos.CENTER_LEFT
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
         * Paciente.
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
         * Información complementaria.
         */
        HBox informacion =
                new HBox(30);

        informacion.setAlignment(
                javafx.geometry.Pos.CENTER_LEFT
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
         * Información adicional para citas canceladas.
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

        return tarjeta;
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
                new Label(titulo);

        lblTitulo.getStyleClass().add(
                "doctor-info-label"
        );

        Label lblValor =
                new Label(
                        valorSeguro(valor)
                );

        lblValor.getStyleClass().add(
                "doctor-info-value"
        );

        lblValor.setWrapText(true);

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

        if (valor == null || valor.isBlank()) {
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
     * Muestra mensajes en pantalla.
     */
    private void mostrarMensaje(
            String mensaje
    ) {

        if (lblMensaje != null) {

            lblMensaje.setText(mensaje);
            lblMensaje.setVisible(true);
            lblMensaje.setManaged(true);
        }
    }

    /**
     * Oculta el mensaje actual.
     */
    private void limpiarMensaje() {

        if (lblMensaje != null) {

            lblMensaje.setText("");
            lblMensaje.setVisible(false);
            lblMensaje.setManaged(false);
        }
    }
}