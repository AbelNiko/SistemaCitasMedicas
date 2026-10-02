package com.citasmedicas.controller;

import com.citasmedicas.util.EjecutorTareas;
import com.citasmedicas.dao.PacienteDAO;
import com.citasmedicas.model.Cita;
import com.citasmedicas.model.Paciente;
import com.citasmedicas.model.Usuario;
import com.citasmedicas.service.CitaService;
import com.citasmedicas.util.SesionUtil;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
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
 * También administra la navegación hacia:
 * - Agendar cita.
 * - Mis citas.
 * - Historial de atenciones.
 * - Mi perfil.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.1
 */
public class DashboardController {
/**
 * Resultado interno utilizado para transportar
 * la información calculada desde el hilo de trabajo
 * hasta JavaFX.
 */
private record ResumenDashboard(
        boolean pacienteEncontrado,
        long cantidadCitas,
        Cita proximaCita
) {
}

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

    /**
     * Constructor principal.
     */
    public DashboardController() {

        this.pacienteDAO =
                new PacienteDAO();

        this.citaService =
                new CitaService();
    }

    /**
     * Recibe el usuario autenticado.
     *
     * @param usuario usuario autenticado.
     */
    public void setUsuario(
            Usuario usuario
    ) {

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
 * Carga el resumen de citas del paciente en segundo plano.
 *
 * Se realiza una sola consulta de citas y, posteriormente,
 * se calcula en memoria:
 *
 * - cantidad de citas activas;
 * - próxima cita disponible.
 *
 * De esta forma evitamos bloquear JavaFX y reducimos
 * consultas innecesarias hacia Aiven.
 */
private void cargarResumenCitas() {

    if (usuarioActual == null) {
        return;
    }

    /*
     * Mostramos inmediatamente un estado de carga.
     * La interfaz continúa respondiendo mientras
     * MySQL procesa la consulta.
     */
    lblProximaCita.setText(
            "Cargando..."
    );

    lblDetalleProximaCita.setText(
            "Consultando tus próximas citas"
    );

    lblCantidadCitas.setText(
            "..."
    );

    int idUsuario =
            usuarioActual.getIdUsuario();

    Task<ResumenDashboard> tarea =
            new Task<>() {

                @Override
                protected ResumenDashboard call()
                        throws Exception {

                    /*
                     * =================================================
                     * PERFIL DEL PACIENTE
                     * =================================================
                     */

                    Paciente paciente =
                            pacienteDAO.buscarPorIdUsuario(
                                    idUsuario
                            );

                    if (paciente == null) {

                        return new ResumenDashboard(
                                false,
                                0,
                                null
                        );
                    }

                    /*
                     * =================================================
                     * UNA SOLA CONSULTA DE CITAS
                     * =================================================
                     */

                    List<Cita> citas =
                            citaService.listarPorPaciente(
                                    paciente.getIdPaciente()
                            );

                    LocalDate hoy =
                            LocalDate.now();

                    LocalTime ahora =
                            LocalTime.now();

                    long cantidadActivas =
                            0;

                    Cita proximaCita =
                            null;

                    /*
                     * CitaDAO ya devuelve las citas ordenadas
                     * por fecha y hora.
                     *
                     * Aprovechamos ese orden para encontrar
                     * la próxima cita durante el mismo recorrido.
                     */
                    for (Cita cita : citas) {

                        if (
                                cita == null
                                        || cita.getFechaCita() == null
                        ) {

                            continue;
                        }

                        boolean activa =
                                "PROGRAMADA".equalsIgnoreCase(
                                        cita.getEstado()
                                )
                                        || "CONFIRMADA".equalsIgnoreCase(
                                                cita.getEstado()
                                        );

                        if (!activa) {
                            continue;
                        }

                        /*
                         * Conservamos la misma regla que tenía
                         * contarCitasProgramadas():
                         *
                         * hoy o una fecha futura.
                         */
                        if (
                                !cita.getFechaCita()
                                        .isBefore(hoy)
                        ) {

                            cantidadActivas++;
                        }

                        /*
                         * Si ya encontramos la próxima cita,
                         * no necesitamos volver a calcularla,
                         * aunque seguimos recorriendo para contar.
                         */
                        if (proximaCita != null) {
                            continue;
                        }

                        if (
                                cita.getFechaCita()
                                        .isAfter(hoy)
                        ) {

                            proximaCita =
                                    cita;

                            continue;
                        }

                        if (
                                cita.getFechaCita()
                                        .isEqual(hoy)
                                && cita.getHoraInicio() != null
                                && !cita
                                        .getHoraInicio()
                                        .isBefore(ahora)
                        ) {

                            proximaCita =
                                    cita;
                        }
                    }

                    return new ResumenDashboard(
                            true,
                            cantidadActivas,
                            proximaCita
                    );
                }
            };

    /*
     * ============================================================
     * RESULTADO CORRECTO
     * ============================================================
     */

    tarea.setOnSucceeded(
            evento -> {

                ResumenDashboard resumen =
                        tarea.getValue();

                if (
                        resumen == null
                                || !resumen.pacienteEncontrado()
                ) {

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

                lblCantidadCitas.setText(
                        String.valueOf(
                                resumen.cantidadCitas()
                        )
                );

                Cita cita =
                        resumen.proximaCita();

                if (cita == null) {

                    lblProximaCita.setText(
                            "Sin citas próximas"
                    );

                    lblDetalleProximaCita.setText(
                            "Agenda una nueva cita médica"
                    );

                    return;
                }

                /*
                 * =================================================
                 * FECHA Y HORA
                 * =================================================
                 */

                String fechaHora =
                        cita.getFechaCita()
                                .format(formatoFecha);

                if (cita.getHoraInicio() != null) {

                    fechaHora +=
                            " · "
                                    + cita
                                            .getHoraInicio()
                                            .format(
                                                    formatoHora
                                            );
                }

                lblProximaCita.setText(
                        fechaHora
                );

                /*
                 * =================================================
                 * DETALLE
                 * =================================================
                 */

                String especialidad =
                        valorSeguro(
                                cita.getNombreEspecialidad(),
                                "Especialidad no disponible"
                        );

                String medico =
                        valorSeguro(
                                cita.getNombreMedico(),
                                "Médico no disponible"
                        );

                lblDetalleProximaCita.setText(
                        especialidad
                                + " · "
                                + medico
                );
            }
    );

    /*
     * ============================================================
     * ERROR
     * ============================================================
     */

    tarea.setOnFailed(
            evento -> {

                lblProximaCita.setText(
                        "No disponible"
                );

                lblDetalleProximaCita.setText(
                        "No fue posible consultar tus citas."
                );

                lblCantidadCitas.setText(
                        "-"
                );

                Throwable error =
                        tarea.getException();

                if (error != null) {

                    System.err.println(
                            "Error cargando resumen del Dashboard: "
                                    + error.getMessage()
                    );
                }
            }
    );

    /*
     * Utilizamos el mismo ejecutor compartido
     * que ya emplea el Login.
     */
    EjecutorTareas.ejecutar(
            tarea
    );
}

/**
 * Evita mostrar valores null o vacíos
 * dentro del Dashboard.
 */
private String valorSeguro(
        String valor,
        String predeterminado
) {

    if (
            valor == null
                    || valor.isBlank()
    ) {

        return predeterminado;
    }

    return valor.trim();
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

            cambiarEscena(
                    scene,
                    "MediAppoint - Agendar cita"
            );

        } catch (IOException e) {

            registrarErrorNavegacion(
                    "agendamiento",
                    e
            );
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

            cambiarEscena(
                    scene,
                    "MediAppoint - Mis citas"
            );

        } catch (IOException e) {

            registrarErrorNavegacion(
                    "Mis citas",
                    e
            );
        }
    }

    /**
     * Abre el historial de atenciones médicas
     * del paciente autenticado.
     */
    @FXML
    private void abrirHistorial() {

        if (usuarioActual == null) {
            return;
        }

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/historial-paciente.fxml"
                            )
                    );

