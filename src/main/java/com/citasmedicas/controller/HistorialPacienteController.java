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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * ================================================================
 *              CONTROLLER - HISTORIAL DEL PACIENTE
 * ================================================================
 *
 * Gestiona la consulta, búsqueda, filtrado y visualización
 * del historial de atenciones médicas del paciente autenticado.
 *
 * El historial muestra únicamente citas con estado ATENDIDA.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 2.0
 */
public class HistorialPacienteController {

    /* ============================================================
                            CONTROLES FXML
       ============================================================ */

    @FXML
    private Label lblNombreUsuario;

    @FXML
    private Label lblCantidadAtenciones;

    @FXML
    private Label lblMensaje;

    @FXML
    private VBox contenedorHistorial;

    @FXML
    private TextField txtBuscarHistorial;

    @FXML
    private ComboBox<String> cmbEspecialidadHistorial;

    @FXML
    private ComboBox<String> cmbFechaHistorial;

    @FXML
    private ComboBox<String> cmbOrdenHistorial;

    /* ============================================================
                              SERVICIOS
       ============================================================ */

    private final PacienteDAO pacienteDAO;
    private final CitaService citaService;

    private Usuario usuarioActual;

    /**
     * Historial completo recuperado desde MySQL.
     * Los filtros trabajan sobre esta colección.
     */
    private List<Cita> historialPaciente =
            new ArrayList<>();

    private final DateTimeFormatter formatoFecha =
            DateTimeFormatter.ofPattern(
                    "dd MMM yyyy",
                    new Locale("es", "EC")
            );

    private final DateTimeFormatter formatoHora =
            DateTimeFormatter.ofPattern("HH:mm");

    public HistorialPacienteController() {

        this.pacienteDAO =
                new PacienteDAO();

        this.citaService =
                new CitaService();
    }

    /**
     * Configura filtros y listeners al cargar el FXML.
     */
    @FXML
    private void initialize() {

        cmbEspecialidadHistorial
                .getItems()
                .setAll("Todas las especialidades");

        cmbEspecialidadHistorial.setValue(
                "Todas las especialidades"
        );

        cmbFechaHistorial
                .getItems()
                .setAll(
                        "Todas las fechas",
                        "Últimos 30 días",
                        "Últimos 6 meses",
                        "Último año"
                );

        cmbFechaHistorial.setValue(
                "Todas las fechas"
        );

        cmbOrdenHistorial
                .getItems()
                .setAll(
                        "Más recientes",
                        "Más antiguas"
                );

        cmbOrdenHistorial.setValue(
                "Más recientes"
        );

        txtBuscarHistorial
                .textProperty()
                .addListener(
                        (observable, anterior, nuevo) ->
                                aplicarFiltros()
                );

        cmbEspecialidadHistorial.setOnAction(
                event -> aplicarFiltros()
        );

        cmbFechaHistorial.setOnAction(
                event -> aplicarFiltros()
        );

        cmbOrdenHistorial.setOnAction(
                event -> aplicarFiltros()
        );
    }

