package com.citasmedicas.controller;

import com.citasmedicas.dao.PacienteDAO;
import com.citasmedicas.model.Cita;
import com.citasmedicas.model.Especialidad;
import com.citasmedicas.model.Establecimiento;
import com.citasmedicas.model.Medico;
import com.citasmedicas.model.Paciente;
import com.citasmedicas.model.Usuario;
import com.citasmedicas.service.CitaService;
import com.citasmedicas.service.EspecialidadService;
import com.citasmedicas.service.EstablecimientoService;
import com.citasmedicas.service.HorarioMedicoService;
import com.citasmedicas.service.MedicoService;
import com.citasmedicas.util.SesionUtil;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * ================================================================
 *                CONTROLLER - AGENDAR CITA
 * ================================================================
 *
 * Gestiona la interacción del paciente durante el proceso
 * de selección y registro de una nueva cita médica.
 *
 * Permite seleccionar especialidad, establecimiento,
 * médico, fecha y horario disponible.
 *
 * Antes de registrar una cita, el sistema vuelve a comprobar
 * la disponibilidad para evitar conflictos de horarios.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public class AgendarCitaController {

    @FXML
    private Label lblNombreUsuario;

    @FXML
    private ComboBox<Especialidad> cmbEspecialidad;

    @FXML
    private ComboBox<Establecimiento> cmbEstablecimiento;

    @FXML
    private ComboBox<Medico> cmbMedico;

    @FXML
    private DatePicker dpFecha;

    @FXML
    private ComboBox<LocalTime> cmbHorario;

    @FXML
    private TextArea txtMotivoConsulta;

    @FXML
    private Label lblMensaje;

    @FXML
    private Button btnConfirmar;

    /*
     * Servicios utilizados por la pantalla.
     */
    private final EspecialidadService especialidadService;
    private final EstablecimientoService establecimientoService;
    private final MedicoService medicoService;
    private final HorarioMedicoService horarioMedicoService;
    private final CitaService citaService;

    /*
     * DAO utilizado para obtener el perfil de paciente
     * asociado al usuario autenticado.
     */
    private final PacienteDAO pacienteDAO;

    /*
     * Usuario que mantiene la sesión actual.
     */
    private Usuario usuario;

    /*
     * Formato utilizado para mostrar los horarios.
     */
    private final DateTimeFormatter formatoHora =
            DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Constructor del controlador.
     */
    public AgendarCitaController() {

        this.especialidadService =
                new EspecialidadService();

        this.establecimientoService =
                new EstablecimientoService();

        this.medicoService =
                new MedicoService();

        this.horarioMedicoService =
                new HorarioMedicoService();

        this.citaService =
                new CitaService();

        this.pacienteDAO =
                new PacienteDAO();
    }

    /**
     * Inicializa la pantalla y configura los eventos
     * necesarios para los filtros dinámicos.
     */
    @FXML
    private void initialize() {

        configurarDatePicker();
        configurarFormatoHorario();
        configurarEventos();

        cargarEspecialidades();
        cargarEstablecimientos();

        actualizarEstadoConfirmacion();
    }

    /**
     * Recibe el usuario autenticado desde el Dashboard.
     *
     * @param usuario usuario que inició sesión.
     */
    public void setUsuario(Usuario usuario) {

        this.usuario = usuario;

        if (usuario != null) {

            lblNombreUsuario.setText(
                    usuario.getNombreCompleto()
            );
        }
    }

    /**
     * Carga las especialidades activas disponibles
     * desde la base de datos.
     */
    private void cargarEspecialidades() {

        try {

            List<Especialidad> especialidades =
                    especialidadService
                            .listarDisponibles();

            cmbEspecialidad.getItems().setAll(
                    especialidades
            );

        } catch (Exception e) {

            mostrarError(
                    "No fue posible cargar las especialidades."
            );

            e.printStackTrace();
        }
    }

    /**
     * Carga los establecimientos activos disponibles
     * desde la base de datos.
     */
    private void cargarEstablecimientos() {

        try {

            List<Establecimiento> establecimientos =
                    establecimientoService
                            .listarDisponibles();

            cmbEstablecimiento.getItems().setAll(
                    establecimientos
            );

        } catch (Exception e) {

            mostrarError(
                    "No fue posible cargar los establecimientos."
            );

            e.printStackTrace();
        }
    }

    /**
     * Configura los eventos de los controles.
     */
    private void configurarEventos() {

        /*
         * Si cambia la especialidad debemos volver
         * a consultar los médicos compatibles.
         */
        cmbEspecialidad.setOnAction(
                event -> actualizarMedicos()
        );

        /*
         * Si cambia el establecimiento también debemos
         * actualizar los médicos disponibles.
         */
        cmbEstablecimiento.setOnAction(
                event -> actualizarMedicos()
        );

        /*
         * Al seleccionar un médico se habilita
         * la selección de fecha.
         */
        cmbMedico.setOnAction(event -> {

            limpiarFechaYHorario();

            if (cmbMedico.getValue() != null) {
                dpFecha.setDisable(false);
            }

            actualizarEstadoConfirmacion();
        });

        /*
         * Cuando cambia la fecha se consultan nuevamente
         * los horarios disponibles.
         */
        dpFecha.setOnAction(event -> {

            cargarHorariosDisponibles();
            actualizarEstadoConfirmacion();
        });

        /*
         * Actualiza el botón cuando cambia el horario.
         */
        cmbHorario.setOnAction(
                event -> actualizarEstadoConfirmacion()
        );

        /*
         * Actualiza el botón cuando cambia el motivo.
         */
        txtMotivoConsulta
                .textProperty()
                .addListener(
                        (observable, anterior, nuevo) ->
                                actualizarEstadoConfirmacion()
                );
    }

    /**
     * Consulta los médicos disponibles según la combinación
     * de especialidad y establecimiento seleccionados.
     */
    private void actualizarMedicos() {

        cmbMedico.getItems().clear();
        cmbMedico.setValue(null);

        limpiarFechaYHorario();

        Especialidad especialidad =
                cmbEspecialidad.getValue();

        Establecimiento establecimiento =
                cmbEstablecimiento.getValue();

        if (
                especialidad == null
                || establecimiento == null
        ) {

            cmbMedico.setDisable(true);

            actualizarEstadoConfirmacion();

            return;
        }

        try {

            List<Medico> medicos =
                    medicoService.listarDisponibles(
                            especialidad
                                    .getIdEspecialidad(),
                            establecimiento
                                    .getIdEstablecimiento()
                    );

            cmbMedico.getItems().setAll(
                    medicos
            );

            cmbMedico.setDisable(
                    medicos.isEmpty()
            );

            if (medicos.isEmpty()) {

                mostrarError(
                        "No existen médicos disponibles para "
                        + "la especialidad y establecimiento "
                        + "seleccionados."
                );

            } else {

                limpiarMensaje();
            }

        } catch (Exception e) {

            cmbMedico.setDisable(true);

            mostrarError(
                    "No fue posible consultar los médicos."
            );

            e.printStackTrace();
        }

        actualizarEstadoConfirmacion();
    }

    /**
     * Consulta los horarios realmente disponibles para
     * el médico, establecimiento y fecha seleccionados.
     */
    private void cargarHorariosDisponibles() {

        cmbHorario.getItems().clear();
        cmbHorario.setValue(null);
        cmbHorario.setDisable(true);

        Medico medico =
                cmbMedico.getValue();

        Establecimiento establecimiento =
                cmbEstablecimiento.getValue();

        LocalDate fecha =
                dpFecha.getValue();

        if (
                medico == null
                || establecimiento == null
                || fecha == null
        ) {

            actualizarEstadoConfirmacion();

            return;
        }

        try {

            List<LocalTime> horarios =
                    horarioMedicoService
                            .obtenerBloquesDisponibles(
                                    medico.getIdMedico(),
                                    establecimiento
                                            .getIdEstablecimiento(),
                                    fecha
                            );
                            /*
 * Si la cita es para hoy, eliminamos del listado
 * todas las horas que ya pasaron.
 */
if (
        fecha.isEqual(
                LocalDate.now()
        )
) {

    LocalTime ahora =
            LocalTime.now();

    horarios =
            horarios
                    .stream()
                    .filter(
                            hora ->
                                    hora != null
                                            && hora.isAfter(
                                                    ahora
                                            )
                    )
                    .toList();
}

            cmbHorario.getItems().setAll(
                    horarios
            );

            cmbHorario.setDisable(
                    horarios.isEmpty()
            );

            if (horarios.isEmpty()) {

                mostrarError(
                        "No existen horarios disponibles "
                        + "para la fecha seleccionada."
                );

            } else {

                limpiarMensaje();
            }

        } catch (Exception e) {

            cmbHorario.setDisable(true);

            mostrarError(
                    "No fue posible consultar la disponibilidad."
            );

            e.printStackTrace();
        }

        actualizarEstadoConfirmacion();
    }

    /**
 * Configura el calendario de agendamiento.
 *
 * La fecha únicamente puede seleccionarse mediante
 * el calendario y nunca puede ser anterior al día actual.
 */
private void configurarDatePicker() {

    /*
     * Impide escribir texto manualmente.
     */
    dpFecha.setEditable(false);

    dpFecha.setDayCellFactory(
            selector -> new DateCell() {

                @Override
                public void updateItem(
                        LocalDate fecha,
                        boolean vacio
                ) {

                    super.updateItem(
                            fecha,
                            vacio
                    );

                    if (
                            vacio
                                    || fecha == null
                    ) {

                        return;
                    }

                    setDisable(
                            fecha.isBefore(
                                    LocalDate.now()
                            )
                    );
                }
            }
    );
}

    /**
     * Configura el formato HH:mm utilizado en el
     * ComboBox de horarios.
     */
    private void configurarFormatoHorario() {

        cmbHorario.setConverter(
                new StringConverter<>() {

                    @Override
                    public String toString(
                            LocalTime hora
                    ) {

                        if (hora == null) {
                            return "";
                        }

                        return hora.format(
                                formatoHora
                        );
                    }

                    @Override
                    public LocalTime fromString(
                            String texto
                    ) {

                        if (
                                texto == null
                                || texto.isBlank()
                        ) {

                            return null;
                        }

                        return LocalTime.parse(
                                texto,
                                formatoHora
                        );
                    }
                }
        );
    }

    /**
     * Limpia la fecha y los horarios cuando cambia
     * el médico, la especialidad o el establecimiento.
     */
    private void limpiarFechaYHorario() {

        dpFecha.setValue(null);
        dpFecha.setDisable(true);

        cmbHorario.getItems().clear();
        cmbHorario.setValue(null);
        cmbHorario.setDisable(true);
    }

    /**
     * Habilita el botón Confirmar únicamente cuando
     * todos los datos obligatorios están completos.
     */
    private void actualizarEstadoConfirmacion() {

        boolean formularioCompleto =
                cmbEspecialidad.getValue() != null
                && cmbEstablecimiento.getValue() != null
                && cmbMedico.getValue() != null
                && dpFecha.getValue() != null
                && cmbHorario.getValue() != null
                && txtMotivoConsulta.getText() != null
                && !txtMotivoConsulta
                        .getText()
                        .trim()
                        .isEmpty();

        btnConfirmar.setDisable(
                !formularioCompleto
        );
    }

    /**
     * Registra la cita médica seleccionada.
     */
    @FXML
    private void confirmarCita() {

        if (usuario == null) {

            mostrarError(
                    "No existe un usuario autenticado."
            );

            return;
        }

        Especialidad especialidad =
                cmbEspecialidad.getValue();

        Establecimiento establecimiento =
                cmbEstablecimiento.getValue();

        Medico medico =
                cmbMedico.getValue();

        LocalDate fecha =
                dpFecha.getValue();

        LocalTime horaInicio =
                cmbHorario.getValue();

        String motivo =
                txtMotivoConsulta.getText() == null
                        ? ""
                        : txtMotivoConsulta
                                .getText()
                                .trim();

        /*
         * Validación adicional del formulario.
         */
        if (
                especialidad == null
                || establecimiento == null
                || medico == null
                || fecha == null
                || horaInicio == null
                || motivo.isEmpty()
        ) {

            mostrarError(
                    "Complete todos los datos obligatorios."
            );

            return;
        }

        try {

            /*
             * Busca el paciente relacionado con el
             * usuario que inició sesión.
             */
            Paciente paciente =
                    pacienteDAO.buscarPorIdUsuario(
                            usuario.getIdUsuario()
                    );

            if (paciente == null) {

                mostrarError(
                        "No se encontró el perfil de paciente "
                        + "asociado a este usuario."
                );

                return;
            }

            /*
             * Calculamos la hora final del bloque.
             *
             * Actualmente los horarios configurados para
             * Carlos son de 30 minutos.
             */
            LocalTime horaFin =
                    horaInicio.plusMinutes(30);

            /*
             * El Service vuelve a comprobar que el horario
             * continúe disponible antes del INSERT.
             */
            Cita cita =
                    citaService.agendarCita(
                            paciente.getIdPaciente(),
                            medico.getIdMedico(),
                            especialidad.getIdEspecialidad(),
                            establecimiento.getIdEstablecimiento(),
                            fecha,
                            horaInicio,
                            horaFin,
                            motivo
                    );

            /*
             * Mostramos la confirmación al paciente.
             */
            /*
 * Limpiamos el motivo de consulta.
 */
txtMotivoConsulta.clear();

/*
 * Actualizamos los horarios disponibles.
 *
 * El horario recién utilizado debe desaparecer
 * inmediatamente del ComboBox.
 */
cargarHorariosDisponibles();

/*
 * Finalmente mostramos el mensaje de éxito.
 *
 * Se realiza después de actualizar los horarios
 * para evitar que cargarHorariosDisponibles()
 * borre el mensaje.
 */
mostrarExito(
        "Cita agendada correctamente. "
        + "Código de cita: "
        + cita.getIdCita()
);

actualizarEstadoConfirmacion();

        } catch (IllegalArgumentException e) {

            mostrarError(
                    e.getMessage()
            );

            /*
             * Si otro proceso utilizó el horario antes de
             * confirmar, actualizamos la disponibilidad.
             */
            cargarHorariosDisponibles();

        } catch (Exception e) {

            mostrarError(
                    "No fue posible registrar la cita."
            );

            System.err.println(
                    "Error al registrar cita: "
                    + e.getMessage()
            );

            e.printStackTrace();
        }
    }

    /**
     * Muestra un mensaje de error.
     */
    private void mostrarError(String mensaje) {

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
    private void mostrarExito(String mensaje) {

        lblMensaje.setText(
                mensaje
        );

        lblMensaje.setStyle(
                "-fx-text-fill: #45D483;"
        );
    }

    /**
     * Limpia el mensaje mostrado al usuario.
     */
    private void limpiarMensaje() {

        lblMensaje.setText("");
    }

    /**
     * Regresa al panel principal conservando
     * el usuario autenticado.
     */
    @FXML
    private void volverDashboard() {

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

            /*
             * Conservamos la sesión del usuario.
             */
            controller.setUsuario(
                    usuario
            );

            Stage stage =
                    (Stage) lblNombreUsuario
                            .getScene()
                            .getWindow();

            stage.setScene(scene);

            stage.setTitle(
                    "MediAppoint - Panel principal"
            );

            stage.setMinWidth(950);
            stage.setMinHeight(620);

            stage.centerOnScreen();

        } catch (IOException e) {

            mostrarError(
                    "No fue posible regresar al panel principal."
            );

            e.printStackTrace();
        }
    }

    /**
 * Abre la pantalla Mis citas conservando
 * el usuario autenticado.
 */
@FXML
private void abrirMisCitas() {

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

        /*
         * Conservamos la sesión del usuario
         * al cambiar de pantalla.
         */
        controller.setUsuario(
                usuario
        );

        Stage stage =
                (Stage) lblNombreUsuario
                        .getScene()
                        .getWindow();

        stage.setScene(
                scene
        );

        stage.setTitle(
                "MediAppoint - Mis citas"
        );

        stage.setMinWidth(
                950
        );

        stage.setMinHeight(
                620
        );

        stage.centerOnScreen();

    } catch (IOException e) {

        mostrarError(
                "No fue posible abrir Mis citas."
        );

        System.err.println(
                "Error al cargar mis-citas.fxml: "
                        + e.getMessage()
        );

        e.printStackTrace();
    }
}

