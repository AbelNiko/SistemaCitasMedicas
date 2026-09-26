package com.citasmedicas.controller;

import com.citasmedicas.dao.MedicoDAO;
import com.citasmedicas.model.Medico;
import com.citasmedicas.model.Usuario;
import com.citasmedicas.util.SesionUtil;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

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
 * @version 1.2
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
         * disponible en la sesión, sin esperar MySQL.
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

        /*
         * La consulta completa del perfil se ejecuta
         * fuera del hilo gráfico de JavaFX.
         */
        Task<DatosPerfilMedico> tarea =
                new Task<>() {

                    @Override
                    protected DatosPerfilMedico call()
                            throws Exception {

                        Medico medico =
                                medicoDAO.buscarPorIdUsuario(
                                        idUsuario
                                );

                        /*
                         * Si no existe perfil médico, se devuelve
                         * un resultado vacío controlado.
                         */
                        if (medico == null) {

                            return new DatosPerfilMedico(
                                    null,
                                    List.of(),
                                    List.of()
                            );
                        }

                        List<String> especialidades =
                                medicoDAO
                                        .listarEspecialidadesPorMedico(
                                                medico.getIdMedico()
                                        );

                        List<String> establecimientos =
                                medicoDAO
                                        .listarEstablecimientosPorMedico(
                                                medico.getIdMedico()
                                        );

                        return new DatosPerfilMedico(
                                medico,
                                especialidades,
                                establecimientos
                        );
                    }
                };

        /*
         * Cuando todas las consultas terminan correctamente,
         * JavaFX vuelve automáticamente al hilo gráfico.
         */
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

        /*
         * Manejo de errores producido durante cualquiera
         * de las consultas realizadas en segundo plano.
         */
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

        /*
         * Si el DAO no devolviera el objeto Usuario,
         * utilizamos la información de la sesión.
         */
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

        /*
         * Nombre principal utilizado en el hero.
         */
        if (lblNombreHero != null) {

            lblNombreHero.setText(
                    nombreCompleto
            );
        }

        /*
         * Avatar generado automáticamente.
         * Ejemplo: Carlos Mendoza -> CM
         */
        if (lblIniciales != null) {

            lblIniciales.setText(
                    obtenerIniciales(
                            nombreCompleto
                    )
            );
        }

        /*
         * También actualizamos el nombre
         * mostrado en el menú lateral.
         */
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
     * Convierte una lista de valores en texto
     * legible para la interfaz.
     *
     * @param valores lista de valores.
     * @param valorVacio texto mostrado si la lista está vacía.
     * @return texto preparado para mostrar.
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
     * Obtiene las iniciales que se muestran
     * dentro del avatar profesional.
     *
     * Ejemplos:
     * Carlos Mendoza -> CM
     * Ana -> A
     *
     * @param nombreCompleto nombre del médico.
     * @return iniciales correspondientes.
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

        /*
         * Si solamente existe un nombre,
         * utilizamos la primera letra.
         */
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

        /*
         * Utilizamos la primera letra del primer
         * nombre y la primera letra del último apellido.
         */
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
     *
     * @param valor valor recibido.
     * @return valor válido para mostrar.
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
     * Regresa a la agenda principal del médico.
     */
    @FXML
    private void abrirAgenda() {

        cambiarPantalla(
                "/fxml/dashboard-medico.fxml"
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
     * Finaliza la sesión actual y regresa
     * a la pantalla de inicio de sesión.
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
     * al Portal Médico.
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
             * Conserva la sesión cuando se cambia
             * entre Mi agenda y Mi perfil.
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
     * Obtiene la ventana actual mediante
     * uno de los controles visibles.
     *
     * @return ventana actual o null.
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
     *
     * Esto evita que las consultas MySQL bloqueen
     * la interfaz gráfica.
     *
     * @param tarea tarea JavaFX.
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

        /*
         * El hilo no impedirá que la aplicación
         * pueda cerrarse normalmente.
         */
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
     *
     * @param mensaje texto mostrado.
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
     * Agrupa temporalmente toda la información
     * obtenida desde MySQL antes de actualizar JavaFX.
     *
     * @param medico perfil médico.
     * @param especialidades especialidades asignadas.
     * @param establecimientos establecimientos asignados.
     */
    private record DatosPerfilMedico(
            Medico medico,
            List<String> especialidades,
            List<String> establecimientos
    ) {
    }
}