    /**
     * Recibe el usuario autenticado.
     */
    public void setUsuario(
            Usuario usuario
    ) {

        this.usuarioActual = usuario;

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
     * Recupera el historial del paciente desde
     * la capa de servicio.
     */
    private void cargarHistorial() {

        limpiarMensaje();

        if (usuarioActual == null) {

            lblCantidadAtenciones.setText("0");

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

                lblCantidadAtenciones.setText("0");

                mostrarMensaje(
                        "No se encontró el perfil del paciente."
                );

                return;
            }

            historialPaciente =
                    new ArrayList<>(
                            citaService.listarHistorialPaciente(
                                    paciente.getIdPaciente()
                            )
                    );

            cargarEspecialidades();

            aplicarFiltros();

        } catch (SQLException e) {

            lblCantidadAtenciones.setText("0");

            mostrarMensaje(
                    "No fue posible cargar el historial de atenciones."
            );

            System.err.println(
                    "Error al cargar historial del paciente: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }
    }

    /**
     * Genera dinámicamente el filtro de especialidades
     * utilizando únicamente las presentes en el historial.
     */
    private void cargarEspecialidades() {

        String seleccionActual =
                cmbEspecialidadHistorial.getValue();

        List<String> especialidades =
                historialPaciente.stream()
                        .map(Cita::getNombreEspecialidad)
                        .filter(valor ->
                                valor != null
                                        && !valor.isBlank()
                        )
                        .distinct()
                        .sorted(
                                String.CASE_INSENSITIVE_ORDER
                        )
                        .toList();

        cmbEspecialidadHistorial
                .getItems()
                .setAll("Todas las especialidades");

        cmbEspecialidadHistorial
                .getItems()
                .addAll(especialidades);

        if (
                seleccionActual != null
                        && cmbEspecialidadHistorial
                                .getItems()
                                .contains(seleccionActual)
        ) {

            cmbEspecialidadHistorial.setValue(
                    seleccionActual
            );

        } else {

            cmbEspecialidadHistorial.setValue(
                    "Todas las especialidades"
            );
        }
    }

    /**
     * Aplica búsqueda, especialidad, período y orden.
     */
    @FXML
    private void aplicarFiltros() {

        if (contenedorHistorial == null) {
            return;
        }

        String texto =
                txtBuscarHistorial == null
                        || txtBuscarHistorial.getText() == null
                        ? ""
                        : txtBuscarHistorial
                                .getText()
                                .trim()
                                .toLowerCase(Locale.ROOT);

        String especialidad =
                cmbEspecialidadHistorial == null
                        ? null
                        : cmbEspecialidadHistorial.getValue();

        String periodo =
                cmbFechaHistorial == null
                        ? null
                        : cmbFechaHistorial.getValue();

        String orden =
                cmbOrdenHistorial == null
                        ? null
                        : cmbOrdenHistorial.getValue();

        List<Cita> resultado =
                historialPaciente.stream()

                        .filter(cita ->
                                coincideBusqueda(
                                        cita,
                                        texto
                                )
                        )

                        .filter(cita ->
                                coincideEspecialidad(
                                        cita,
                                        especialidad
                                )
                        )

                        .filter(cita ->
                                coincidePeriodo(
                                        cita,
                                        periodo
                                )
                        )

                        .sorted(
                                obtenerComparador(
                                        orden
                                )
                        )

                        .toList();

        mostrarHistorial(resultado);
    }

    /**
     * Busca en los principales datos visibles
     * de una atención.
     */
    private boolean coincideBusqueda(
            Cita cita,
            String texto
    ) {

        if (texto == null || texto.isBlank()) {
            return true;
        }

        return contieneTexto(
                cita.getNombreEspecialidad(),
                texto
        )
                || contieneTexto(
                        cita.getNombreMedico(),
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

    private boolean coincideEspecialidad(
            Cita cita,
            String especialidad
    ) {

        if (
                especialidad == null
                        || "Todas las especialidades"
                                .equals(especialidad)
        ) {
            return true;
        }

        return especialidad.equalsIgnoreCase(
                cita.getNombreEspecialidad()
        );
    }

    /**
     * Filtra por períodos relativos a la fecha actual.
     */
    private boolean coincidePeriodo(
            Cita cita,
            String periodo
    ) {

        if (
                periodo == null
                        || "Todas las fechas".equals(periodo)
        ) {
            return true;
        }

        if (cita.getFechaCita() == null) {
            return false;
        }

        LocalDate hoy =
                LocalDate.now();

        LocalDate limite;

        switch (periodo) {

            case "Últimos 30 días" ->
                    limite = hoy.minusDays(30);

            case "Últimos 6 meses" ->
                    limite = hoy.minusMonths(6);

            case "Último año" ->
                    limite = hoy.minusYears(1);

            default -> {
                return true;
            }
        }

        return !cita.getFechaCita().isBefore(limite)
                && !cita.getFechaCita().isAfter(hoy);
    }

    /**
     * Define el orden cronológico seleccionado.
     */
    private Comparator<Cita> obtenerComparador(
            String orden
    ) {

        Comparator<Cita> comparador =
                Comparator.comparing(
                        Cita::getFechaCita,
                        Comparator.nullsLast(
                                Comparator.naturalOrder()
                        )
                );

        if (!"Más antiguas".equals(orden)) {
            comparador = comparador.reversed();
        }

        return comparador;
    }

    private boolean contieneTexto(
            String valor,
            String texto
    ) {

        return valor != null
                && valor.toLowerCase(Locale.ROOT)
                        .contains(texto);
    }

    /**
     * Restablece todos los filtros.
     */
    @FXML
    private void limpiarFiltros() {

        txtBuscarHistorial.clear();

        cmbEspecialidadHistorial.setValue(
                "Todas las especialidades"
        );

        cmbFechaHistorial.setValue(
                "Todas las fechas"
        );

        cmbOrdenHistorial.setValue(
                "Más recientes"
        );

        aplicarFiltros();
    }

    /**
     * Renderiza las atenciones filtradas.
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
                    historialPaciente.isEmpty()
                            ? "Todavía no existen atenciones médicas registradas."
                            : "No encontramos atenciones que coincidan con los filtros seleccionados."
            );

            return;
        }

        for (Cita cita : historial) {

            contenedorHistorial
                    .getChildren()
                    .add(
                            crearTarjetaHistorial(cita)
                    );
        }
    }

    /**
     * Construye una tarjeta visual para cada
     * atención médica realizada.
     */
    private HBox crearTarjetaHistorial(
            Cita cita
    ) {

        HBox tarjeta =
                new HBox(0);

        tarjeta.getStyleClass().add(
                "history-v2-card"
        );

        /*
         * ========================================================
         * COLUMNA DE FECHA
         * ========================================================
         */

        VBox bloqueFecha =
                new VBox(5);

        bloqueFecha.setAlignment(
                Pos.TOP_LEFT
        );

        bloqueFecha.setPrefWidth(145);

        bloqueFecha.getStyleClass().add(
                "history-v2-date-column"
        );

        Label lblDia =
                new Label(
                        cita.getFechaCita() == null
                                ? "--"
                                : String.format(
                                        "%02d",
                                        cita.getFechaCita()
                                                .getDayOfMonth()
                                )
                );

        lblDia.getStyleClass().add(
                "history-v2-day"
        );

        Label lblMesAnio =
                new Label(
                        cita.getFechaCita() == null
                                ? "FECHA NO DISPONIBLE"
                                : cita.getFechaCita()
                                        .format(
                                                DateTimeFormatter.ofPattern(
                                                        "MMM yyyy",
                                                        new Locale(
                                                                "es",
                                                                "EC"
                                                        )
                                                )
                                        )
                                        .toUpperCase()
                );

        lblMesAnio.getStyleClass().add(
                "history-v2-month"
        );

        Label lblHora =
                new Label(
                        obtenerHorario(cita)
                );

        lblHora.getStyleClass().add(
                "history-v2-time"
        );

        bloqueFecha.getChildren().addAll(
                lblDia,
                lblMesAnio,
                lblHora
        );

        /*
         * ========================================================
         * CONTENIDO PRINCIPAL
         * ========================================================
         */

        VBox contenido =
                new VBox(13);

        contenido.getStyleClass().add(
                "history-v2-content"
        );

        HBox.setHgrow(
                contenido,
                Priority.ALWAYS
        );

        HBox cabecera =
                new HBox(12);

        cabecera.setAlignment(
                Pos.CENTER_LEFT
        );

        VBox titulo =
                new VBox(3);

        HBox.setHgrow(
                titulo,
                Priority.ALWAYS
        );

        Label lblEspecialidad =
                new Label(
                        valorSeguro(
                                cita.getNombreEspecialidad()
                        )
                );

        lblEspecialidad.getStyleClass().add(
                "history-v2-specialty"
        );

        Label lblTipo =
                new Label(
                        "Atención médica realizada"
                );

        lblTipo.getStyleClass().add(
                "history-v2-type"
        );

        titulo.getChildren().addAll(
                lblEspecialidad,
                lblTipo
        );

        Region espacio =
                new Region();

        HBox.setHgrow(
                espacio,
                Priority.ALWAYS
        );

        Label lblEstado =
                new Label("✓  ATENDIDA");

        lblEstado.getStyleClass().add(
                "history-v2-status"
        );

        cabecera.getChildren().addAll(
                titulo,
                espacio,
                lblEstado
        );

        /*
         * Médico y establecimiento.
         */

        HBox informacion =
                new HBox(35);

        informacion.setAlignment(
                Pos.TOP_LEFT
        );

        VBox medico =
                crearBloqueInformacion(
                        "MÉDICO",
                        cita.getNombreMedico()
                );

        VBox establecimiento =
                crearBloqueInformacion(
                        "ESTABLECIMIENTO",
                        cita.getNombreEstablecimiento()
                );

        HBox.setHgrow(
                medico,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                establecimiento,
                Priority.ALWAYS
        );

        informacion.getChildren().addAll(
                medico,
                establecimiento
        );

        /*
         * Motivo.
         */

        VBox motivo =
                crearBloqueInformacion(
                        "MOTIVO DE CONSULTA",
                        cita.getMotivoConsulta()
                );

        /*
         * Observación médica.
         */

        VBox observacion =
                new VBox(5);

        observacion.getStyleClass().add(
                "history-v2-observation"
        );

        Label lblObservacionTitulo =
                new Label(
                        "OBSERVACIÓN DE LA ATENCIÓN"
                );

        lblObservacionTitulo.getStyleClass().add(
                "history-v2-observation-label"
        );

        Label lblObservacion =
                new Label(
                        valorSeguro(
                                cita.getObservacion()
                        )
                );

        lblObservacion.setWrapText(true);

        lblObservacion.getStyleClass().add(
                "history-v2-observation-value"
        );

        observacion.getChildren().addAll(
                lblObservacionTitulo,
                lblObservacion
        );

        contenido.getChildren().addAll(
                cabecera,
                informacion,
                motivo,
                observacion
        );

        tarjeta.getChildren().addAll(
                bloqueFecha,
                contenido
        );

        return tarjeta;
    }

    /**
     * Crea un bloque de etiqueta + valor.
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
                "history-v2-info-label"
        );

        Label lblValor =
                new Label(
                        valorSeguro(valor)
                );

        lblValor.setWrapText(true);

        lblValor.getStyleClass().add(
                "history-v2-info-value"
        );

        bloque.getChildren().addAll(
                lblTitulo,
                lblValor
        );

        return bloque;
    }

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
                + "  —  "
                + cita.getHoraFin()
                        .format(formatoHora);
    }

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

    /* ============================================================
                              NAVEGACIÓN
       ============================================================ */

    @FXML
    private void abrirInicio() {

        cambiarPantalla(
                "/fxml/dashboard.fxml",
                "MediAppoint - Panel principal"
        );
    }

    @FXML
    private void abrirAgendarCita() {

        cambiarPantalla(
                "/fxml/agendar-cita.fxml",
                "MediAppoint - Agendar cita"
        );
    }

    @FXML
    private void abrirMisCitas() {

        cambiarPantalla(
                "/fxml/mis-citas.fxml",
                "MediAppoint - Mis citas"
        );
    }

    @FXML
    private void abrirHistorial() {

        cargarHistorial();
    }

    @FXML
    private void abrirPerfil() {

        cambiarPantalla(
                "/fxml/perfil.fxml",
                "MediAppoint - Mi perfil"
        );
    }

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

            stage.setScene(scene);
            stage.setTitle(titulo);

            stage.setMinWidth(1050);
            stage.setMinHeight(680);

            stage.centerOnScreen();

        } catch (IOException e) {

            mostrarMensaje(
                    "No fue posible abrir la pantalla seleccionada."
            );

            System.err.println(
                    "Error al cambiar de pantalla: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }
    }

    /* ============================================================
                              MENSAJES
       ============================================================ */

    private void mostrarMensaje(
            String mensaje
    ) {

        if (lblMensaje == null) {
            return;
        }

        lblMensaje.setText(mensaje);
        lblMensaje.setVisible(true);
        lblMensaje.setManaged(true);
    }

    private void limpiarMensaje() {

        if (lblMensaje == null) {
            return;
        }

        lblMensaje.setText("");
        lblMensaje.setVisible(false);
        lblMensaje.setManaged(false);
    }

    @FXML
    private void cerrarSesion() {

        try {

            Stage stage =
                    (Stage) lblNombreUsuario
                            .getScene()
                            .getWindow();

            usuarioActual = null;
            historialPaciente.clear();

            SesionUtil.cerrarSesion(stage);

        } catch (Exception e) {

            mostrarMensaje(
                    "No fue posible cerrar la sesión."
            );

            System.err.println(
                    "Error al cerrar sesión: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }
    }
}