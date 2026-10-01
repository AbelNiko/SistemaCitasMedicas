package com.citasmedicas.controller;

import com.citasmedicas.model.Paciente;
import com.citasmedicas.model.Usuario;
import com.citasmedicas.model.PreguntaSeguridad;
import com.citasmedicas.service.PerfilService;
import com.citasmedicas.util.SesionUtil;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.stage.Modality;

import java.io.IOException;
import java.time.LocalDate;

/**
 * ================================================================
 *                  CONTROLLER - MI PERFIL
 * ================================================================
 *
 * Gestiona la consulta y actualización de la información
 * personal, de contacto y médica básica del paciente autenticado.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.1
 */
public class PerfilController {

    /* ============================================================
                           INFORMACIÓN GENERAL
       ============================================================ */

    @FXML
    private Label lblNombreUsuario;

    @FXML
    private Label lblMensaje;

    /* ============================================================
                           DATOS PERSONALES
       ============================================================ */

    @FXML
    private TextField txtNombres;

    @FXML
    private TextField txtApellidos;

    @FXML
    private TextField txtCedula;

    @FXML
    private DatePicker dpFechaNacimiento;

    @FXML
    private ComboBox<String> cmbSexo;

    /* ============================================================
                         DATOS DE CONTACTO
       ============================================================ */

    @FXML
    private TextField txtCorreo;

    @FXML
    private TextField txtTelefono;

    @FXML
    private TextField txtDireccion;

    /*
 * ============================================================
 * SEGURIDAD DE LA CUENTA
 * ============================================================
 */

@FXML
private Label lblEstadoSeguridadPerfil;

@FXML
private Label lblPreguntaSeguridadPerfil;

@FXML
private Button btnSeguridadCuenta;

    /* ============================================================
                         INFORMACIÓN MÉDICA
       ============================================================ */

    @FXML
    private ComboBox<String> cmbTipoSangre;

    @FXML
    private TextArea txtAlergias;

    @FXML
    private TextArea txtCondicionesMedicas;

    /* ============================================================
                       CONTACTO DE EMERGENCIA
       ============================================================ */

    @FXML
    private TextField txtContactoEmergencia;

    @FXML
    private TextField txtTelefonoEmergencia;

    /* ============================================================
                             ESTADO
       ============================================================ */

    private Usuario usuarioActual;
    private Paciente pacienteActual;

    private final PerfilService perfilService;

    public PerfilController() {
        this.perfilService = new PerfilService();
    }

    /**
     * Configura las opciones disponibles de los controles
     * cuando JavaFX termina de cargar el FXML.
     */
    @FXML
    private void initialize() {

        cmbSexo.getItems().setAll(
                "Masculino",
                "Femenino",
                "Otro",
                "Prefiero no indicar"
        );

        cmbTipoSangre.getItems().setAll(
                "A+",
                "A-",
                "B+",
                "B-",
                "AB+",
                "AB-",
                "O+",
                "O-",
                "No conoce"
        );

        /*
         * La cédula identifica al usuario dentro del sistema
         * y no debe modificarse desde el perfil.
         */
        txtCedula.setEditable(false);
        txtCedula.setFocusTraversable(false);

        /*
         * No permitimos seleccionar fechas futuras.
         */
        dpFechaNacimiento.setDayCellFactory(
                datePicker -> new javafx.scene.control.DateCell() {

                    @Override
                    public void updateItem(
                            LocalDate fecha,
                            boolean empty
                    ) {
                        super.updateItem(fecha, empty);

                        if (empty || fecha == null) {
                            return;
                        }

                        setDisable(
                                fecha.isAfter(LocalDate.now())
                        );
                    }
                }
        );
    }

    /**
     * Recibe el usuario autenticado y carga
     * la información completa del perfil.
     */
    public void setUsuario(Usuario usuario) {

        this.usuarioActual =
        usuario;

cargarDatos();

actualizarEstadoSeguridad();
    }

