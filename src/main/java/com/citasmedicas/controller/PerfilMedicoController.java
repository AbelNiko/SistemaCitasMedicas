package com.citasmedicas.controller;

import com.citasmedicas.dao.MedicoDAO;
import com.citasmedicas.model.Medico;
import com.citasmedicas.model.Usuario;
import com.citasmedicas.model.PreguntaSeguridad;
import com.citasmedicas.util.SesionUtil;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.stage.Modality;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * ================================================================
 *                CONTROLADOR - PERFIL MÉDICO
 * ================================================================
 *
 * Gestiona la pantalla de consulta del perfil profesional
 * del médico autenticado.
 *
 * La información mostrada es de solo lectura.
 *
 * Permite consultar:
 * - información personal;
 * - información de contacto;
 * - registro profesional;
 * - especialidades asignadas;
 * - establecimientos asignados;
 * - estado profesional;
 * - observación administrativa;
 * - fecha de registro.
 *
 * Las consultas a MySQL se ejecutan en segundo plano para evitar
 * bloquear el JavaFX Application Thread.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.3
 */
public class PerfilMedicoController {

    /* ============================================================
                             CONTROLES FXML
       ============================================================ */

    @FXML
    private Label lblNombreMedico;

    @FXML
    private Label lblIniciales;

    @FXML
    private Label lblNombreHero;

    @FXML
    private Label lblNombreCompleto;

    @FXML
    private Label lblCedula;

    @FXML
    private Label lblCorreo;

    @FXML
    private Label lblTelefono;

    @FXML
    private Label lblCedulaProfesional;

    @FXML
    private Label lblEspecialidades;

    @FXML
    private Label lblEstablecimientos;

    @FXML
    private Label lblEstado;

    @FXML
    private Label lblObservacion;

    @FXML
    private Label lblFechaRegistro;

    @FXML
    private Label lblMensaje;

    /*
 * ============================================================
 * SEGURIDAD DE LA CUENTA
 * ============================================================
 */

@FXML
private Label lblEstadoSeguridadMedico;

@FXML
private Label lblPreguntaSeguridadMedico;

@FXML
private Button btnSeguridadMedico;

    /* ============================================================
                         DEPENDENCIAS Y ESTADO
       ============================================================ */

    private final MedicoDAO medicoDAO;

    private Usuario usuarioActual;
    private Medico medicoActual;

    private final DateTimeFormatter formatoFecha =
            DateTimeFormatter.ofPattern(
                    "dd 'de' MMMM 'de' yyyy",
                    new Locale("es", "EC")
            );

    /**
     * Constructor principal.
     */
    public PerfilMedicoController() {

        this.medicoDAO =
                new MedicoDAO();
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

        String nombre =
                valorSeguro(
                        usuarioActual.getNombreCompleto()
                );

        /*
         * Mostramos inmediatamente la información
         * disponible en la sesión.
         */
        lblNombreMedico.setText(
                nombre
        );

        if (lblNombreHero != null) {

            lblNombreHero.setText(
                    nombre
            );
        }

        if (lblIniciales != null) {

            lblIniciales.setText(
                    obtenerIniciales(
                            nombre
                    )
            );
        }

        cargarPerfil();

actualizarEstadoSeguridad();
    }

    /* ============================================================
                         CARGA DEL PERFIL
       ============================================================ */

    /**
     * Consulta la información profesional del médico
     * utilizando un hilo secundario.
     */
    private void cargarPerfil() {

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
                "Cargando información profesional..."
        );

        int idUsuario =
                usuarioActual.getIdUsuario();

        Task<DatosPerfilMedico> tarea =
                new Task<>() {

                    @Override
                    protected DatosPerfilMedico call()
                            throws Exception {

                        Medico medico =
                                medicoDAO.buscarPorIdUsuario(
                                        idUsuario
                                );

                        if (medico == null) {

                            return new DatosPerfilMedico(
                                    null,
                                    List.of(),
                                    List.of()
                            );
                        }

                        List<String> especialidades =
                                medicoDAO.listarEspecialidadesPorMedico(
                                        medico.getIdMedico()
                                );

                        List<String> establecimientos =
                                medicoDAO.listarEstablecimientosPorMedico(
                                        medico.getIdMedico()
                                );

                        return new DatosPerfilMedico(
                                medico,
                                especialidades,
                                establecimientos
                        );
                    }
                };

        tarea.setOnSucceeded(
                evento -> {

                    DatosPerfilMedico datos =
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

                    cargarDatosPersonales();

                    cargarDatosProfesionales(
                            datos.especialidades(),
                            datos.establecimientos()
                    );

                    ocultarMensaje();
                }
        );