            Scene scene =
                    new Scene(
                            loader.load()
                    );

            HistorialPacienteController controller =
                    loader.getController();

            controller.setUsuario(
                    usuarioActual
            );

            cambiarEscena(
                    scene,
                    "MediAppoint - Historial de atenciones"
            );

        } catch (IOException e) {

            registrarErrorNavegacion(
                    "Historial de atenciones",
                    e
            );
        }
    }

    /**
     * Abre el perfil del paciente autenticado.
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

            registrarErrorNavegacion(
                    "Mi perfil",
                    e
            );
        }
    }

    /**
     * Cambia la escena de la ventana principal.
     *
     * Centraliza la configuración de tamaño y título
     * utilizada durante la navegación del paciente.
     *
     * @param scene escena que será mostrada.
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

        stage.setMinWidth(
                950
        );

        stage.setMinHeight(
                620
        );

        stage.centerOnScreen();
    }

    /**
     * Registra en consola un error ocurrido
     * durante la navegación.
     *
     * @param pantalla nombre de la pantalla.
     * @param e excepción generada.
     */
    private void registrarErrorNavegacion(
            String pantalla,
            IOException e
    ) {

        System.err.println(
                "No fue posible abrir "
                        + pantalla
                        + ": "
                        + e.getMessage()
        );

        e.printStackTrace();
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
                    "Error al cerrar sesión: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }
    }
}