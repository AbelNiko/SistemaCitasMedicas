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

import java.sql.Connection;
import java.sql.SQLException;

import java.time.LocalDate;

import java.util.regex.Pattern;

/**
 * ================================================================
 *              SERVICE - REGISTRO DE PACIENTE
 * ================================================================
 *
 * Gestiona el proceso de creación de cuentas para pacientes.
 *
 * El registro público asigna exclusivamente el rol PACIENTE.
 *
 * También obliga al nuevo usuario a configurar una pregunta
 * de seguridad que podrá utilizar posteriormente durante
 * el proceso de recuperación de contraseña.
 *
 * La respuesta de seguridad nunca se almacena en texto plano.
 * Se normaliza y protege mediante BCrypt.
 *
 * El registro de Usuario y Paciente se realiza mediante una
 * transacción para garantizar la integridad de la información.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.1
 */
public class RegistroPacienteService {

    private static final String ROL_PACIENTE =
            "PACIENTE";

    private static final Pattern PATRON_CORREO =
            Pattern.compile(
                    "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
            );

    private final UsuarioDAO usuarioDAO;
    private final PacienteDAO pacienteDAO;
    private final RolDAO rolDAO;

    /**
     * Constructor principal.
     */
    public RegistroPacienteService() {

        this.usuarioDAO =
                new UsuarioDAO();

        this.pacienteDAO =
                new PacienteDAO();

        this.rolDAO =
                new RolDAO();
    }

    /**
     * Registra una nueva cuenta de paciente.
     *
     * @param nombres nombres.
     * @param apellidos apellidos.
     * @param cedula cédula.
     * @param correo correo electrónico.
     * @param telefono teléfono.
     * @param password contraseña.
     * @param confirmarPassword confirmación de contraseña.
     * @param preguntaSeguridad pregunta seleccionada.
     * @param respuestaSeguridad respuesta proporcionada.
     * @param fechaNacimiento fecha de nacimiento.
     * @param direccion dirección.
     * @param sexo sexo.
     * @param contactoEmergencia contacto de emergencia.
     * @param telefonoEmergencia teléfono de emergencia.
     * @return paciente registrado.
     * @throws SQLException si ocurre un error.
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
         * VALIDACIÓN
         * ========================================================
         */

        validarDatos(
                nombres,
                apellidos,
                cedula,
                correo,
                password,
                confirmarPassword,
                preguntaSeguridad,
                respuestaSeguridad,
                fechaNacimiento
        );

        /*
         * ========================================================
         * NORMALIZACIÓN
         * ========================================================
         */

        String cedulaNormalizada =
                cedula.trim();

        String correoNormalizado =
                correo
                        .trim()
                        .toLowerCase();

        /*
         * ========================================================
         * DUPLICADOS
         * ========================================================
         */

        verificarDuplicados(
                cedulaNormalizada,
                correoNormalizado
        );

        /*
         * ========================================================
         * ROL
         * ========================================================
         */

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

        /*
         * ========================================================
         * CONSTRUCCIÓN DEL USUARIO
         * ========================================================
         */

        Usuario usuario =
                construirUsuario(
                        rolPaciente,
                        nombres,
                        apellidos,
                        cedulaNormalizada,
                        correoNormalizado,
                        telefono,
                        password,
                        preguntaSeguridad,
                        respuestaSeguridad
                );

        /*
         * ========================================================
         * CONSTRUCCIÓN DEL PACIENTE
         * ========================================================
         */

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
         * REGISTRO TRANSACCIONAL
         * ========================================================
         */

        registrarTransaccion(
                usuario,
                paciente
        );

