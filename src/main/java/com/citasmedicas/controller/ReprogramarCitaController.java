package com.citasmedicas.controller;

import com.citasmedicas.dao.PacienteDAO;
import com.citasmedicas.model.Cita;
import com.citasmedicas.model.Paciente;
import com.citasmedicas.model.Usuario;
import com.citasmedicas.service.CitaService;
import com.citasmedicas.service.HorarioMedicoService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * ================================================================
 *              CONTROLLER - REPROGRAMAR CITA
 * ================================================================
 *
 * Gestiona la selección de una nueva fecha y horario
 * para una cita médica existente.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public class ReprogramarCitaController {

    @FXML
    private Label lblNombreUsuario;

    @FXML
    private Label lblEspecialidad;

    @FXML
    private Label lblMedico;

    @FXML
    private Label lblEstablecimiento;

    @FXML
    private Label lblCitaActual;

    @FXML
    private Label lblMensaje;

    @FXML
    private DatePicker dpNuevaFecha;

    @FXML
    private VBox contenedorHorarios;

    private final HorarioMedicoService horarioMedicoService;
    private final CitaService citaService;
    private final PacienteDAO pacienteDAO;

    private Usuario usuarioActual;
    private Cita citaActual;

    private LocalTime horaSeleccionadaInicio;
    private LocalTime horaSeleccionadaFin;

    private Button botonHorarioSeleccionado;

    private final DateTimeFormatter formatoFecha =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final DateTimeFormatter formatoHora =
            DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Constructor.
     */
    public ReprogramarCitaController() {

        this.horarioMedicoService =
                new HorarioMedicoService();

        this.citaService =
                new CitaService();

        this.pacienteDAO =
                new PacienteDAO();
    }

    /**
     * Configura las restricciones básicas del selector de fecha.
     */
    @FXML
    private void initialize() {

        dpNuevaFecha.setDayCellFactory(
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
                                fecha != null
                                && fecha.isBefore(
                                        LocalDate.now()
                                )
                        ) {

                            setDisable(true);
                        }
                    }
                }
        );
    }

    /**
     * Recibe el usuario autenticado y la cita seleccionada.
     */
    public void configurar(
            Usuario usuario,
            Cita cita
    ) {

        this.usuarioActual =
                usuario;

        this.citaActual =
                cita;

        cargarInformacion();

        /*
         * Cargamos inmediatamente los horarios de la fecha
         * actual de la cita.
         */
        cargarHorariosDisponibles();
    }

    /**
     * Muestra los datos de la cita seleccionada.
     */
    private void cargarInformacion() {

        if (
                usuarioActual == null
                || citaActual == null
        ) {

            mostrarError(
                    "No fue posible cargar la información de la cita."
            );

            return;
        }

        lblNombreUsuario.setText(
                usuarioActual.getNombreCompleto()
        );

        lblEspecialidad.setText(
                citaActual.getNombreEspecialidad()
        );

        lblMedico.setText(
                citaActual.getNombreMedico()
        );

        lblEstablecimiento.setText(
                citaActual.getNombreEstablecimiento()
        );

        lblCitaActual.setText(
                citaActual.getFechaCita()
                        .format(formatoFecha)
                        + " · "
                        + citaActual.getHoraInicio()
                        .format(formatoHora)
                        + " - "
                        + citaActual.getHoraFin()
                        .format(formatoHora)
        );

        dpNuevaFecha.setValue(
                citaActual.getFechaCita()
        );
    }

    /**
     * Consulta y muestra los horarios disponibles
     * para la fecha seleccionada.
     */
    @FXML
    private void cargarHorariosDisponibles() {

        horaSeleccionadaInicio = null;
        horaSeleccionadaFin = null;
        botonHorarioSeleccionado = null;

        contenedorHorarios
                .getChildren()
                .clear();

        if (citaActual == null) {
            return;
        }

        LocalDate fecha =
                dpNuevaFecha.getValue();

        if (fecha == null) {

            mostrarError(
                    "Debe seleccionar una fecha."
            );

            return;
        }

        if (fecha.isBefore(LocalDate.now())) {

            mostrarError(
                    "No puede seleccionar una fecha anterior."
            );

            return;
        }

        try {

            List<LocalTime> horarios =
                    horarioMedicoService
                            .obtenerBloquesDisponiblesParaReprogramacion(
                                    citaActual.getIdCita(),
                                    citaActual.getIdMedico(),
                                    citaActual.getIdEstablecimiento(),
                                    fecha
                            );

            if (horarios.isEmpty()) {

                mostrarError(
                        "No existen horarios disponibles "
                        + "para la fecha seleccionada."
                );

                return;
            }

            FlowPane panelHorarios =
                    new FlowPane();

            panelHorarios.setHgap(10);
            panelHorarios.setVgap(10);

            panelHorarios.setPadding(
                    new Insets(
                            8,
                            8,
                            8,
                            8
                    )
            );

            for (LocalTime hora : horarios) {

                Button boton =
                        crearBotonHorario(
                                hora
                        );

                panelHorarios
                        .getChildren()
                        .add(
                                boton
                        );
            }

            contenedorHorarios
                    .getChildren()
                    .add(
                            panelHorarios
                    );

            limpiarMensaje();

        } catch (Exception e) {

            mostrarError(
                    "No fue posible consultar los horarios disponibles."
            );

            System.err.println(
                    "Error al consultar horarios para reprogramación: "
                    + e.getMessage()
            );

            e.printStackTrace();
        }
    }

    /**
     * Crea un botón correspondiente a un horario disponible.
     */
    private Button crearBotonHorario(
            LocalTime horaInicio
    ) {

        Button boton =
                new Button(
                        horaInicio.format(
                                formatoHora
                        )
                );

        boton.getStyleClass().add(
                "primary-button"
        );

        boton.setPrefWidth(90);

        boton.setOnAction(event -> {

            horaSeleccionadaInicio =
                    horaInicio;

            /*
             * Actualmente los bloques configurados son
             * de 30 minutos, igual que en Agendar cita.
             */
            horaSeleccionadaFin =
                    horaInicio.plusMinutes(30);

            botonHorarioSeleccionado =
                    boton;

            mostrarMensaje(
                    "Horario seleccionado: "
                    + horaSeleccionadaInicio
                            .format(formatoHora)
                    + " - "
                    + horaSeleccionadaFin
                            .format(formatoHora)
            );
        });

        return boton;
    }

    /**
     * Confirma la modificación de fecha y horario.
     */
    @FXML
    private void confirmarReprogramacion() {

        if (
                usuarioActual == null
                || citaActual == null
        ) {

            mostrarError(
                    "No existe una cita válida para reprogramar."
            );

            return;
        }

        LocalDate nuevaFecha =
                dpNuevaFecha.getValue();

        if (
                nuevaFecha == null
                || horaSeleccionadaInicio == null
                || horaSeleccionadaFin == null
        ) {

            mostrarError(
                    "Seleccione una fecha y un horario disponible."
            );

            return;
        }

        /*
         * Evitamos una actualización innecesaria si el
         * paciente selecciona exactamente la misma cita.
         */
        if (
                nuevaFecha.equals(
                        citaActual.getFechaCita()
                )
                && horaSeleccionadaInicio.equals(
                        citaActual.getHoraInicio()
                )
        ) {

            mostrarError(
                    "Seleccione una fecha u horario diferente "
                    + "al de la cita actual."
            );

            return;
        }

        Alert confirmacion =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        confirmacion.setTitle(
                "Confirmar reprogramación"
        );

        confirmacion.setHeaderText(
                "¿Deseas reprogramar esta cita?"
        );

        confirmacion.setContentText(
                "Nueva fecha: "
                + nuevaFecha.format(formatoFecha)
                + "\nNuevo horario: "
                + horaSeleccionadaInicio.format(formatoHora)
                + " - "
                + horaSeleccionadaFin.format(formatoHora)
        );

        Optional<ButtonType> respuesta =
                confirmacion.showAndWait();

        if (
                respuesta.isEmpty()
                || respuesta.get() != ButtonType.OK
        ) {

            return;
        }

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

            citaService.reprogramarCita(
                    citaActual.getIdCita(),
                    paciente.getIdPaciente(),
                    citaActual.getIdMedico(),
                    citaActual.getIdEstablecimiento(),
                    nuevaFecha,
                    horaSeleccionadaInicio,
                    horaSeleccionadaFin
            );

            Alert exito =
                    new Alert(
                            Alert.AlertType.INFORMATION
                    );

            exito.setTitle(
                    "Cita reprogramada"
            );

            exito.setHeaderText(
                    "La cita fue reprogramada correctamente."
            );

            exito.setContentText(
                    nuevaFecha.format(formatoFecha)
                    + " · "
                    + horaSeleccionadaInicio.format(formatoHora)
                    + " - "
                    + horaSeleccionadaFin.format(formatoHora)
            );

            exito.showAndWait();

            volverMisCitas();

        } catch (IllegalArgumentException e) {

            mostrarError(
                    e.getMessage()
            );

            /*
             * Si el horario fue ocupado entre la selección
             * y la confirmación, actualizamos la lista.
             */
            cargarHorariosDisponibles();

        } catch (Exception e) {

            mostrarError(
                    "No fue posible reprogramar la cita."
            );

            System.err.println(
                    "Error al reprogramar cita: "
                    + e.getMessage()
            );

            e.printStackTrace();
        }
    }

    /**
     * Regresa a Mis citas.
     */
    @FXML
    private void volverMisCitas() {

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

            mostrarError(
                    "No fue posible regresar a Mis citas."
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
     * Limpia los mensajes.
     */
    private void limpiarMensaje() {

        lblMensaje.setText("");
    }
}