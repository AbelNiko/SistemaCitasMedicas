package com.citasmedicas.service;

import com.citasmedicas.dao.PacienteDAO;
import com.citasmedicas.dao.RolDAO;
import com.citasmedicas.dao.UsuarioDAO;
import com.citasmedicas.model.Paciente;
import com.citasmedicas.model.Rol;
import com.citasmedicas.model.Usuario;
import com.citasmedicas.util.ConexionBD;
import com.citasmedicas.util.PasswordUtil;

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
 * Las cuentas de médicos y personal administrativo deben ser
 * gestionadas mediante los mecanismos administrativos del sistema.
 *
 * El registro de Usuario y Paciente se realiza mediante una
 * transacción para garantizar la integridad de la información.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
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
     * @param nombres nombres del paciente.
     * @param apellidos apellidos del paciente.
     * @param cedula cédula del paciente.
     * @param correo correo electrónico.
     * @param telefono teléfono.
     * @param password contraseña.
     * @param confirmarPassword confirmación de contraseña.
     * @param fechaNacimiento fecha de nacimiento.
     * @param direccion dirección domiciliaria.
     * @param sexo sexo registrado.
     * @param contactoEmergencia contacto de emergencia.
     * @param telefonoEmergencia teléfono de emergencia.
     * @return paciente registrado.
     * @throws SQLException si ocurre un error de base de datos.
     */
    public Paciente registrar(
            String nombres,
            String apellidos,
            String cedula,
            String correo,
            String telefono,
            String password,
            String confirmarPassword,
            LocalDate fechaNacimiento,
            String direccion,
            String sexo,
            String contactoEmergencia,
            String telefonoEmergencia
    ) throws SQLException {

        validarDatos(
                nombres,
                apellidos,
                cedula,
                correo,
                password,
                confirmarPassword,
                fechaNacimiento
        );

        String cedulaNormalizada =
                cedula.trim();

        String correoNormalizado =
                correo.trim().toLowerCase();

        verificarDuplicados(
                cedulaNormalizada,
                correoNormalizado
        );

        Rol rolPaciente =
                rolDAO.buscarPorNombre(
                        ROL_PACIENTE
                );

        if (rolPaciente == null
                || !rolPaciente.isEstado()) {

            throw new IllegalStateException(
                    "El rol PACIENTE no está disponible."
            );
        }

        Usuario usuario =
                construirUsuario(
                        rolPaciente,
                        nombres,
                        apellidos,
                        cedulaNormalizada,
                        correoNormalizado,
                        telefono,
                        password
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

        if (usuarioDAO.buscarPorCedula(cedula) != null) {

            throw new IllegalArgumentException(
                    "Ya existe una cuenta registrada con esta cédula."
            );
        }

        if (usuarioDAO.buscarPorCorreo(correo) != null) {

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
            String password
    ) {

        Usuario usuario =
                new Usuario();

        usuario.setRol(rol);

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
                normalizarOpcional(telefono)
        );

        usuario.setPasswordHash(
                PasswordUtil.generarHash(password)
        );

        usuario.setEstado(true);

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

        paciente.setUsuario(usuario);

        paciente.setFechaNacimiento(
                fechaNacimiento
        );

        paciente.setDireccion(
                normalizarOpcional(direccion)
        );

        paciente.setSexo(
                normalizarOpcional(sexo)
        );

        paciente.setContactoEmergencia(
                normalizarOpcional(contactoEmergencia)
        );

        paciente.setTelefonoEmergencia(
                normalizarOpcional(telefonoEmergencia)
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

                conexion.setAutoCommit(false);

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
                    e.addSuppressed(rollbackError);
                }

                if (e instanceof SQLException sqlException) {
                    throw sqlException;
                }

                if (e instanceof RuntimeException runtimeException) {
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
                    // La conexión será cerrada inmediatamente.
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

        validarCedula(cedula);

        validarCorreo(correo);

        validarPassword(
                password,
                confirmarPassword
        );

        if (fechaNacimiento == null) {

            throw new IllegalArgumentException(
                    "La fecha de nacimiento es obligatoria."
            );
        }

        if (fechaNacimiento.isAfter(LocalDate.now())) {

            throw new IllegalArgumentException(
                    "La fecha de nacimiento no puede ser futura."
            );
        }
    }

    /**
     * Valida un campo de texto obligatorio.
     */
    private void validarTextoObligatorio(
            String valor,
            String mensaje
    ) {

        if (valor == null || valor.isBlank()) {

            throw new IllegalArgumentException(
                    mensaje
            );
        }
    }

    /**
     * Valida el formato básico de la cédula.
     */
    private void validarCedula(String cedula) {

        validarTextoObligatorio(
                cedula,
                "La cédula es obligatoria."
        );

        if (!cedula.trim().matches("\\d{10}")) {

            throw new IllegalArgumentException(
                    "La cédula debe contener exactamente 10 dígitos."
            );
        }
    }

    /**
     * Valida el correo electrónico.
     */
    private void validarCorreo(String correo) {

        validarTextoObligatorio(
                correo,
                "El correo electrónico es obligatorio."
        );

        if (!PATRON_CORREO
                .matcher(correo.trim())
                .matches()) {

            throw new IllegalArgumentException(
                    "El formato del correo electrónico no es válido."
            );
        }
    }

    /**
     * Valida la contraseña y su confirmación.
     */
    private void validarPassword(
            String password,
            String confirmarPassword
    ) {

        validarTextoObligatorio(
                password,
                "La contraseña es obligatoria."
        );

        if (password.length() < 8) {

            throw new IllegalArgumentException(
                    "La contraseña debe contener al menos 8 caracteres."
            );
        }

        if (!password.equals(confirmarPassword)) {

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

        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim();
    }
}