    /**
     * Consulta nuevamente la información del paciente
     * y la muestra en pantalla.
     */
    private void cargarDatos() {

        if (usuarioActual == null) {
            mostrarError(
                    "No existe un usuario autenticado."
            );
            return;
        }

        limpiarMensaje();

        try {

            pacienteActual =
                    perfilService.obtenerPaciente(
                            usuarioActual.getIdUsuario()
                    );

            /*
             * =====================================================
             * USUARIO
             * =====================================================
             */

            lblNombreUsuario.setText(
                    usuarioActual.getNombreCompleto()
            );

            txtNombres.setText(
                    valorSeguro(
                            usuarioActual.getNombres()
                    )
            );

            txtApellidos.setText(
                    valorSeguro(
                            usuarioActual.getApellidos()
                    )
            );

            txtCedula.setText(
                    valorSeguro(
                            usuarioActual.getCedula()
                    )
            );

            txtCorreo.setText(
                    valorSeguro(
                            usuarioActual.getCorreo()
                    )
            );

            txtTelefono.setText(
                    valorSeguro(
                            usuarioActual.getTelefono()
                    )
            );

            /*
             * =====================================================
             * PACIENTE
             * =====================================================
             */

            if (pacienteActual == null) {

                limpiarDatosPaciente();

                mostrarError(
                        "No se encontró el perfil de paciente."
                );

                return;
            }

            dpFechaNacimiento.setValue(
                    pacienteActual.getFechaNacimiento()
            );

            seleccionarValor(
                    cmbSexo,
                    pacienteActual.getSexo()
            );

            txtDireccion.setText(
                    valorSeguro(
                            pacienteActual.getDireccion()
                    )
            );

            seleccionarValor(
                    cmbTipoSangre,
                    pacienteActual.getTipoSangre()
            );

            txtAlergias.setText(
                    valorSeguro(
                            pacienteActual.getAlergias()
                    )
            );

            txtCondicionesMedicas.setText(
                    valorSeguro(
                            pacienteActual.getCondicionesMedicas()
                    )
            );

            txtContactoEmergencia.setText(
                    valorSeguro(
                            pacienteActual.getContactoEmergencia()
                    )
            );

            txtTelefonoEmergencia.setText(
                    valorSeguro(
                            pacienteActual.getTelefonoEmergencia()
                    )
            );

        } catch (Exception e) {

            mostrarError(
                    "No fue posible cargar la información del perfil."
            );

            System.err.println(
                    "Error al cargar perfil: "
                    + e.getMessage()
            );

            e.printStackTrace();
        }
    }

    /**
     * Descarta los cambios realizados en pantalla
     * y vuelve a cargar los valores persistidos.
     */
    @FXML
    private void recargarDatos() {

        cargarDatos();

        if (pacienteActual != null) {
            mostrarMensaje(
                    "Los cambios fueron descartados."
            );
        }
    }

    /**
     * Guarda los cambios realizados por el paciente.
     */
    @FXML
    private void guardarCambios() {

        if (
                usuarioActual == null
                || pacienteActual == null
        ) {

            mostrarError(
                    "No fue posible identificar el perfil."
            );

            return;
        }

        try {

            perfilService.actualizarPerfil(
                    usuarioActual,
                    pacienteActual.getIdPaciente(),
                    txtNombres.getText(),
                    txtApellidos.getText(),
                    txtCorreo.getText(),
                    txtTelefono.getText(),
                    dpFechaNacimiento.getValue(),
                    cmbSexo.getValue(),
                    txtDireccion.getText(),
                    cmbTipoSangre.getValue(),
                    txtAlergias.getText(),
                    txtCondicionesMedicas.getText(),
                    txtContactoEmergencia.getText(),
                    txtTelefonoEmergencia.getText()
            );

            /*
             * Volvemos a consultar MySQL después del UPDATE
             * para mantener el objeto local sincronizado.
             */
            pacienteActual =
                    perfilService.obtenerPaciente(
                            usuarioActual.getIdUsuario()
                    );

            /*
             * PerfilService ya sincroniza los datos básicos
             * del objeto Usuario después del commit.
             */
            lblNombreUsuario.setText(
                    usuarioActual.getNombreCompleto()
            );

            /*
             * Normalizamos nuevamente la representación
             * visual usando los datos persistidos.
             */
            cargarCamposActualizados();

            mostrarExito(
                    "Perfil actualizado correctamente."
            );

        } catch (IllegalArgumentException e) {

            mostrarError(
                    e.getMessage()
            );

        } catch (Exception e) {

            mostrarError(
                    "No fue posible actualizar el perfil."
            );

            System.err.println(
                    "Error al actualizar perfil: "
                    + e.getMessage()
            );

            e.printStackTrace();
        }
    }

