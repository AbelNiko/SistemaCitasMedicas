package com.citasmedicas.service;

import com.citasmedicas.dao.PacienteDAO;
import com.citasmedicas.dao.RolDAO;
import com.citasmedicas.dao.UsuarioDAO;

import com.citasmedicas.model.Paciente;
import com.citasmedicas.model.PreguntaSeguridad;
import com.citasmedicas.model.Rol;
import com.citasmedicas.model.Usuario;

import com.citasmedicas.util.ConexionBD;
import com.citasmedicas.util.PasswordUtil;
import com.citasmedicas.util.RespuestaSeguridadUtil;
import com.citasmedicas.util.ValidacionDatosUtil;

import java.sql.Connection;
import java.sql.SQLException;

import java.time.LocalDate;

/**
 * ================================================================
 *              SERVICE - REGISTRO DE PACIENTE
 * ================================================================
 *
 * Gestiona el proceso completo de creación de una cuenta
 * de paciente.
 *
 * Responsabilidades:
 *
 * - Normalizar los datos recibidos.
 * - Validar información personal y de acceso.
 * - Verificar duplicados.
 * - Asignar exclusivamente el rol PACIENTE.
 * - Generar hashes BCrypt.
 * - Configurar la pregunta de seguridad.
 * - Registrar Usuario y Paciente en una transacción.
 *
 * La interfaz gráfica no contiene las reglas definitivas
 * de negocio. Estas se validan nuevamente en esta capa.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.2
 */
public class RegistroPacienteService {

    private static final String ROL_PACIENTE =
            "PACIENTE";

    private static final int LONGITUD_MAX_PASSWORD =
            64;

    private final UsuarioDAO usuarioDAO;

    private final PacienteDAO pacienteDAO;

    private final RolDAO rolDAO;

    /**
     * Constructor principal.
     */
    public RegistroPacienteService() {

        usuarioDAO =
                new UsuarioDAO();

        pacienteDAO =
                new PacienteDAO();

        rolDAO =
                new RolDAO();
    }

    /**
     * Registra una nueva cuenta de paciente.
     *
     * @param nombres nombres del paciente.
     * @param apellidos apellidos.
     * @param cedula número de cédula.
     * @param correo correo electrónico.
     * @param telefono teléfono opcional.
     * @param password contraseña.
     * @param confirmarPassword confirmación.
     * @param preguntaSeguridad pregunta elegida.
     * @param respuestaSeguridad respuesta de seguridad.
     * @param fechaNacimiento fecha de nacimiento.
     * @param direccion dirección opcional.
     * @param sexo sexo seleccionado.
     * @param contactoEmergencia contacto opcional.
     * @param telefonoEmergencia teléfono de emergencia.
     * @return paciente registrado.
     * @throws SQLException si ocurre un error en MySQL.
     */
    public Paciente registrar(
            String nombres,
            String apellidos,
            String cedula,
            String correo,
            String telefono,
            String password,
            String confirmarPassword,
            PreguntaSeguridad preguntaSeguridad,
            String respuestaSeguridad,
            LocalDate fechaNacimiento,
            String direccion,
            String sexo,
            String contactoEmergencia,
            String telefonoEmergencia
    ) throws SQLException {

        /*
         * ========================================================
         * NORMALIZACIÓN
         * ========================================================
         */

        nombres =
                ValidacionDatosUtil.normalizarNombre(
                        nombres
                );

        apellidos =
                ValidacionDatosUtil.normalizarNombre(
                        apellidos
                );

        correo =
                ValidacionDatosUtil.normalizarCorreo(
                        correo
                );

        cedula =
                normalizarObligatorio(
                        cedula
                );

        telefono =
                normalizarOpcional(
                        telefono
                );

        direccion =
                normalizarOpcional(
                        direccion
                );

        sexo =
                normalizarOpcional(
                        sexo
                );

        contactoEmergencia =
                normalizarOpcional(
                        contactoEmergencia
                );

        telefonoEmergencia =
                normalizarOpcional(
                        telefonoEmergencia
                );

        /*
         * ========================================================
         * VALIDACIÓN
         * ========================================================
         */

        validarDatos(
                nombres,
                apellidos,
                cedula,
                correo,
                telefono,
                password,
                confirmarPassword,
                preguntaSeguridad,
                respuestaSeguridad,
                fechaNacimiento,
                direccion,
                contactoEmergencia,
                telefonoEmergencia
        );

        /*
         * ========================================================
         * DUPLICADOS
         * ========================================================
         */

        verificarDuplicados(
                cedula,
                correo
        );

        /*
         * ========================================================
         * ROL PACIENTE
         * ========================================================
         */

        Rol rolPaciente =
                obtenerRolPaciente();

        /*
         * ========================================================
         * CONSTRUCCIÓN DE ENTIDADES
         * ========================================================
         */

        Usuario usuario =
                construirUsuario(
                        rolPaciente,
                        nombres,
                        apellidos,
                        cedula,
                        correo,
                        telefono,
                        password,
                        preguntaSeguridad,
                        respuestaSeguridad
                );

        Paciente paciente =
                construirPaciente(
                        usuario,
                        fechaNacimiento,
                        direccion,
                        sexo,
                        contactoEmergencia,
                        telefonoEmergencia
                );

        /*
         * ========================================================
         * TRANSACCIÓN
         * ========================================================
         */

        registrarTransaccion(
                usuario,
                paciente
        );

        return paciente;
    }