/**
 * Abre la pantalla Mi perfil conservando
 * el usuario autenticado.
 */
@FXML
private void abrirPerfil() {

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

        /*
         * Conservamos la sesión del usuario
         * al cambiar de pantalla.
         */
        controller.setUsuario(
                usuario
        );

        Stage stage =
                (Stage) lblNombreUsuario
                        .getScene()
                        .getWindow();

        stage.setScene(
                scene
        );

        stage.setTitle(
                "MediAppoint - Mi perfil"
        );

        stage.setMinWidth(
                950
        );

        stage.setMinHeight(
                620
        );

        stage.centerOnScreen();

    } catch (IOException e) {

        mostrarError(
                "No fue posible abrir Mi perfil."
        );

        System.err.println(
                "Error al cargar perfil.fxml: "
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

        /*
         * Obtenemos la ventana actual antes
         * de eliminar la referencia al usuario.
         */
        Stage stage =
                (Stage) lblNombreUsuario
                        .getScene()
                        .getWindow();

        /*
         * Eliminamos la referencia mantenida
         * por este controlador.
         */
        usuario = null;

        /*
         * Regresamos al Login.
         */
        SesionUtil.cerrarSesion(stage);

    } catch (IOException e) {

        mostrarError(
                "No fue posible cerrar la sesión."
        );

        System.err.println(
                "Error al cerrar sesión desde Agendar cita: "
                + e.getMessage()
        );

        e.printStackTrace();
    }
}
}