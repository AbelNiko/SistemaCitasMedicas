package com.citasmedicas.controller;

import com.citasmedicas.model.Paciente;
import com.citasmedicas.model.PreguntaSeguridad;
import com.citasmedicas.service.RegistroPacienteService;

import javafx.concurrent.Task;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import javafx.stage.Stage;

import javafx.util.StringConverter;

import java.io.IOException;

import java.sql.SQLException;

import java.time.LocalDate;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * ================================================================
 *             CONTROLADOR - REGISTRO DE PACIENTE
 * ================================================================
 *
 * Gestiona el registro de nuevas cuentas de pacientes.
 *
 * Incluye:
 *
 * - Datos personales.
 * - Fecha de nacimiento.
 * - Datos de acceso.
 * - Pregunta de seguridad.
 * - Registro asíncrono para base de datos remota.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.2
 */
public class RegistroController {

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

    @FXML
    private TextField txtDireccion;

    @FXML
    private TextField txtTelefono;

    @FXML
    private TextField txtCorreo;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private PasswordField txtConfirmarPassword;

    @FXML
    private ComboBox<PreguntaSeguridad> cmbPreguntaSeguridad;

    @FXML
    private TextField txtRespuestaSeguridad;

    @FXML
    private TextField txtContactoEmergencia;

    @FXML
    private TextField txtTelefonoEmergencia;

    @FXML
    private Label lblMensaje;

    @FXML
    private Button btnCrearCuenta;

    private final RegistroPacienteService registroService;

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern(
                    "dd/MM/yyyy"
            );

    /**
     * Constructor principal.
     */
    public RegistroController() {

        registroService =
                new RegistroPacienteService();
    }

    /**
     * Configuración inicial de la pantalla.
     */
    @FXML
    private void initialize() {

        configurarSexo();

        configurarPreguntasSeguridad();

        configurarCalendario();
    }

    /**
     * Configura las opciones de sexo.
     */
    private void configurarSexo() {

        cmbSexo.getItems().setAll(
                "Femenino",
                "Masculino",
                "Otro",
                "Prefiero no indicar"
        );
    }

    /**
     * Carga las preguntas de seguridad disponibles.
     */
    private void configurarPreguntasSeguridad() {

        cmbPreguntaSeguridad
                .getItems()
                .setAll(
                        PreguntaSeguridad.values()
                );
    }

