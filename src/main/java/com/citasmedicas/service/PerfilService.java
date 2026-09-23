package com.citasmedicas.service;

import com.citasmedicas.dao.PacienteDAO;
import com.citasmedicas.model.Paciente;
import com.citasmedicas.model.Usuario;
import com.citasmedicas.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.Set;

/**
 * ================================================================
 *                   SERVICE - PERFIL
 * ================================================================
 *
 * Gestiona la consulta y actualización del perfil del paciente.
 *
 * La actualización de usuarios y pacientes se realiza dentro
 * de una única transacción para mantener la consistencia
 * de la información.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.2
 */
public class PerfilService {

    private static final Set<String> TIPOS_SANGRE_VALIDOS =
            Set.of(
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

    private static final Set<String> SEXOS_VALIDOS =
            Set.of(
                    "Masculino",
                    "Femenino",
                    "Otro",
                    "Prefiero no indicar"
            );

    private final PacienteDAO pacienteDAO;

    public PerfilService() {
        this.pacienteDAO = new PacienteDAO();
    }

    /**
     * Obtiene el perfil de paciente asociado
     * al usuario autenticado.
     */
    public Paciente obtenerPaciente(
            int idUsuario
    ) throws SQLException {

        if (idUsuario <= 0) {
            throw new IllegalArgumentException(
                    "El usuario no es válido."
            );
        }

        return pacienteDAO.buscarPorIdUsuario(
                idUsuario
        );
    }

    /**
     * Actualiza los datos personales, de contacto
     * y médicos básicos del paciente.
     *
     * Los cambios de usuarios y pacientes se realizan
     * dentro de una misma transacción.
     */
    public void actualizarPerfil(
            Usuario usuario,
            int idPaciente,
            String nombres,
            String apellidos,
            String correo,
            String telefono,
            LocalDate fechaNacimiento,
            String sexo,
            String direccion,
            String tipoSangre,
            String alergias,
            String condicionesMedicas,
            String contactoEmergencia,
            String telefonoEmergencia
    ) throws SQLException {

        if (usuario == null) {
            throw new IllegalArgumentException(
                    "No existe un usuario autenticado."
            );
        }

        if (idPaciente <= 0) {
            throw new IllegalArgumentException(
                    "El paciente no es válido."
            );
        }

        /*
         * Normalización de los datos recibidos.
         */
        nombres = limpiar(nombres);
        apellidos = limpiar(apellidos);
        correo = limpiar(correo).toLowerCase();
        telefono = limpiar(telefono);

        sexo = limpiar(sexo);
        direccion = limpiar(direccion);
        tipoSangre = limpiar(tipoSangre);
        alergias = limpiar(alergias);
        condicionesMedicas =
                limpiar(condicionesMedicas);
        contactoEmergencia =
                limpiar(contactoEmergencia);
        telefonoEmergencia =
                limpiar(telefonoEmergencia);

        /*
         * Validación antes de acceder a la base de datos.
         */
        validarDatos(
                nombres,
                apellidos,
                correo,
                telefono,
                fechaNacimiento,
                sexo,
                direccion,
                tipoSangre,
                alergias,
                condicionesMedicas,
                contactoEmergencia,
                telefonoEmergencia
        );

        String sqlUsuario = """
                UPDATE usuarios
                SET
                    nombres = ?,
                    apellidos = ?,
                    correo = ?,
                    telefono = ?
                WHERE id_usuario = ?
                """;

        String sqlPaciente = """
                UPDATE pacientes
                SET
                    fecha_nacimiento = ?,
                    direccion = ?,
                    sexo = ?,
                    tipo_sangre = ?,
                    alergias = ?,
                    condiciones_medicas = ?,
                    contacto_emergencia = ?,
                    telefono_emergencia = ?
                WHERE id_paciente = ?
                  AND id_usuario = ?
                """;

        try (
                Connection conexion =
                        ConexionBD.obtenerConexion()
        ) {

            conexion.setAutoCommit(false);

            try (
                    PreparedStatement stmtUsuario =
                            conexion.prepareStatement(
                                    sqlUsuario
                            );

                    PreparedStatement stmtPaciente =
                            conexion.prepareStatement(
                                    sqlPaciente
                            )
            ) {

                /*
                 * =================================================
                 * USUARIO
                 * =================================================
                 */

                stmtUsuario.setString(
                        1,
                        nombres
                );

                stmtUsuario.setString(
                        2,
                        apellidos
                );

                stmtUsuario.setString(
                        3,
                        correo
                );

                asignarTextoOpcional(
                        stmtUsuario,
                        4,
                        telefono
                );

                stmtUsuario.setInt(
                        5,
                        usuario.getIdUsuario()
                );

                int usuariosActualizados =
                        stmtUsuario.executeUpdate();

                if (usuariosActualizados != 1) {
                    throw new SQLException(
                            "No fue posible actualizar "
                            + "los datos del usuario."
                    );
                }

                /*
                 * =================================================
                 * PACIENTE
                 * =================================================
                 */

                if (fechaNacimiento == null) {

                    stmtPaciente.setNull(
                            1,
                            Types.DATE
                    );

                } else {

                    stmtPaciente.setDate(
                            1,
                            java.sql.Date.valueOf(
                                    fechaNacimiento
                            )
                    );
                }

                asignarTextoOpcional(
                        stmtPaciente,
                        2,
                        direccion
                );

                asignarTextoOpcional(
                        stmtPaciente,
                        3,
                        sexo
                );

                asignarTextoOpcional(
                        stmtPaciente,
                        4,
                        tipoSangre
                );

                asignarTextoOpcional(
                        stmtPaciente,
                        5,
                        alergias
                );

                asignarTextoOpcional(
                        stmtPaciente,
                        6,
                        condicionesMedicas
                );

                asignarTextoOpcional(
                        stmtPaciente,
                        7,
                        contactoEmergencia
                );

                asignarTextoOpcional(
                        stmtPaciente,
                        8,
                        telefonoEmergencia
                );

                stmtPaciente.setInt(
                        9,
                        idPaciente
                );

                /*
                 * Garantiza que el paciente modificado
                 * pertenezca al usuario autenticado.
                 */
                stmtPaciente.setInt(
                        10,
                        usuario.getIdUsuario()
                );

                int pacientesActualizados =
                        stmtPaciente.executeUpdate();

                if (pacientesActualizados != 1) {
                    throw new SQLException(
                            "No fue posible actualizar "
                            + "los datos del paciente."
                    );
                }

                /*
                 * Confirmamos únicamente cuando ambas
                 * actualizaciones fueron exitosas.
                 */
                conexion.commit();

                /*
                 * Sincronizamos el Usuario mantenido
                 * actualmente en memoria.
                 */
                usuario.setNombres(
                        nombres
                );

                usuario.setApellidos(
                        apellidos
                );

                usuario.setCorreo(
                        correo
                );

                usuario.setTelefono(
                        telefono.isEmpty()
                                ? null
                                : telefono
                );

            } catch (Exception e) {

                try {
                    conexion.rollback();

                } catch (SQLException rollbackError) {
                    e.addSuppressed(
                            rollbackError
                    );
                }

                if (e instanceof SQLException sqlException) {
                    throw sqlException;
                }

                if (
                        e instanceof
                        IllegalArgumentException illegalArgumentException
                ) {
                    throw illegalArgumentException;
                }

                throw new SQLException(
                        "No fue posible actualizar el perfil.",
                        e
                );
            }
        }
    }

    /**
     * Valida los datos editables del perfil.
     */
    private void validarDatos(
            String nombres,
            String apellidos,
            String correo,
            String telefono,
            LocalDate fechaNacimiento,
            String sexo,
            String direccion,
            String tipoSangre,
            String alergias,
            String condicionesMedicas,
            String contactoEmergencia,
            String telefonoEmergencia
    ) {

        if (nombres.isEmpty()) {
            throw new IllegalArgumentException(
                    "Los nombres son obligatorios."
            );
        }

        if (apellidos.isEmpty()) {
            throw new IllegalArgumentException(
                    "Los apellidos son obligatorios."
            );
        }

        if (correo.isEmpty()) {
            throw new IllegalArgumentException(
                    "El correo electrónico es obligatorio."
            );
        }

        if (
                !correo.matches(
                        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
                )
        ) {
            throw new IllegalArgumentException(
                    "El correo electrónico no tiene "
                    + "un formato válido."
            );
        }

        if (
                !telefono.isEmpty()
                && !telefono.matches("\\d{7,10}")
        ) {
            throw new IllegalArgumentException(
                    "El teléfono debe contener "
                    + "entre 7 y 10 dígitos."
            );
        }

        if (
                fechaNacimiento != null
                && fechaNacimiento.isAfter(
                        LocalDate.now()
                )
        ) {
            throw new IllegalArgumentException(
                    "La fecha de nacimiento no puede "
                    + "ser posterior a la fecha actual."
            );
        }

        if (
                !sexo.isEmpty()
                && !SEXOS_VALIDOS.contains(sexo)
        ) {
            throw new IllegalArgumentException(
                    "El sexo seleccionado no es válido."
            );
        }

        if (
                !tipoSangre.isEmpty()
                && !TIPOS_SANGRE_VALIDOS.contains(
                        tipoSangre
                )
        ) {
            throw new IllegalArgumentException(
                    "El tipo de sangre seleccionado "
                    + "no es válido."
            );
        }

        if (
                !telefonoEmergencia.isEmpty()
                && !telefonoEmergencia.matches(
                        "\\d{7,10}"
                )
        ) {
            throw new IllegalArgumentException(
                    "El teléfono de emergencia debe "
                    + "contener entre 7 y 10 dígitos."
            );
        }

        if (nombres.length() > 100) {
            throw new IllegalArgumentException(
                    "Los nombres son demasiado largos."
            );
        }

        if (apellidos.length() > 100) {
            throw new IllegalArgumentException(
                    "Los apellidos son demasiado largos."
            );
        }

        if (correo.length() > 150) {
            throw new IllegalArgumentException(
                    "El correo electrónico es demasiado largo."
            );
        }

        if (direccion.length() > 255) {
            throw new IllegalArgumentException(
                    "La dirección no puede superar "
                    + "los 255 caracteres."
            );
        }

        if (tipoSangre.length() > 15) {
            throw new IllegalArgumentException(
                    "El tipo de sangre no es válido."
            );
        }

        if (alergias.length() > 500) {
            throw new IllegalArgumentException(
                    "Las alergias no pueden superar "
                    + "los 500 caracteres."
            );
        }

        if (condicionesMedicas.length() > 500) {
            throw new IllegalArgumentException(
                    "Las condiciones médicas no pueden "
                    + "superar los 500 caracteres."
            );
        }

        if (contactoEmergencia.length() > 150) {
            throw new IllegalArgumentException(
                    "El contacto de emergencia no puede "
                    + "superar los 150 caracteres."
            );
        }
    }

    /**
     * Asigna NULL a campos opcionales vacíos.
     */
    private void asignarTextoOpcional(
            PreparedStatement sentencia,
            int indice,
            String valor
    ) throws SQLException {

        if (valor == null || valor.isBlank()) {

            sentencia.setNull(
                    indice,
                    Types.VARCHAR
            );

        } else {

            sentencia.setString(
                    indice,
                    valor.trim()
            );
        }
    }

    /**
     * Normaliza valores ingresados por el usuario.
     */
    private String limpiar(
            String valor
    ) {

        return valor == null
                ? ""
                : valor.trim();
    }
}