        tarea.setOnFailed(
                evento -> {

                    Throwable error =
                            tarea.getException();

                    mostrarMensaje(
                            "No fue posible cargar la información del perfil."
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
                "cargar-perfil-profesional"
        );
    }

    /* ============================================================
                       INFORMACIÓN PERSONAL
       ============================================================ */

    /**
     * Muestra los datos personales almacenados
     * en la cuenta de usuario.
     */
    private void cargarDatosPersonales() {

        if (medicoActual == null) {
            return;
        }

        Usuario usuario =
                medicoActual.getUsuario();

        if (usuario == null) {

            usuario =
                    usuarioActual;
        }

        if (usuario == null) {
            return;
        }

        String nombreCompleto =
                valorSeguro(
                        usuario.getNombreCompleto()
                );

        lblNombreCompleto.setText(
                nombreCompleto
        );

        if (lblNombreHero != null) {

            lblNombreHero.setText(
                    nombreCompleto
            );
        }

        if (lblIniciales != null) {

            lblIniciales.setText(
                    obtenerIniciales(
                            nombreCompleto
                    )
            );
        }

        lblNombreMedico.setText(
                nombreCompleto
        );

        lblCedula.setText(
                valorSeguro(
                        usuario.getCedula()
                )
        );

        lblCorreo.setText(
                valorSeguro(
                        usuario.getCorreo()
                )
        );

        lblTelefono.setText(
                valorSeguro(
                        usuario.getTelefono()
                )
        );
    }

    /* ============================================================
                     INFORMACIÓN PROFESIONAL
       ============================================================ */

    /**
     * Muestra los datos profesionales obtenidos
     * previamente desde MySQL.
     *
     * @param especialidades especialidades asignadas.
     * @param establecimientos establecimientos asignados.
     */
    private void cargarDatosProfesionales(
            List<String> especialidades,
            List<String> establecimientos
    ) {

        if (medicoActual == null) {
            return;
        }

        lblCedulaProfesional.setText(
                valorSeguro(
                        medicoActual.getCedulaProfesional()
                )
        );

        lblEspecialidades.setText(
                convertirLista(
                        especialidades,
                        "Sin especialidades asignadas"
                )
        );

        lblEstablecimientos.setText(
                convertirLista(
                        establecimientos,
                        "Sin establecimientos asignados"
                )
        );

        lblEstado.setText(
                medicoActual.isEstado()
                        ? "ACTIVO"
                        : "INACTIVO"
        );

        lblObservacion.setText(
                valorSeguro(
                        medicoActual.getObservacion()
                )
        );

        if (
                medicoActual.getFechaCreacion()
                        != null
        ) {

            lblFechaRegistro.setText(
                    medicoActual
                            .getFechaCreacion()
                            .format(
                                    formatoFecha
                            )
            );

        } else {

            lblFechaRegistro.setText(
                    "No especificado"
            );
        }
    }

    /* ============================================================
                       UTILIDADES DE PRESENTACIÓN
       ============================================================ */

       /**
 * Actualiza visualmente el estado de seguridad
 * de la cuenta médica.
 */
private void actualizarEstadoSeguridad() {

    if (
            usuarioActual == null
                    || lblEstadoSeguridadMedico == null
                    || lblPreguntaSeguridadMedico == null
                    || btnSeguridadMedico == null
    ) {

        return;
    }

    if (
            usuarioActual
                    .tienePreguntaSeguridadConfigurada()
    ) {

        /*
         * ====================================================
         * SEGURIDAD CONFIGURADA
         * ====================================================
         */

        lblEstadoSeguridadMedico.setText(
                "CONFIGURADA"
        );

        lblEstadoSeguridadMedico
                .getStyleClass()
                .removeAll(
                        "doctor-security-warning",
                        "doctor-security-success"
                );

        lblEstadoSeguridadMedico
                .getStyleClass()
                .add(
                        "doctor-security-success"
                );

        PreguntaSeguridad pregunta =
                PreguntaSeguridad.desdeCodigo(
                        usuarioActual.getPreguntaSeguridad()
                );

        if (pregunta != null) {

            lblPreguntaSeguridadMedico.setText(
                    pregunta.getTexto()
            );

        } else {

            lblPreguntaSeguridadMedico.setText(
                    "Pregunta de seguridad configurada."
            );
        }

        btnSeguridadMedico.setText(
                "CAMBIAR SEGURIDAD"
        );

    } else {

        /*
         * ====================================================
         * SEGURIDAD NO CONFIGURADA
         * ====================================================
         */

        lblEstadoSeguridadMedico.setText(
                "NO CONFIGURADA"
        );

        lblEstadoSeguridadMedico
                .getStyleClass()
                .removeAll(
                        "doctor-security-warning",
                        "doctor-security-success"
                );

        lblEstadoSeguridadMedico
                .getStyleClass()
                .add(
                        "doctor-security-warning"
                );

        lblPreguntaSeguridadMedico.setText(
                "Aún no has configurado una pregunta de seguridad."
        );

        btnSeguridadMedico.setText(
                "CONFIGURAR SEGURIDAD"
        );
    }
}

    /**
     * Convierte una lista de valores en texto
     * legible para la interfaz.
     */
    private String convertirLista(
            List<String> valores,
            String valorVacio
    ) {

        if (
                valores == null
                        || valores.isEmpty()
        ) {

            return valorVacio;
        }

        return String.join(
                " • ",
                valores
        );
    }

    /**
     * Obtiene las iniciales del médico
     * para el avatar profesional.
     */
    private String obtenerIniciales(
            String nombreCompleto
    ) {

        if (
                nombreCompleto == null
                        || nombreCompleto.isBlank()
                        || "No especificado".equalsIgnoreCase(
                                nombreCompleto
                        )
        ) {

            return "MD";
        }

        String[] partes =
                nombreCompleto
                        .trim()
                        .split("\\s+");

        if (partes.length == 1) {

            return partes[0]
                    .substring(
                            0,
                            1
                    )
                    .toUpperCase(
                            Locale.ROOT
                    );
        }

        String primera =
                partes[0]
                        .substring(
                                0,
                                1
                        );

        String ultima =
                partes[
                        partes.length - 1
                        ]
                        .substring(
                                0,
                                1
                        );

        return (
                primera
                        + ultima
        ).toUpperCase(
                Locale.ROOT
        );
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

        return valor.trim();
    }

    /* ============================================================
                            NAVEGACIÓN
       ============================================================ */

       /**
 * Abre la configuración de seguridad
 * de la cuenta médica.
 */
@FXML
private void abrirSeguridadCuenta() {

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
                                "/fxml/seguridad-cuenta.fxml"
                        )
                );