    /**
     * Refresca los controles después de guardar,
     * sin eliminar el mensaje de éxito.
     */
    private void cargarCamposActualizados() {

        txtNombres.setText(
                valorSeguro(
                        usuarioActual.getNombres()
                )
        );

        txtApellidos.setText(
                valorSeguro(
                        usuarioActual.getApellidos()
                )
        );

        txtCedula.setText(
                valorSeguro(
                        usuarioActual.getCedula()
                )
        );

        txtCorreo.setText(
                valorSeguro(
                        usuarioActual.getCorreo()
                )
        );

        txtTelefono.setText(
                valorSeguro(
                        usuarioActual.getTelefono()
                )
        );

        if (pacienteActual == null) {
            return;
        }

        dpFechaNacimiento.setValue(
                pacienteActual.getFechaNacimiento()
        );

        seleccionarValor(
                cmbSexo,
                pacienteActual.getSexo()
        );

        txtDireccion.setText(
                valorSeguro(
                        pacienteActual.getDireccion()
                )
        );

        seleccionarValor(
                cmbTipoSangre,
                pacienteActual.getTipoSangre()
        );

        txtAlergias.setText(
                valorSeguro(
                        pacienteActual.getAlergias()
                )
        );

        txtCondicionesMedicas.setText(
                valorSeguro(
                        pacienteActual.getCondicionesMedicas()
                )
        );

        txtContactoEmergencia.setText(
                valorSeguro(
                        pacienteActual.getContactoEmergencia()
                )
        );

        txtTelefonoEmergencia.setText(
                valorSeguro(
                        pacienteActual.getTelefonoEmergencia()
                )
        );
    }

    /**
     * Limpia únicamente los datos propios del paciente.
     */
    private void limpiarDatosPaciente() {

        dpFechaNacimiento.setValue(null);
        cmbSexo.getSelectionModel().clearSelection();
        txtDireccion.clear();

        cmbTipoSangre
                .getSelectionModel()
                .clearSelection();

        txtAlergias.clear();
        txtCondicionesMedicas.clear();
        txtContactoEmergencia.clear();
        txtTelefonoEmergencia.clear();
    }

    /**
     * Selecciona un elemento del ComboBox cuando
     * existe un valor persistido.
     */
    private void seleccionarValor(
            ComboBox<String> comboBox,
            String valor
    ) {

        if (valor == null || valor.isBlank()) {

            comboBox
                    .getSelectionModel()
                    .clearSelection();

            return;
        }

        comboBox.setValue(
                valor.trim()
        );
    }

    /* ============================================================
                              NAVEGACIÓN
       ============================================================ */

    /**
     * Regresa al Dashboard.
     */
    @FXML
    private void volverDashboard() {

        cambiarPantalla(
                "/fxml/dashboard.fxml",
                "MediAppoint - Panel principal"
        );
    }

    /**
     * Abre Agendar cita.
     */
    @FXML
    private void abrirAgendarCita() {

        cambiarPantalla(
                "/fxml/agendar-cita.fxml",
                "MediAppoint - Agendar cita"
        );
    }

    /**
     * Abre Mis citas.
     */
    @FXML
    private void abrirMisCitas() {

        cambiarPantalla(
                "/fxml/mis-citas.fxml",
                "MediAppoint - Mis citas"
        );
    }

    /**
     * Abre el historial de atenciones del paciente.
     */
    @FXML
    private void abrirHistorial() {

        cambiarPantalla(
                "/fxml/historial-paciente.fxml",
                "MediAppoint - Historial de atenciones"
        );
    }

    /**
     * Permanece en Mi perfil y descarta
     * modificaciones no guardadas.
     */
    @FXML
    private void abrirPerfil() {

        recargarDatos();
    }

