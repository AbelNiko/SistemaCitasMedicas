package com.citasmedicas.dao;

import com.citasmedicas.model.Paciente;
import com.citasmedicas.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * ================================================================
 *                  DAO - PACIENTE
 * ================================================================
 *
 * Gestiona las operaciones de acceso a datos relacionadas
 * con la tabla pacientes.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public class PacienteDAO {

    /**
     * Registra un paciente utilizando una conexión existente.
     *
     * Este método permite que el registro de Usuario y Paciente
     * pueda ejecutarse dentro de una misma transacción.
     *
     * @param conexion conexión activa con MySQL.
     * @param paciente paciente que será registrado.
     * @return identificador generado para el paciente.
     * @throws SQLException si ocurre un error en la base de datos.
     */
    public int insertar(
            Connection conexion,
            Paciente paciente
    ) throws SQLException {

        String sql =
                """
                INSERT INTO pacientes (
                    id_usuario,
                    fecha_nacimiento,
                    direccion,
                    sexo,
                    contacto_emergencia,
                    telefono_emergencia
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (
                PreparedStatement sentencia =
                        conexion.prepareStatement(
                                sql,
                                Statement.RETURN_GENERATED_KEYS
                        )
        ) {

            sentencia.setInt(
                    1,
                    paciente.getUsuario().getIdUsuario()
            );

            if (paciente.getFechaNacimiento() != null) {

                sentencia.setDate(
                        2,
                        java.sql.Date.valueOf(
                                paciente.getFechaNacimiento()
                        )
                );

            } else {

                sentencia.setNull(
                        2,
                        java.sql.Types.DATE
                );
            }

            sentencia.setString(
                    3,
                    paciente.getDireccion()
            );

            sentencia.setString(
                    4,
                    paciente.getSexo()
            );

            sentencia.setString(
                    5,
                    paciente.getContactoEmergencia()
            );

            sentencia.setString(
                    6,
                    paciente.getTelefonoEmergencia()
            );

            int filasAfectadas =
                    sentencia.executeUpdate();

            if (filasAfectadas == 0) {

                throw new SQLException(
                        "No fue posible registrar el paciente."
                );
            }

            try (
                    ResultSet claves =
                            sentencia.getGeneratedKeys()
            ) {

                if (claves.next()) {

                    int idPaciente =
                            claves.getInt(1);

                    paciente.setIdPaciente(
                            idPaciente
                    );

                    return idPaciente;
                }
            }

            throw new SQLException(
                    "No fue posible obtener el identificador del paciente."
            );
        }
    }

    /**
     * Busca un paciente mediante el identificador
     * del usuario asociado.
     *
     * @param idUsuario identificador del usuario.
     * @return paciente encontrado o null.
     * @throws SQLException si ocurre un error.
     */
    public Paciente buscarPorIdUsuario(
            int idUsuario
    ) throws SQLException {

        String sql =
                """
                SELECT
                    id_paciente,
                    fecha_nacimiento,
                    direccion,
                    sexo,
                    contacto_emergencia,
                    telefono_emergencia
                FROM pacientes
                WHERE id_usuario = ?
                """;

        try (
                Connection conexion =
                        ConexionBD.obtenerConexion();

                PreparedStatement sentencia =
                        conexion.prepareStatement(sql)
        ) {

            sentencia.setInt(
                    1,
                    idUsuario
            );

            try (
                    ResultSet resultado =
                            sentencia.executeQuery()
            ) {

                if (!resultado.next()) {
                    return null;
                }

                Paciente paciente =
                        new Paciente();

                paciente.setIdPaciente(
                        resultado.getInt(
                                "id_paciente"
                        )
                );

                java.sql.Date fecha =
                        resultado.getDate(
                                "fecha_nacimiento"
                        );

                if (fecha != null) {

                    paciente.setFechaNacimiento(
                            fecha.toLocalDate()
                    );
                }

                paciente.setDireccion(
                        resultado.getString(
                                "direccion"
                        )
                );

                paciente.setSexo(
                        resultado.getString(
                                "sexo"
                        )
                );

                paciente.setContactoEmergencia(
                        resultado.getString(
                                "contacto_emergencia"
                        )
                );

                paciente.setTelefonoEmergencia(
                        resultado.getString(
                                "telefono_emergencia"
                        )
                );

                return paciente;
            }
        }
    }
}