    /**
     * Valida todos los datos recibidos desde
     * la interfaz.
     */
    private void validarDatos(
            String nombres,
            String apellidos,
            String cedula,
            String correo,
            String telefono,
            String password,
            String confirmarPassword,
            PreguntaSeguridad preguntaSeguridad,
            String respuestaSeguridad,
            LocalDate fechaNacimiento,
            String direccion,
            String contactoEmergencia,
            String telefonoEmergencia
    ) {

        /*
         * --------------------------------------------------------
         * NOMBRES Y APELLIDOS
         * --------------------------------------------------------
         */

        ValidacionDatosUtil.validarNombrePersona(
                nombres,
                "Los nombres",
                100
        );

        ValidacionDatosUtil.validarNombrePersona(
                apellidos,
                "Los apellidos",
                100
        );

        /*
         * --------------------------------------------------------
         * IDENTIFICACIÓN Y CORREO
         * --------------------------------------------------------
         */

        ValidacionDatosUtil.validarCedula(
                cedula
        );

        ValidacionDatosUtil.validarCorreo(
                correo
        );

        /*
         * --------------------------------------------------------
         * TELÉFONO
         * --------------------------------------------------------
         */

        ValidacionDatosUtil.validarTelefonoOpcional(
                telefono,
                "El teléfono"
        );

        /*
         * --------------------------------------------------------
         * CONTRASEÑA
         * --------------------------------------------------------
         */

        validarPassword(
                password,
                confirmarPassword
        );

        /*
         * --------------------------------------------------------
         * SEGURIDAD DE CUENTA
         * --------------------------------------------------------
         */

        if (preguntaSeguridad == null) {

            throw new IllegalArgumentException(
                    "Debes seleccionar una pregunta de seguridad."
            );
        }

        RespuestaSeguridadUtil.validarParaRegistro(
                respuestaSeguridad
        );

        /*
         * --------------------------------------------------------
         * FECHA DE NACIMIENTO
         * --------------------------------------------------------
         */

        validarFechaNacimiento(
                fechaNacimiento
        );

        /*
         * --------------------------------------------------------
         * DIRECCIÓN
         * --------------------------------------------------------
         */

        if (
                direccion != null
                        && direccion.length() > 255
        ) {

            throw new IllegalArgumentException(
                    "La dirección no puede superar los 255 caracteres."
            );
        }

        /*
         * --------------------------------------------------------
         * CONTACTO DE EMERGENCIA
         * --------------------------------------------------------
         */

        ValidacionDatosUtil.validarNombrePersonaOpcional(
                contactoEmergencia,
                "El contacto de emergencia",
                150
        );

        ValidacionDatosUtil.validarTelefonoOpcional(
                telefonoEmergencia,
                "El teléfono de emergencia"
        );
    }