    /**
     * Gestiona la navegación manteniendo
     * el usuario autenticado.
     */
    private void cambiarPantalla(
            String ruta,
            String titulo
    ) {

        if (usuarioActual == null) {

            mostrarError(
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
                    instanceof HistorialPacienteController historial
            ) {

                historial.setUsuario(
                        usuarioActual
                );

            } else if (
                    controller
                    instanceof PerfilController perfil
            ) {

                perfil.setUsuario(
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

            mostrarError(
                    "No fue posible abrir la pantalla seleccionada."
            );

            System.err.println(
                    "Error al cambiar de pantalla: "
                    + e.getMessage()
            );

            e.printStackTrace();
        }
    }

/**
 * Actualiza la sección visual de seguridad
 * según la configuración actual del usuario.
 */
private void actualizarEstadoSeguridad() {

    if (
            usuarioActual == null
                    || lblEstadoSeguridadPerfil == null
                    || lblPreguntaSeguridadPerfil == null
    ) {

        return;
    }

    if (
            usuarioActual
                    .tienePreguntaSeguridadConfigurada()
    ) {

        lblEstadoSeguridadPerfil.setText(
                "CONFIGURADA"
        );

        lblEstadoSeguridadPerfil
                .getStyleClass()
                .removeAll(
                        "profile-security-warning",
                        "profile-security-success"
                );

        lblEstadoSeguridadPerfil
                .getStyleClass()
                .add(
                        "profile-security-success"
                );

        PreguntaSeguridad pregunta =
                PreguntaSeguridad.desdeCodigo(
                        usuarioActual.getPreguntaSeguridad()
                );

        if (pregunta != null) {

            lblPreguntaSeguridadPerfil.setText(
                    pregunta.getTexto()
            );

        } else {

            lblPreguntaSeguridadPerfil.setText(
                    "Pregunta de seguridad configurada."
            );
        }

        btnSeguridadCuenta.setText(
                "CAMBIAR SEGURIDAD"
        );

    } else {

        lblEstadoSeguridadPerfil.setText(
                "NO CONFIGURADA"
        );

        lblEstadoSeguridadPerfil
                .getStyleClass()
                .removeAll(
                        "profile-security-warning",
                        "profile-security-success"
                );

        lblEstadoSeguridadPerfil
                .getStyleClass()
                .add(
                        "profile-security-warning"
                );

        lblPreguntaSeguridadPerfil.setText(
                "Aún no has configurado una pregunta de seguridad."
        );

        btnSeguridadCuenta.setText(
                "CONFIGURAR SEGURIDAD"
        );
    }
}

    /* ============================================================
                              MENSAJES
       ============================================================ */

/**
 * Abre la configuración de seguridad
 * del usuario autenticado.
 */
@FXML
private void abrirSeguridadCuenta() {

    if (usuarioActual == null) {

        mostrarError(
                "No existe un usuario autenticado."
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

        Scene scene =
                new Scene(
                        loader.load()
                );

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
                scene
        );

        ventanaSeguridad.setResizable(
                false
        );

        /*
         * La ventana pertenece al perfil actual.
         */
        ventanaSeguridad.initOwner(
                lblNombreUsuario
                        .getScene()
                        .getWindow()
        );

        /*
         * Impide interactuar con el perfil
         * mientras esta ventana está abierta.
         */
        ventanaSeguridad.initModality(
                Modality.WINDOW_MODAL
        );

        ventanaSeguridad.centerOnScreen();

        ventanaSeguridad.showAndWait();

        /*
         * SeguridadCuentaService actualiza el mismo
         * objeto usuarioActual en memoria.
         */
        actualizarEstadoSeguridad();

    } catch (IOException e) {

        mostrarError(
                "No fue posible abrir la seguridad de la cuenta."
        );

        System.err.println(
                "Error cargando seguridad-cuenta.fxml: "
                        + e.getMessage()
        );

        e.printStackTrace();
    }
}

    private void mostrarMensaje(
            String mensaje
    ) {

        if (lblMensaje == null) {
            return;
        }

        lblMensaje.setText(
                mensaje
        );

        lblMensaje.setStyle(
                "-fx-text-fill: #A8BBC9;"
        );
    }

    private void mostrarError(
            String mensaje
    ) {

        if (lblMensaje == null) {
            return;
        }

        lblMensaje.setText(
                mensaje
        );

        lblMensaje.setStyle(
                "-fx-text-fill: #FF7B7B;"
        );
    }

    private void mostrarExito(
            String mensaje
    ) {

        if (lblMensaje == null) {
            return;
        }

        lblMensaje.setText(
                mensaje
        );

        lblMensaje.setStyle(
                "-fx-text-fill: #45D483;"
        );
    }

    private void limpiarMensaje() {

        if (lblMensaje != null) {
            lblMensaje.setText("");
        }
    }

    /**
     * Evita mostrar valores null.
     */
    private String valorSeguro(
            String valor
    ) {

        return valor == null
                ? ""
                : valor;
    }

    /**
     * Cierra la sesión del usuario autenticado.
     */
    @FXML
    private void cerrarSesion() {

        try {

            Stage stage =
                    (Stage) lblNombreUsuario
                            .getScene()
                            .getWindow();

            usuarioActual = null;
            pacienteActual = null;

            SesionUtil.cerrarSesion(
                    stage
            );

        } catch (IOException e) {

            System.err.println(
                    "Error al cerrar sesión desde Mi perfil: "
                    + e.getMessage()
            );

            e.printStackTrace();
        }
    }
}