    /**
     * Configura el calendario para fecha de nacimiento.
     */
    private void configurarCalendario() {

        dpFechaNacimiento.setEditable(
                true
        );

        dpFechaNacimiento.setShowWeekNumbers(
                false
        );

        dpFechaNacimiento.setPromptText(
                "dd/mm/aaaa"
        );

        /*
         * Permite visualizar y escribir la fecha
         * con formato ecuatoriano:
         *
         * día / mes / año
         */
        dpFechaNacimiento.setConverter(
                new StringConverter<>() {

                    @Override
                    public String toString(
                            LocalDate fecha
                    ) {

                        if (fecha == null) {
                            return "";
                        }

                        return FORMATO_FECHA.format(
                                fecha
                        );
                    }

                    @Override
                    public LocalDate fromString(
                            String texto
                    ) {

                        if (
                                texto == null
                                        || texto.isBlank()
                        ) {

                            return null;
                        }

                        try {

                            return LocalDate.parse(
                                    texto.trim(),
                                    FORMATO_FECHA
                            );

                        } catch (
                                DateTimeParseException e
                        ) {

                            return null;
                        }
                    }
                }
        );

        LocalDate hoy =
                LocalDate.now();

        LocalDate fechaMinima =
                hoy.minusYears(
                        120
                );

        /*
         * No permitimos fechas futuras ni fechas
         * de más de 120 años.
         */
        dpFechaNacimiento.setDayCellFactory(
                picker -> new DateCell() {

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
                                fecha.isAfter(hoy)
                                        || fecha.isBefore(
                                                fechaMinima
                                        )
                        );
                    }
                }
        );
    }

    /**
     * Inicia el proceso de registro.
     *
     * La conexión a Aiven se realiza fuera del
     * hilo gráfico de JavaFX para evitar congelar
     * la aplicación.
     */
    @FXML
    private void registrarPaciente() {

        limpiarMensaje();

        /*
         * Copiamos los datos antes de crear el hilo.
         * Los controles JavaFX deben consultarse
         * únicamente desde el hilo gráfico.
         */

        String nombres =
                txtNombres.getText();

        String apellidos =
                txtApellidos.getText();

        String cedula =
                txtCedula.getText();

        String correo =
                txtCorreo.getText();

        String telefono =
                txtTelefono.getText();

        String password =
                txtPassword.getText();

        String confirmarPassword =
                txtConfirmarPassword.getText();

        PreguntaSeguridad pregunta =
                cmbPreguntaSeguridad.getValue();

        String respuestaSeguridad =
                txtRespuestaSeguridad.getText();

        LocalDate fechaNacimiento =
                dpFechaNacimiento.getValue();

        String direccion =
                txtDireccion.getText();

        String sexo =
                cmbSexo.getValue();

        String contactoEmergencia =
                txtContactoEmergencia.getText();

        String telefonoEmergencia =
                txtTelefonoEmergencia.getText();

        /*
         * Indicamos inmediatamente al usuario
         * que el registro está procesándose.
         */
        btnCrearCuenta.setDisable(
                true
        );

        btnCrearCuenta.setText(
                "CREANDO CUENTA..."
        );

        mostrarInformacion(
                "Validando información y creando tu cuenta..."
        );

        Task<Paciente> tarea =
                new Task<>() {

                    @Override
                    protected Paciente call()
                            throws Exception {

                        return registroService.registrar(
                                nombres,
                                apellidos,
                                cedula,
                                correo,
                                telefono,
                                password,
                                confirmarPassword,
                                pregunta,
                                respuestaSeguridad,
                                fechaNacimiento,
                                direccion,
                                sexo,
                                contactoEmergencia,
                                telefonoEmergencia
                        );
                    }
                };

        /*
         * ========================================================
         * REGISTRO CORRECTO
         * ========================================================
         */

        tarea.setOnSucceeded(
                evento -> {

                    restaurarBoton();

                    Paciente paciente =
                            tarea.getValue();

                    String mensaje =
                            "Cuenta creada correctamente. "
                                    + "Bienvenido, "
                                    + paciente
                                            .getUsuario()
                                            .getNombreCompleto()
                                    + ".";

                    mostrarExito(
                            mensaje
                    );

                    mostrarAlerta(
                            Alert.AlertType.INFORMATION,
                            "Cuenta creada",
                            "Registro completado",
                            "Tu cuenta fue creada correctamente. "
                                    + "Ya puedes iniciar sesión."
                    );

                    limpiarFormulario();
                }
        );

        /*
         * ========================================================
         * ERROR DURANTE EL REGISTRO
         * ========================================================
         */

        tarea.setOnFailed(
                evento -> {

                    restaurarBoton();

                    Throwable error =
                            tarea.getException();

                    /*
                     * Las validaciones de negocio se muestran
                     * directamente al usuario.
                     */
                    if (
                            error
                                    instanceof IllegalArgumentException
                            || error
                                    instanceof IllegalStateException
                    ) {

                        mostrarError(
                                error.getMessage()
                        );

                        mostrarAlerta(
                                Alert.AlertType.WARNING,
                                "Revisa la información",
                                "No se pudo crear la cuenta",
                                error.getMessage()
                        );

                        return;
                    }

                    /*
                     * Si MySQL genera un error, dejamos la
                     * información técnica en la terminal.
                     */
                    if (
                            error
                                    instanceof SQLException sqlError
                    ) {

                        System.err.println(
                                "=============================================="
                        );

                        System.err.println(
                                "ERROR SQL DURANTE EL REGISTRO"
                        );

                        System.err.println(
                                "SQLState: "
                                        + sqlError.getSQLState()
                        );

                        System.err.println(
                                "Código: "
                                        + sqlError.getErrorCode()
                        );

                        System.err.println(
                                "Mensaje: "
                                        + sqlError.getMessage()
                        );

                        System.err.println(
                                "=============================================="
                        );

                        sqlError.printStackTrace();

                        mostrarError(
                                "No fue posible guardar la cuenta "
                                        + "en la base de datos."
                        );

                        mostrarAlerta(
                                Alert.AlertType.ERROR,
                                "Error de registro",
                                "No se pudo guardar la cuenta",
                                "No fue posible completar el registro. "
                                        + "Revisa tu conexión e inténtalo nuevamente."
                        );

                        return;
                    }

                    /*
                     * Error no previsto.
                     */
                    System.err.println(
                            "=============================================="
                    );

                    System.err.println(
                            "ERROR INESPERADO DURANTE EL REGISTRO"
                    );

                    if (error != null) {
                        error.printStackTrace();
                    }

                    System.err.println(
                            "=============================================="
                    );

                    mostrarError(
                            "No fue posible crear la cuenta."
                    );

                    mostrarAlerta(
                            Alert.AlertType.ERROR,
                            "Error",
                            "No se pudo crear la cuenta",
                            "Ocurrió un error inesperado durante el registro."
                    );
                }
        );

        Thread hilo =
                new Thread(
                        tarea,
                        "registro-paciente-aiven"
                );

        hilo.setDaemon(
                true
        );

        hilo.start();
    }

    /**
     * Restaura el botón después de completar
     * la operación.
     */
    private void restaurarBoton() {

        btnCrearCuenta.setDisable(
                false
        );

        btnCrearCuenta.setText(
                "CREAR CUENTA"
        );
    }

    /**
     * Limpia el formulario después
     * de un registro correcto.
     */
    private void limpiarFormulario() {

        txtNombres.clear();
        txtApellidos.clear();
        txtCedula.clear();

        dpFechaNacimiento.setValue(
                null
        );

        cmbSexo.setValue(
                null
        );

        txtDireccion.clear();
        txtTelefono.clear();
        txtCorreo.clear();

        txtPassword.clear();
        txtConfirmarPassword.clear();

        cmbPreguntaSeguridad.setValue(
                null
        );

        txtRespuestaSeguridad.clear();

        txtContactoEmergencia.clear();
        txtTelefonoEmergencia.clear();
    }

    /**
     * Regresa al login.
     */
    @FXML
    private void volverLogin() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/login.fxml"
                            )
                    );

            Parent root =
                    loader.load();

            Stage stage =
                    (Stage) lblMensaje
                            .getScene()
                            .getWindow();

            stage.setScene(
                    new Scene(root)
            );

            stage.setTitle(
                    "MediAppoint - Iniciar sesión"
            );

            stage.centerOnScreen();

        } catch (IOException e) {

            mostrarError(
                    "No fue posible regresar al inicio de sesión."
            );

            e.printStackTrace();
        }
    }

    /**
     * Muestra error en pantalla.
     */
    private void mostrarError(
            String mensaje
    ) {

        lblMensaje.setStyle(
                "-fx-text-fill: #FF6B6B;"
        );

        lblMensaje.setText(
                mensaje
        );
    }

    /**
     * Muestra estado informativo.
     */
    private void mostrarInformacion(
            String mensaje
    ) {

        lblMensaje.setStyle(
                "-fx-text-fill: #67BFFF;"
        );

        lblMensaje.setText(
                mensaje
        );
    }

    /**
     * Muestra resultado satisfactorio.
     */
    private void mostrarExito(
            String mensaje
    ) {

        lblMensaje.setStyle(
                "-fx-text-fill: #45D483;"
        );

        lblMensaje.setText(
                mensaje
        );
    }

    /**
     * Limpia mensajes anteriores.
     */
    private void limpiarMensaje() {

        lblMensaje.setText(
                ""
        );
    }

    /**
     * Muestra una ventana de información visible
     * incluso si el formulario está desplazado.
     */
    private void mostrarAlerta(
            Alert.AlertType tipo,
            String titulo,
            String encabezado,
            String contenido
    ) {

        Alert alerta =
                new Alert(
                        tipo
                );

        alerta.setTitle(
                titulo
        );

        alerta.setHeaderText(
                encabezado
        );

        alerta.setContentText(
                contenido
        );

        alerta.showAndWait();
    }
}