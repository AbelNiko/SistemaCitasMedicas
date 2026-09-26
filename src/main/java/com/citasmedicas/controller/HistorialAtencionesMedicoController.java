package com.citasmedicas.controller;

import com.citasmedicas.dao.MedicoDAO;
import com.citasmedicas.model.Cita;
import com.citasmedicas.model.Medico;
import com.citasmedicas.model.Usuario;
import com.citasmedicas.service.CitaService;
import com.citasmedicas.util.SesionUtil;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * ================================================================
 *          CONTROLADOR - HISTORIAL DE ATENCIONES MÉDICAS
 * ================================================================
 *
 * Gestiona el historial de las citas que ya fueron atendidas
 * por el médico autenticado.
 *
 * Permite:
 * - consultar las citas finalizadas;
 * - visualizar paciente, fecha y horario;
 * - consultar especialidad y establecimiento;
 * - revisar motivo de consulta;
 * - revisar observaciones médicas;
 * - buscar dentro del historial;
 * - filtrar por fecha;
 * - ordenar los resultados;
 * - navegar dentro del Portal Médico.
 *
 * Las consultas a MySQL se ejecutan en segundo plano
 * para evitar bloquear la interfaz JavaFX.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public class HistorialAtencionesMedicoController {

    /* ============================================================
                             CONTROLES FXML
       ============================================================ */

    @FXML
    private Label lblNombreMedico;

    @FXML
    private Label lblCedulaProfesional;

    @FXML
    private Label lblTotalAtenciones;

    @FXML
    private Label lblCantidadResultados;

    @FXML
    private Label lblMensaje;

    @FXML
    private TextField txtBuscar;

    @FXML
    private ComboBox<String> cmbFecha;

    @FXML
    private ComboBox<String> cmbOrden;

    @FXML
    private VBox contenedorAtenciones;

    /* ============================================================
                         DEPENDENCIAS Y ESTADO
       ============================================================ */

    private final MedicoDAO medicoDAO;
    private final CitaService citaService;

    private Usuario usuarioActual;
    private Medico medicoActual;

    private List<Cita> historial =
            new ArrayList<>();

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
    public HistorialAtencionesMedicoController() {

        this.medicoDAO =
                new MedicoDAO();

        this.citaService =
                new CitaService();
    }

    /* ============================================================
                             INICIALIZACIÓN
       ============================================================ */

    /**
     * Inicializa los filtros de la pantalla.
     */
    @FXML
    private void initialize() {

        configurarFiltros();
    }

    /**
     * Configura las opciones disponibles
     * para filtrar y ordenar el historial.
     */
    private void configurarFiltros() {

        cmbFecha.getItems().setAll(
                "Todas",
                "Últimos 30 días",
                "Este año"
        );

        cmbFecha.setValue(
                "Todas"
        );

        cmbOrden.getItems().setAll(
                "Más recientes",
                "Más antiguas"
        );

        cmbOrden.setValue(
                "Más recientes"
        );

        txtBuscar.textProperty().addListener(
                (observable, anterior, nuevo) ->
                        aplicarFiltros()
        );

        cmbFecha.setOnAction(
                evento ->
                        aplicarFiltros()
        );

        cmbOrden.setOnAction(
                evento ->
                        aplicarFiltros()
        );
    }

    /* ============================================================
                          USUARIO AUTENTICADO
       ============================================================ */

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
                valorSeguro(
                        usuarioActual.getNombreCompleto()
                )
        );

        cargarHistorial();
    }

    /* ============================================================
                         CARGA DEL HISTORIAL
       ============================================================ */

    /**
     * Consulta el perfil del médico y posteriormente
     * su historial de atenciones en segundo plano.
     */
    @FXML
    private void cargarHistorial() {

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
                "Cargando historial de atenciones..."
        );

        int idUsuario =
                usuarioActual.getIdUsuario();

        Task<DatosHistorial> tarea =
                new Task<>() {

                    @Override
                    protected DatosHistorial call()
                            throws Exception {

                        Medico medico =
                                medicoDAO.buscarPorIdUsuario(
                                        idUsuario
                                );

                        if (medico == null) {

                            return new DatosHistorial(
                                    null,
                                    List.of()
                            );
                        }

                        List<Cita> atenciones =
                                citaService.listarHistorialMedico(
                                        medico.getIdMedico()
                                );

                        return new DatosHistorial(
                                medico,
                                atenciones
                        );
                    }
                };

        tarea.setOnSucceeded(
                evento -> {

                    DatosHistorial datos =
                            tarea.getValue();

                    if (
                            datos == null
                                    || datos.medico() == null
                    ) {

                        mostrarMensaje(
                                "No se encontró el perfil médico asociado."
                        );

                        return;
                    }

                    medicoActual =
                            datos.medico();

                    historial =
                            datos.atenciones() == null
                                    ? new ArrayList<>()
                                    : new ArrayList<>(
                                            datos.atenciones()
                                    );

                    lblCedulaProfesional.setText(
                            valorSeguro(
                                    medicoActual
                                            .getCedulaProfesional()
                            )
                    );

                    lblTotalAtenciones.setText(
                            String.valueOf(
                                    historial.size()
                            )
                    );

                    aplicarFiltros();
                }
        );

        tarea.setOnFailed(
                evento -> {

                    Throwable error =
                            tarea.getException();

                    mostrarMensaje(
                            "No fue posible cargar el historial de atenciones."
                    );

                    if (error != null) {

                        System.err.println(
                                "Error al cargar historial médico: "
                                        + error.getMessage()
                        );

                        error.printStackTrace();
                    }
                }
        );

        ejecutarTarea(
                tarea,
                "cargar-historial-medico"
        );
    }

    /* ============================================================
                               FILTROS
       ============================================================ */

    /**
     * Aplica búsqueda, periodo y orden
     * sobre las atenciones cargadas.
     */
    private void aplicarFiltros() {

        if (contenedorAtenciones == null) {
            return;
        }

        String texto =
                txtBuscar.getText() == null
                        ? ""
                        : txtBuscar
                                .getText()
                                .trim()
                                .toLowerCase(
                                        Locale.ROOT
                                );

        String fechaSeleccionada =
                cmbFecha.getValue();

        String ordenSeleccionado =
                cmbOrden.getValue();

        LocalDate hoy =
                LocalDate.now();

        List<Cita> resultados =
                historial.stream()
                        .filter(
                                cita ->
                                        coincideTexto(
                                                cita,
                                                texto
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
                        .sorted(
                                obtenerComparador(
                                        ordenSeleccionado
                                )
                        )
                        .toList();

        mostrarAtenciones(
                resultados
        );
    }

    /**
     * Comprueba el buscador de texto.
     */
    private boolean coincideTexto(
            Cita cita,
            String texto
    ) {

        if (
                texto == null
                        || texto.isBlank()
        ) {

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
     * Comprueba el filtro temporal.
     */
    private boolean coincideFecha(
            Cita cita,
            String filtro,
            LocalDate hoy
    ) {

        if (
                filtro == null
                        || "Todas".equalsIgnoreCase(
                                filtro
                        )
        ) {

            return true;
        }

        if (cita.getFechaCita() == null) {
            return false;
        }

        if (
                "Últimos 30 días".equalsIgnoreCase(
                        filtro
                )
        ) {

            LocalDate limite =
                    hoy.minusDays(
                            30
                    );

            return !cita
                    .getFechaCita()
                    .isBefore(
                            limite
                    )
                    && !cita
                            .getFechaCita()
                            .isAfter(
                                    hoy
                            );
        }

        if (
                "Este año".equalsIgnoreCase(
                        filtro
                )
        ) {

            return cita
                    .getFechaCita()
                    .getYear()
                    == hoy.getYear();
        }

        return true;
    }

    /**
     * Construye el comparador según el
     * orden elegido por el usuario.
     */
    private Comparator<Cita> obtenerComparador(
            String orden
    ) {

        Comparator<Cita> comparador =
                Comparator
                        .comparing(
                                Cita::getFechaCita,
                                Comparator.nullsLast(
                                        Comparator.naturalOrder()
                                )
                        )
                        .thenComparing(
                                Cita::getHoraInicio,
                                Comparator.nullsLast(
                                        Comparator.naturalOrder()
                                )
                        );

        if (
                orden == null
                        || "Más recientes".equalsIgnoreCase(
                                orden
                        )
        ) {

            return comparador.reversed();
        }

        return comparador;
    }

    /**
     * Restablece los filtros.
     */
    @FXML
    private void limpiarFiltros() {

        txtBuscar.clear();

        cmbFecha.setValue(
                "Todas"
        );

        cmbOrden.setValue(
                "Más recientes"
        );

        aplicarFiltros();
    }

    /* ============================================================
                         REPRESENTACIÓN VISUAL
       ============================================================ */

    /**
     * Muestra las atenciones filtradas.
     */
    private void mostrarAtenciones(
            List<Cita> atenciones
    ) {

        contenedorAtenciones
                .getChildren()
                .clear();

        if (
                atenciones == null
                        || atenciones.isEmpty()
        ) {

            lblCantidadResultados.setText(
                    "0"
            );

            mostrarMensaje(
                    historial.isEmpty()
                            ? "Todavía no existen atenciones médicas finalizadas."
                            : "No existen atenciones que coincidan con los filtros seleccionados."
            );

            return;
        }

        lblCantidadResultados.setText(
                String.valueOf(
                        atenciones.size()
                )
        );

        ocultarMensaje();

        for (Cita cita : atenciones) {

            contenedorAtenciones
                    .getChildren()
                    .add(
                            crearTarjetaAtencion(
                                    cita
                            )
                    );
        }
    }

    /**
     * Construye una tarjeta visual para
     * una atención finalizada.
     */
    private VBox crearTarjetaAtencion(
            Cita cita
    ) {

        VBox tarjeta =
                new VBox(
                        15
                );

        tarjeta.getStyleClass().add(
                "doctor-history-card"
        );

        tarjeta.setPadding(
                new Insets(
                        19,
                        21,
                        19,
                        21
                )
        );

        /* --------------------------------------------------------
                             ENCABEZADO
           -------------------------------------------------------- */

        HBox encabezado =
                new HBox(
                        14
                );

        encabezado.setAlignment(
                Pos.CENTER_LEFT
        );

        VBox bloqueFecha =
                new VBox(
                        3
                );

        Label lblFecha =
                new Label(
                        obtenerFecha(
                                cita
                        )
                );

        lblFecha.getStyleClass().add(
                "doctor-history-date"
        );

        Label lblHorario =
                new Label(
                        obtenerHorario(
                                cita
                        )
                );

        lblHorario.getStyleClass().add(
                "doctor-history-time"
        );

        bloqueFecha.getChildren().addAll(
                lblFecha,
                lblHorario
        );

        Region espacio =
                new Region();

        HBox.setHgrow(
                espacio,
                Priority.ALWAYS
        );

        Label lblEstadoAtencion =
                new Label(
                        "ATENDIDA"
                );

        lblEstadoAtencion
                .getStyleClass()
                .add(
                        "doctor-history-status"
                );

        encabezado.getChildren().addAll(
                bloqueFecha,
                espacio,
                lblEstadoAtencion
        );

        /* --------------------------------------------------------
                             PACIENTE
           -------------------------------------------------------- */

        VBox paciente =
                new VBox(
                        3
                );

        Label lblPacienteEtiqueta =
                new Label(
                        "PACIENTE"
                );

        lblPacienteEtiqueta
                .getStyleClass()
                .add(
                        "doctor-history-label"
                );

        Label lblPaciente =
                new Label(
                        valorSeguro(
                                cita.getNombrePaciente()
                        )
                );

        lblPaciente
                .getStyleClass()
                .add(
                        "doctor-history-patient"
                );

        Label lblEspecialidad =
                new Label(
                        valorSeguro(
                                cita.getNombreEspecialidad()
                        )
                );

        lblEspecialidad
                .getStyleClass()
                .add(
                        "doctor-history-specialty"
                );

        paciente.getChildren().addAll(
                lblPacienteEtiqueta,
                lblPaciente,
                lblEspecialidad
        );

        /* --------------------------------------------------------
                       ESTABLECIMIENTO Y MOTIVO
           -------------------------------------------------------- */

        HBox informacion =
                new HBox(
                        30
                );

        informacion.setAlignment(
                Pos.TOP_LEFT
        );

        VBox bloqueEstablecimiento =
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
                bloqueEstablecimiento,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                bloqueMotivo,
                Priority.ALWAYS
        );

        informacion.getChildren().addAll(
                bloqueEstablecimiento,
                bloqueMotivo
        );

        /* --------------------------------------------------------
                        OBSERVACIÓN MÉDICA
           -------------------------------------------------------- */

        VBox observacion =
                new VBox(
                        5
                );

        observacion
                .getStyleClass()
                .add(
                        "doctor-history-observation"
                );

        observacion.setPadding(
                new Insets(
                        12,
                        14,
                        12,
                        14
                )
        );

        Label lblObservacionTitulo =
                new Label(
                        "OBSERVACIÓN DE LA ATENCIÓN"
                );

        lblObservacionTitulo
                .getStyleClass()
                .add(
                        "doctor-history-observation-title"
                );

        Label lblObservacion =
                new Label(
                        valorSeguro(
                                cita.getObservacion()
                        )
                );

        lblObservacion.setWrapText(
                true
        );

        lblObservacion.setMaxWidth(
                Double.MAX_VALUE
        );

        lblObservacion
                .getStyleClass()
                .add(
                        "doctor-history-observation-text"
                );

        observacion.getChildren().addAll(
                lblObservacionTitulo,
                lblObservacion
        );

        tarjeta.getChildren().addAll(
                encabezado,
                paciente,
                informacion,
                observacion
        );

        return tarjeta;
    }

    /**
     * Crea un bloque pequeño de información.
     */
    private VBox crearBloqueInformacion(
            String titulo,
            String valor
    ) {

        VBox bloque =
                new VBox(
                        4
                );

        bloque.setMaxWidth(
                Double.MAX_VALUE
        );

        Label lblTitulo =
                new Label(
                        titulo
                );

        lblTitulo
                .getStyleClass()
                .add(
                        "doctor-history-label"
                );

        Label lblValor =
                new Label(
                        valorSeguro(
                                valor
                        )
                );

        lblValor.setWrapText(
                true
        );

        lblValor.setMaxWidth(
                Double.MAX_VALUE
        );

        lblValor
                .getStyleClass()
                .add(
                        "doctor-history-value"
                );

        bloque.getChildren().addAll(
                lblTitulo,
                lblValor
        );

        return bloque;
    }

    /**
     * Obtiene la fecha preparada para la interfaz.
     */
    private String obtenerFecha(
            Cita cita
    ) {

        if (
                cita == null
                        || cita.getFechaCita() == null
        ) {

            return "FECHA NO DISPONIBLE";
        }

        return cita
                .getFechaCita()
                .format(
                        formatoFecha
                )
                .toUpperCase();
    }

    /**
     * Obtiene el horario completo de la atención.
     */
    private String obtenerHorario(
            Cita cita
    ) {

        if (
                cita == null
                        || cita.getHoraInicio() == null
                        || cita.getHoraFin() == null
        ) {

            return "Horario no disponible";
        }

        return cita
                .getHoraInicio()
                .format(
                        formatoHora
                )
                + "  —  "
                + cita
                        .getHoraFin()
                        .format(
                                formatoHora
                        );
    }

    /**
     * Comprueba si un valor contiene el texto buscado.
     */
    private boolean contieneTexto(
            String valor,
            String texto
    ) {

        return valor != null
                && valor
                        .toLowerCase(
                                Locale.ROOT
                        )
                        .contains(
                                texto
                        );
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

        return valor.trim();
    }

    /* ============================================================
                              NAVEGACIÓN
       ============================================================ */

    /**
     * Abre la agenda del médico.
     */
    @FXML
    private void abrirAgenda() {

        cambiarPantalla(
                "/fxml/dashboard-medico.fxml"
        );
    }

    /**
     * Abre el perfil profesional.
     */
    @FXML
    private void abrirPerfil() {

        cambiarPantalla(
                "/fxml/perfil-medico.fxml"
        );
    }

    /**
     * Cierra la sesión.
     */
    @FXML
    private void cerrarSesion() {

        Stage stage =
                obtenerStage();

        if (stage == null) {

            mostrarMensaje(
                    "No fue posible identificar la ventana actual."
            );

            return;
        }

        try {

            SesionUtil.cerrarSesion(
                    stage
            );

        } catch (IOException excepcion) {

            mostrarMensaje(
                    "No fue posible cerrar la sesión."
            );

            excepcion.printStackTrace();
        }
    }

    /**
     * Cambia entre las pantallas
     * pertenecientes al Portal Médico.
     */
    private void cambiarPantalla(
            String rutaFXML
    ) {

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
                                    rutaFXML
                            )
                    );

            Parent root =
                    loader.load();

            Object controlador =
                    loader.getController();

            if (
                    controlador
                            instanceof DashboardMedicoController dashboard
            ) {

                dashboard.setUsuario(
                        usuarioActual
                );

            } else if (
                    controlador
                            instanceof PerfilMedicoController perfil
            ) {

                perfil.setUsuario(
                        usuarioActual
                );

            } else if (
                    controlador
                            instanceof HistorialAtencionesMedicoController historialController
            ) {

                historialController.setUsuario(
                        usuarioActual
                );
            }

            Stage stage =
                    obtenerStage();

            if (stage == null) {
                return;
            }

            stage.setScene(
                    new Scene(
                            root
                    )
            );

            stage.setTitle(
                    "MediAppoint - Portal médico"
            );

            stage.setMinWidth(
                    1050
            );

            stage.setMinHeight(
                    680
            );

            stage.centerOnScreen();

        } catch (IOException excepcion) {

            mostrarMensaje(
                    "No fue posible abrir la pantalla solicitada."
            );

            excepcion.printStackTrace();
        }
    }

    /**
     * Obtiene el Stage actual.
     */
    private Stage obtenerStage() {

        if (
                lblNombreMedico == null
                        || lblNombreMedico.getScene() == null
        ) {

            return null;
        }

        return (Stage)
                lblNombreMedico
                        .getScene()
                        .getWindow();
    }

    /* ============================================================
                         SEGUNDO PLANO
       ============================================================ */

    /**
     * Ejecuta consultas fuera del hilo JavaFX.
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

    /* ============================================================
                              MENSAJES
       ============================================================ */

    /**
     * Muestra un mensaje informativo.
     */
    private void mostrarMensaje(
            String mensaje
    ) {

        if (lblMensaje == null) {
            return;
        }

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

    /**
     * Oculta el mensaje informativo.
     */
    private void ocultarMensaje() {

        if (lblMensaje == null) {
            return;
        }

        lblMensaje.setText(
                ""
        );

        lblMensaje.setVisible(
                false
        );

        lblMensaje.setManaged(
                false
        );
    }

    /* ============================================================
                       RESULTADO DE CONSULTA
       ============================================================ */

    /**
     * Resultado obtenido desde MySQL.
     */
    private record DatosHistorial(
            Medico medico,
            List<Cita> atenciones
    ) {
    }
}