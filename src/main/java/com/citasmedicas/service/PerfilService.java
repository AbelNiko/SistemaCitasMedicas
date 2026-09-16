package com.citasmedicas.service;

import com.citasmedicas.dao.PacienteDAO;
import com.citasmedicas.model.Paciente;
import com.citasmedicas.model.Usuario;
import com.citasmedicas.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

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
 * @version 1.1
 */
public class PerfilService {

    private final PacienteDAO pacienteDAO;

    public PerfilService() {

        this.pacienteDAO =
                new PacienteDAO();
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
     * Actualiza los datos editables del usuario
     * y del paciente dentro de una única transacción.
     */
    public void actualizarPerfil(
            Usuario usuario,
            int idPaciente,
            String nombres,
            String apellidos,
            String correo,
            String telefono,
            String direccion
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

        nombres = limpiar(nombres);
        apellidos = limpiar(apellidos);
        correo = limpiar(correo).toLowerCase();
        telefono = limpiar(telefono);
        direccion = limpiar(direccion);

        validarDatos(
                nombres,
                apellidos,
                correo,
                telefono,
                direccion
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
                SET direccion = ?
                WHERE id_paciente = ?
                  AND id_usuario = ?
                """;

        try (
                Connection conexion =
                        ConexionBD.obtenerConexion()
        ) {

            /*
             * Desactivamos temporalmente el autocommit.
             * Los dos UPDATE deben completarse correctamente
             * antes de confirmar la operación.
             */
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
                 * ACTUALIZACIÓN DE USUARIOS
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

                if (telefono.isEmpty()) {

                    stmtUsuario.setNull(
                            4,
                            Types.VARCHAR
                    );

                } else {

                    stmtUsuario.setString(
                            4,
                            telefono
                    );
                }

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
                 * ACTUALIZACIÓN DE PACIENTES
                 * =================================================
                 */

                if (direccion.isEmpty()) {

                    stmtPaciente.setNull(
                            1,
                            Types.VARCHAR
                    );

                } else {

                    stmtPaciente.setString(
                            1,
                            direccion
                    );
                }

                stmtPaciente.setInt(
                        2,
                        idPaciente
                );

                /*
                 * También comprobamos que el paciente
                 * pertenezca al usuario autenticado.
                 */
                stmtPaciente.setInt(
                        3,
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
                 * Los dos UPDATE finalizaron correctamente.
                 */
                conexion.commit();

                /*
                 * Solo después del COMMIT actualizamos
                 * el objeto que permanece en memoria.
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

                /*
                 * Si cualquiera de las operaciones falla,
                 * revertimos todos los cambios.
                 */
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

                throw e;
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
            String direccion
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