        return paciente;
    }

    /**
     * Comprueba que no exista otro usuario con la misma
     * cédula o correo electrónico.
     */
    private void verificarDuplicados(
            String cedula,
            String correo
    ) throws SQLException {

        if (
                usuarioDAO.buscarPorCedula(
                        cedula
                ) != null
        ) {

            throw new IllegalArgumentException(
                    "Ya existe una cuenta registrada con esta cédula."
            );
        }

        if (
                usuarioDAO.buscarPorCorreo(
                        correo
                ) != null
        ) {

            throw new IllegalArgumentException(
                    "Ya existe una cuenta registrada con este correo electrónico."
            );
        }
    }

    /**
     * Construye el usuario que será almacenado.
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
                nombres.trim()
        );

        usuario.setApellidos(
                apellidos.trim()
        );

        usuario.setCedula(
                cedula
        );

        usuario.setCorreo(
                correo
        );

        usuario.setTelefono(
                normalizarOpcional(
                        telefono
                )
        );

        /*
         * ========================================================
         * CONTRASEÑA
         * ========================================================
         */

        usuario.setPasswordHash(
                PasswordUtil.generarHash(
                        password
                )
        );

        /*
         * ========================================================
         * PREGUNTA DE SEGURIDAD
         * ========================================================
         *
         * Se almacena el código de la pregunta y no su texto.
         */

        usuario.setPreguntaSeguridad(
                preguntaSeguridad.getCodigo()
        );

        /*
         * Nunca almacenamos la respuesta original.
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
     * Construye el perfil específico del paciente.
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
                normalizarOpcional(
                        direccion
                )
        );

        paciente.setSexo(
                normalizarOpcional(
                        sexo
                )
        );

        paciente.setContactoEmergencia(
                normalizarOpcional(
                        contactoEmergencia
                )
        );

        paciente.setTelefonoEmergencia(
                normalizarOpcional(
                        telefonoEmergencia
                )
        );

        return paciente;
    }

    /**
     * Guarda Usuario y Paciente dentro de una misma transacción.
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

                usuarioDAO.insertar(
                        conexion,
                        usuario
                );

                pacienteDAO.insertar(
                        conexion,
                        paciente
                );

                conexion.commit();

            } catch (Exception e) {

                try {

                    conexion.rollback();

                } catch (SQLException rollbackError) {

                    e.addSuppressed(
                            rollbackError
                    );
                }

                if (
                        e instanceof SQLException sqlException
                ) {

                    throw sqlException;
                }

                if (
                        e instanceof RuntimeException runtimeException
                ) {

                    throw runtimeException;
                }

                throw new SQLException(
                        "No fue posible completar el registro del paciente.",
                        e
                );

            } finally {

                try {

                    conexion.setAutoCommit(
                            autoCommitOriginal
                    );

                } catch (SQLException ignored) {

                    /*
                     * La conexión será cerrada inmediatamente.
                     */
                }
            }
        }
    }

    /**
     * Valida los datos obligatorios del formulario.
     */
    private void validarDatos(
            String nombres,
            String apellidos,
            String cedula,
            String correo,
            String password,
            String confirmarPassword,
            PreguntaSeguridad preguntaSeguridad,
            String respuestaSeguridad,
            LocalDate fechaNacimiento
    ) {

        validarTextoObligatorio(
                nombres,
                "Los nombres son obligatorios."
        );

        validarTextoObligatorio(
                apellidos,
                "Los apellidos son obligatorios."
        );

        validarCedula(
                cedula
        );

        validarCorreo(
                correo
        );

        validarPassword(
                password,
                confirmarPassword
        );

        /*
         * ========================================================
         * SEGURIDAD DE LA CUENTA
         * ========================================================
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
         * ========================================================
         * FECHA DE NACIMIENTO
         * ========================================================
         */

        if (fechaNacimiento == null) {

            throw new IllegalArgumentException(
                    "La fecha de nacimiento es obligatoria."
            );
        }

        if (
                fechaNacimiento.isAfter(
                        LocalDate.now()
                )
        ) {

            throw new IllegalArgumentException(
                    "La fecha de nacimiento no puede ser futura."
            );
        }
    }

    /**
     * Valida un texto obligatorio.
     */
    private void validarTextoObligatorio(
            String valor,
            String mensaje
    ) {

        if (
                valor == null
                        || valor.isBlank()
        ) {

            throw new IllegalArgumentException(
                    mensaje
            );
        }
    }

    /**
     * Valida la cédula.
     */
    private void validarCedula(
            String cedula
    ) {

        validarTextoObligatorio(
                cedula,
                "La cédula es obligatoria."
        );

        if (
                !cedula
                        .trim()
                        .matches("\\d{10}")
        ) {

            throw new IllegalArgumentException(
                    "La cédula debe contener exactamente 10 dígitos."
            );
        }
    }

    /**
     * Valida el correo electrónico.
     */
    private void validarCorreo(
            String correo
    ) {

        validarTextoObligatorio(
                correo,
                "El correo electrónico es obligatorio."
        );

        if (
                !PATRON_CORREO
                        .matcher(
                                correo.trim()
                        )
                        .matches()
        ) {

            throw new IllegalArgumentException(
                    "El formato del correo electrónico no es válido."
            );
        }
    }

    /**
     * Valida contraseña y confirmación.
     */
    private void validarPassword(
            String password,
            String confirmarPassword
    ) {

        validarTextoObligatorio(
                password,
                "La contraseña es obligatoria."
        );

        if (
                password.length() < 8
        ) {

            throw new IllegalArgumentException(
                    "La contraseña debe contener al menos 8 caracteres."
            );
        }

        if (
                !password.equals(
                        confirmarPassword
                )
        ) {

            throw new IllegalArgumentException(
                    "Las contraseñas no coinciden."
            );
        }
    }

    /**
     * Convierte cadenas opcionales vacías en null.
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

        return valor.trim();
    }
}