    /**
     * Valida contraseña y confirmación.
     */
    private void validarPassword(
            String password,
            String confirmarPassword
    ) {

        if (
                password == null
                        || password.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "La contraseña es obligatoria."
            );
        }

        if (
                password.length() < 8
        ) {

            throw new IllegalArgumentException(
                    "La contraseña debe contener al menos 8 caracteres."
            );
        }

        if (
                password.length()
                        > LONGITUD_MAX_PASSWORD
        ) {

            throw new IllegalArgumentException(
                    "La contraseña no puede superar los 64 caracteres."
            );
        }

        if (
                confirmarPassword == null
                        || !password.equals(
                                confirmarPassword
                        )
        ) {

            throw new IllegalArgumentException(
                    "Las contraseñas no coinciden."
            );
        }
    }

    /**
     * Valida la fecha de nacimiento.
     */
    private void validarFechaNacimiento(
            LocalDate fechaNacimiento
    ) {

        if (fechaNacimiento == null) {

            throw new IllegalArgumentException(
                    "La fecha de nacimiento es obligatoria."
            );
        }

        LocalDate hoy =
                LocalDate.now();

        if (
                fechaNacimiento.isAfter(
                        hoy
                )
        ) {

            throw new IllegalArgumentException(
                    "La fecha de nacimiento no puede ser futura."
            );
        }

        if (
                fechaNacimiento.isBefore(
                        hoy.minusYears(
                                120
                        )
                )
        ) {

            throw new IllegalArgumentException(
                    "La fecha de nacimiento ingresada no es válida."
            );
        }
    }

    /**
     * Comprueba que la cédula y el correo
     * todavía no existan.
     */
    private void verificarDuplicados(
            String cedula,
            String correo
    ) throws SQLException {

        Usuario usuarioCedula =
                usuarioDAO.buscarPorCedula(
                        cedula
                );

        if (usuarioCedula != null) {

            throw new IllegalArgumentException(
                    "Ya existe una cuenta registrada con esta cédula."
            );
        }

        Usuario usuarioCorreo =
                usuarioDAO.buscarPorCorreo(
                        correo
                );

        if (usuarioCorreo != null) {

            throw new IllegalArgumentException(
                    "Ya existe una cuenta registrada con este correo electrónico."
            );
        }
    }

    /**
     * Recupera el rol PACIENTE.
     */
    private Rol obtenerRolPaciente()
            throws SQLException {

        Rol rolPaciente =
                rolDAO.buscarPorNombre(
                        ROL_PACIENTE
                );

        if (
                rolPaciente == null
                        || !rolPaciente.isEstado()
        ) {

            throw new IllegalStateException(
                    "El rol PACIENTE no está disponible."
            );
        }

        return rolPaciente;
    }

    /**
     * Construye el usuario principal.
     */
    private Usuario construirUsuario(
            Rol rol,
            String nombres,
            String apellidos,
            String cedula,
            String correo,
            String telefono,
            String password,
            PreguntaSeguridad preguntaSeguridad,
            String respuestaSeguridad
    ) {

        Usuario usuario =
                new Usuario();

        usuario.setRol(
                rol
        );

        usuario.setNombres(
                nombres
        );

        usuario.setApellidos(
                apellidos
        );

        usuario.setCedula(
                cedula
        );

        usuario.setCorreo(
                correo
        );

        usuario.setTelefono(
                telefono
        );

        /*
         * Contraseña BCrypt.
         */
        usuario.setPasswordHash(
                PasswordUtil.generarHash(
                        password
                )
        );

        /*
         * Guardamos el código de la pregunta,
         * no el texto mostrado en pantalla.
         */
        usuario.setPreguntaSeguridad(
                preguntaSeguridad.getCodigo()
        );

        /*
         * La respuesta tampoco se guarda en texto plano.
         */
        usuario.setRespuestaSeguridadHash(
                RespuestaSeguridadUtil.generarHash(
                        respuestaSeguridad
                )
        );

        usuario.setIntentosRecuperacion(
                0
        );

        usuario.setBloqueadoRecuperacionHasta(
                null
        );

        usuario.setEstado(
                true
        );

        return usuario;
    }

    /**
     * Construye la información específica
     * del paciente.
     */
    private Paciente construirPaciente(
            Usuario usuario,
            LocalDate fechaNacimiento,
            String direccion,
            String sexo,
            String contactoEmergencia,
            String telefonoEmergencia
    ) {

        Paciente paciente =
                new Paciente();

        paciente.setUsuario(
                usuario
        );

        paciente.setFechaNacimiento(
                fechaNacimiento
        );

        paciente.setDireccion(
                direccion
        );

        paciente.setSexo(
                sexo
        );

        paciente.setContactoEmergencia(
                contactoEmergencia
        );

        paciente.setTelefonoEmergencia(
                telefonoEmergencia
        );

        return paciente;
    }

    /**
     * Guarda el usuario y su perfil de paciente dentro
     * de una única transacción.
     *
     * Si cualquiera de los INSERT falla, se ejecuta
     * rollback para evitar registros incompletos.
     */
    private void registrarTransaccion(
            Usuario usuario,
            Paciente paciente
    ) throws SQLException {

        try (
                Connection conexion =
                        ConexionBD.obtenerConexion()
        ) {

            boolean autoCommitOriginal =
                    conexion.getAutoCommit();

            try {

                conexion.setAutoCommit(
                        false
                );

                /*
                 * Primero se crea el usuario.
                 *
                 * UsuarioDAO asigna el ID generado
                 * al objeto Usuario.
                 */
                usuarioDAO.insertar(
                        conexion,
                        usuario
                );

                /*
                 * Después se crea el perfil del paciente
                 * utilizando el ID del usuario.
                 */
                pacienteDAO.insertar(
                        conexion,
                        paciente
                );

                conexion.commit();

            } catch (Exception error) {

                intentarRollback(
                        conexion,
                        error
                );

                if (
                        error
                                instanceof SQLException sqlException
                ) {

                    throw sqlException;
                }

                if (
                        error
                                instanceof RuntimeException runtimeException
                ) {

                    throw runtimeException;
                }

                throw new SQLException(
                        "No fue posible completar el registro del paciente.",
                        error
                );

            } finally {

                restaurarAutoCommit(
                        conexion,
                        autoCommitOriginal
                );
            }
        }
    }

    /**
     * Intenta revertir la transacción.
     */
    private void intentarRollback(
            Connection conexion,
            Exception errorOriginal
    ) {

        try {

            conexion.rollback();

        } catch (SQLException errorRollback) {

            /*
             * Conservamos ambos errores para diagnóstico
             * sin ocultar la causa original.
             */
            errorOriginal.addSuppressed(
                    errorRollback
            );
        }
    }

    /**
     * Restaura el estado original de autoCommit.
     */
    private void restaurarAutoCommit(
            Connection conexion,
            boolean autoCommitOriginal
    ) {

        try {

            conexion.setAutoCommit(
                    autoCommitOriginal
            );

        } catch (SQLException error) {

            /*
             * La conexión se cerrará inmediatamente
             * por try-with-resources.
             *
             * No interrumpimos un registro ya confirmado
             * por un error al restaurar autoCommit.
             */
            System.err.println(
                    "No fue posible restaurar autoCommit: "
                            + error.getMessage()
            );
        }
    }

    /**
     * Normaliza campos obligatorios simples.
     */
    private String normalizarObligatorio(
            String valor
    ) {

        if (valor == null) {
            return "";
        }

        return valor.trim();
    }

    /**
     * Convierte valores opcionales vacíos en null
     * y elimina espacios innecesarios.
     */
    private String normalizarOpcional(
            String valor
    ) {

        if (
                valor == null
                        || valor.isBlank()
        ) {

            return null;
        }

        return valor
                .trim()
                .replaceAll(
                        "\\s+",
                        " "
                );
    }
}