        Parent root =
                loader.load();

        SeguridadCuentaController controller =
                loader.getController();

        controller.setUsuario(
                usuarioActual
        );

        Stage ventanaSeguridad =
                new Stage();

        ventanaSeguridad.setTitle(
                "MediAppoint - Seguridad de la cuenta"
        );

        ventanaSeguridad.setScene(
                new Scene(root)
        );

        ventanaSeguridad.setResizable(
                false
        );

        Stage ventanaActual =
                obtenerStage();

        if (ventanaActual != null) {

            ventanaSeguridad.initOwner(
                    ventanaActual
            );

            ventanaSeguridad.initModality(
                    Modality.WINDOW_MODAL
            );
        }

        ventanaSeguridad.centerOnScreen();

        /*
         * Esperamos a que el médico termine
         * de configurar su seguridad.
         */
        ventanaSeguridad.showAndWait();

        /*
         * SeguridadCuentaService modifica el mismo
         * usuarioActual mantenido en memoria.
         */
        actualizarEstadoSeguridad();

    } catch (IOException e) {

        mostrarMensaje(
                "No fue posible abrir la seguridad de la cuenta."
        );

        System.err.println(
                "Error cargando seguridad-cuenta.fxml: "
                        + e.getMessage()
        );

        e.printStackTrace();
    }
}

    /**
     * Abre la agenda principal del médico.
     */
    @FXML
    private void abrirAgenda() {

        cambiarPantalla(
                "/fxml/dashboard-medico.fxml"
        );
    }

    /**
     * Abre el historial de atenciones
     * del médico autenticado.
     */
    @FXML
    private void abrirHistorial() {

        cambiarPantalla(
                "/fxml/historial-atenciones-medico.fxml"
        );
    }

    /**
     * Recarga la información actual del perfil.
     */
    @FXML
    private void abrirPerfil() {

        cargarPerfil();
    }

    /**
     * Finaliza la sesión actual.
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

            System.err.println(
                    "Error al cerrar sesión: "
                            + excepcion.getMessage()
            );

            excepcion.printStackTrace();
        }
    }

    /**
     * Cambia entre las pantallas pertenecientes
     * al Portal Médico conservando la sesión.
     *
     * @param rutaFXML ruta de la pantalla.
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

            /*
             * Transferencia del usuario autenticado
             * al controlador de la pantalla destino.
             */
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
                            instanceof HistorialAtencionesMedicoController historial
            ) {

                historial.setUsuario(
                        usuarioActual
                );
            }

            Stage stage =
                    obtenerStage();

            if (stage == null) {

                mostrarMensaje(
                        "No fue posible identificar la ventana actual."
                );

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

            System.err.println(
                    "Error al cambiar pantalla: "
                            + excepcion.getMessage()
            );

            excepcion.printStackTrace();
        }
    }

    /**
     * Obtiene la ventana actual.
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
                        EJECUCIÓN EN SEGUNDO PLANO
       ============================================================ */

    /**
     * Ejecuta una tarea JavaFX utilizando
     * un hilo secundario.
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
     * Muestra un mensaje dentro de la interfaz.
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
                       RESULTADO DE CONSULTAS
       ============================================================ */

    /**
     * Agrupa la información obtenida desde MySQL.
     */
    private record DatosPerfilMedico(
            Medico medico,
            List<String> especialidades,
            List<String> establecimientos
    ) {
    }
}