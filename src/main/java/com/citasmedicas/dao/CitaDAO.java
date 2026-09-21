package com.citasmedicas.dao;

import com.citasmedicas.model.Cita;
import com.citasmedicas.util.ConexionBD;

import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * ================================================================
 *                         DAO - CITA
 * ================================================================
 *
 * Gestiona las operaciones de acceso a datos relacionadas
 * con las citas médicas.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public class CitaDAO {

    /**
     * Comprueba si existe una cita activa que se solape
     * con el intervalo solicitado.
     */
    public boolean existeSolapamiento(
            int idMedico,
            int idEstablecimiento,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin
    ) throws SQLException {

        try (Connection conexion =
                     ConexionBD.obtenerConexion()) {

            return existeSolapamiento(
                    conexion,
                    idMedico,
                    idEstablecimiento,
                    fecha,
                    horaInicio,
                    horaFin
            );
        }
    }

    /**
     * Comprueba solapamientos utilizando una conexión existente.
     *
     * Este método permite realizar la comprobación dentro de
     * la misma transacción utilizada para registrar la cita.
     */
    public boolean existeSolapamiento(
            Connection conexion,
            int idMedico,
            int idEstablecimiento,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin
    ) throws SQLException {

        String sql = """
                SELECT COUNT(*) AS total
                FROM citas
                WHERE id_medico = ?
                  AND id_establecimiento = ?
                  AND fecha_cita = ?
                  AND estado IN ('PROGRAMADA', 'CONFIRMADA')
                  AND hora_inicio < ?
                  AND hora_fin > ?
                """;

        try (PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idMedico);
            sentencia.setInt(2, idEstablecimiento);

            sentencia.setDate(
                    3,
                    java.sql.Date.valueOf(fecha)
            );

            sentencia.setTime(
                    4,
                    java.sql.Time.valueOf(horaFin)
            );

            sentencia.setTime(
                    5,
                    java.sql.Time.valueOf(horaInicio)
            );

            try (ResultSet resultado =
                         sentencia.executeQuery()) {

                return resultado.next()
                        && resultado.getInt("total") > 0;
            }
        }
    }

    /**
     * Inserta una nueva cita utilizando una conexión existente.
     *
     * @return identificador generado para la cita.
     */
    public int insertar(
            Connection conexion,
            Cita cita
    ) throws SQLException {

        String sql = """
                INSERT INTO citas (
                    id_paciente,
                    id_medico,
                    id_especialidad,
                    id_establecimiento,
                    fecha_cita,
                    hora_inicio,
                    hora_fin,
                    estado,
                    motivo_consulta
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement sentencia =
                     conexion.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS
                     )) {

            sentencia.setInt(
                    1,
                    cita.getIdPaciente()
            );

            sentencia.setInt(
                    2,
                    cita.getIdMedico()
            );

            sentencia.setInt(
                    3,
                    cita.getIdEspecialidad()
            );

            sentencia.setInt(
                    4,
                    cita.getIdEstablecimiento()
            );

            sentencia.setDate(
                    5,
                    java.sql.Date.valueOf(
                            cita.getFechaCita()
                    )
            );

            sentencia.setTime(
                    6,
                    java.sql.Time.valueOf(
                            cita.getHoraInicio()
                    )
            );

            sentencia.setTime(
                    7,
                    java.sql.Time.valueOf(
                            cita.getHoraFin()
                    )
            );

            sentencia.setString(
                    8,
                    cita.getEstado()
            );

            sentencia.setString(
                    9,
                    cita.getMotivoConsulta()
            );

            int filas =
                    sentencia.executeUpdate();

            if (filas == 0) {
                throw new SQLException(
                        "No fue posible registrar la cita."
                );
            }

            try (ResultSet claves =
                         sentencia.getGeneratedKeys()) {

                if (claves.next()) {

                    int idCita =
                            claves.getInt(1);

                    cita.setIdCita(idCita);

                    return idCita;
                }
            }

            throw new SQLException(
                    "La cita fue registrada, pero no se obtuvo su identificador."
            );
        }
    }
    /**
 * Obtiene las citas de un paciente con la información
 * descriptiva necesaria para mostrarlas en la interfaz.
 */
public List<Cita> listarPorPaciente(
        int idPaciente
) throws SQLException {

    String sql = """
            SELECT
                c.id_cita,
                c.id_paciente,
                c.id_medico,
                c.id_especialidad,
                c.id_establecimiento,
                c.fecha_cita,
                c.hora_inicio,
                c.hora_fin,
                c.estado,
                c.motivo_consulta,
                c.observacion,
                c.motivo_cancelacion,
                c.fecha_creacion,
                c.fecha_actualizacion,
                CONCAT(
                    u.nombres,
                    ' ',
                    u.apellidos
                ) AS nombre_medico,
                e.nombre AS nombre_especialidad,
                est.nombre AS nombre_establecimiento
            FROM citas c
            INNER JOIN medicos m
                ON c.id_medico = m.id_medico
            INNER JOIN usuarios u
                ON m.id_usuario = u.id_usuario
            INNER JOIN especialidades e
                ON c.id_especialidad = e.id_especialidad
            INNER JOIN establecimientos est
                ON c.id_establecimiento = est.id_establecimiento
            WHERE c.id_paciente = ?
            ORDER BY
                c.fecha_cita ASC,
                c.hora_inicio ASC
            """;

    List<Cita> citas =
            new ArrayList<>();

    try (
            Connection conexion =
                    ConexionBD.obtenerConexion();

            PreparedStatement sentencia =
                    conexion.prepareStatement(sql)
    ) {

        sentencia.setInt(
                1,
                idPaciente
        );

        try (
                ResultSet resultado =
                        sentencia.executeQuery()
        ) {

            while (resultado.next()) {

                citas.add(
                        construirCita(resultado)
                );
            }
        }
    }

    return citas;
}

/**
 * Obtiene las citas asignadas a un médico con la información
 * necesaria para mostrar su agenda.
 *
 * Incluye los datos descriptivos del paciente, especialidad
 * y establecimiento asociados a cada cita.
 *
 * @param idMedico identificador del médico.
 * @return lista de citas asignadas al médico.
 * @throws SQLException si ocurre un error de acceso a datos.
 */
public List<Cita> listarPorMedico(
        int idMedico
) throws SQLException {

    String sql = """
            SELECT
                c.id_cita,
                c.id_paciente,
                c.id_medico,
                c.id_especialidad,
                c.id_establecimiento,
                c.fecha_cita,
                c.hora_inicio,
                c.hora_fin,
                c.estado,
                c.motivo_consulta,
                c.observacion,
                c.motivo_cancelacion,
                c.fecha_creacion,
                c.fecha_actualizacion,

                CONCAT(
                    um.nombres,
                    ' ',
                    um.apellidos
                ) AS nombre_medico,

                CONCAT(
                    up.nombres,
                    ' ',
                    up.apellidos
                ) AS nombre_paciente,

                e.nombre AS nombre_especialidad,
                est.nombre AS nombre_establecimiento

            FROM citas c

            INNER JOIN medicos m
                ON c.id_medico = m.id_medico

            INNER JOIN usuarios um
                ON m.id_usuario = um.id_usuario

            INNER JOIN pacientes p
                ON c.id_paciente = p.id_paciente

            INNER JOIN usuarios up
                ON p.id_usuario = up.id_usuario

            INNER JOIN especialidades e
                ON c.id_especialidad = e.id_especialidad

            INNER JOIN establecimientos est
                ON c.id_establecimiento = est.id_establecimiento

            WHERE c.id_medico = ?

            ORDER BY
                c.fecha_cita ASC,
                c.hora_inicio ASC
            """;

    List<Cita> citas =
            new ArrayList<>();

    try (
            Connection conexion =
                    ConexionBD.obtenerConexion();

            PreparedStatement sentencia =
                    conexion.prepareStatement(sql)
    ) {

        sentencia.setInt(
                1,
                idMedico
        );

        try (
                ResultSet resultado =
                        sentencia.executeQuery()
        ) {

            while (resultado.next()) {

                Cita cita =
                        construirCita(
                                resultado
                        );

                cita.setNombrePaciente(
                        resultado.getString(
                                "nombre_paciente"
                        )
                );

                citas.add(
                        cita
                );
            }
        }
    }

    return citas;
}

/**
 * Construye un objeto Cita a partir del resultado
 * obtenido desde MySQL.
 */
private Cita construirCita(
        ResultSet resultado
) throws SQLException {

    Cita cita =
            new Cita();

    cita.setIdCita(
            resultado.getInt("id_cita")
    );

    cita.setIdPaciente(
            resultado.getInt("id_paciente")
    );

    cita.setIdMedico(
            resultado.getInt("id_medico")
    );

    cita.setIdEspecialidad(
            resultado.getInt("id_especialidad")
    );

    cita.setIdEstablecimiento(
            resultado.getInt("id_establecimiento")
    );

    cita.setFechaCita(
            resultado
                    .getDate("fecha_cita")
                    .toLocalDate()
    );

    cita.setHoraInicio(
            resultado
                    .getTime("hora_inicio")
                    .toLocalTime()
    );

    cita.setHoraFin(
            resultado
                    .getTime("hora_fin")
                    .toLocalTime()
    );

    cita.setEstado(
            resultado.getString("estado")
    );

    cita.setMotivoConsulta(
            resultado.getString(
                    "motivo_consulta"
            )
    );

    cita.setObservacion(
            resultado.getString(
                    "observacion"
            )
    );

    cita.setMotivoCancelacion(
            resultado.getString(
                    "motivo_cancelacion"
            )
    );

    if (
            resultado.getTimestamp(
                    "fecha_creacion"
            ) != null
    ) {

        cita.setFechaCreacion(
                resultado
                        .getTimestamp(
                                "fecha_creacion"
                        )
                        .toLocalDateTime()
        );
    }

    if (
            resultado.getTimestamp(
                    "fecha_actualizacion"
            ) != null
    ) {

        cita.setFechaActualizacion(
                resultado
                        .getTimestamp(
                                "fecha_actualizacion"
                        )
                        .toLocalDateTime()
        );
    }

    cita.setNombreMedico(
            resultado.getString(
                    "nombre_medico"
            )
    );

    cita.setNombreEspecialidad(
            resultado.getString(
                    "nombre_especialidad"
            )
    );

    cita.setNombreEstablecimiento(
            resultado.getString(
                    "nombre_establecimiento"
            )
    );

    return cita;
}
/**
 * Cancela una cita perteneciente a un paciente.
 *
 * Solamente permite cancelar citas que actualmente
 * se encuentren PROGRAMADAS o CONFIRMADAS.
 *
 * @return true si la cita fue cancelada correctamente.
 */
public boolean cancelarCita(
        int idCita,
        int idPaciente,
        String motivoCancelacion
) throws SQLException {

    String sql = """
            UPDATE citas
            SET
                estado = 'CANCELADA',
                motivo_cancelacion = ?
            WHERE id_cita = ?
              AND id_paciente = ?
              AND estado IN ('PROGRAMADA', 'CONFIRMADA')
            """;

    try (
            Connection conexion =
                    ConexionBD.obtenerConexion();

            PreparedStatement sentencia =
                    conexion.prepareStatement(sql)
    ) {

        sentencia.setString(
                1,
                motivoCancelacion
        );

        sentencia.setInt(
                2,
                idCita
        );

        sentencia.setInt(
                3,
                idPaciente
        );

        return sentencia.executeUpdate() > 0;
    }
}
/**
 * Reprograma la fecha y el horario de una cita
 * perteneciente a un paciente.
 *
 * Solamente permite modificar citas que se encuentren
 * en estado PROGRAMADA o CONFIRMADA.
 *
 * @return true si la cita fue actualizada correctamente.
 */
public boolean reprogramarCita(
        Connection conexion,
        int idCita,
        int idPaciente,
        LocalDate nuevaFecha,
        LocalTime nuevaHoraInicio,
        LocalTime nuevaHoraFin
) throws SQLException {

    String sql = """
            UPDATE citas
            SET
                fecha_cita = ?,
                hora_inicio = ?,
                hora_fin = ?
            WHERE id_cita = ?
              AND id_paciente = ?
              AND estado IN ('PROGRAMADA', 'CONFIRMADA')
            """;

    try (PreparedStatement sentencia =
                 conexion.prepareStatement(sql)) {

        sentencia.setDate(
                1,
                java.sql.Date.valueOf(nuevaFecha)
        );

        sentencia.setTime(
                2,
                java.sql.Time.valueOf(nuevaHoraInicio)
        );

        sentencia.setTime(
                3,
                java.sql.Time.valueOf(nuevaHoraFin)
        );

        sentencia.setInt(
                4,
                idCita
        );

        sentencia.setInt(
                5,
                idPaciente
        );

        return sentencia.executeUpdate() > 0;
    }
}
/**
 * Comprueba si existe un solapamiento excluyendo
 * una cita específica.
 *
 * Se utiliza durante la reprogramación para evitar
 * que la cita se detecte a sí misma como ocupada.
 */
public boolean existeSolapamientoExcluyendoCita(
        Connection conexion,
        int idCitaExcluir,
        int idMedico,
        int idEstablecimiento,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin
) throws SQLException {

    String sql = """
            SELECT COUNT(*) AS total
            FROM citas
            WHERE id_medico = ?
              AND id_establecimiento = ?
              AND fecha_cita = ?
              AND estado IN ('PROGRAMADA', 'CONFIRMADA')
              AND id_cita <> ?
              AND hora_inicio < ?
              AND hora_fin > ?
            """;

    try (PreparedStatement sentencia =
                 conexion.prepareStatement(sql)) {

        sentencia.setInt(
                1,
                idMedico
        );

        sentencia.setInt(
                2,
                idEstablecimiento
        );

        sentencia.setDate(
                3,
                java.sql.Date.valueOf(fecha)
        );

        sentencia.setInt(
                4,
                idCitaExcluir
        );

        sentencia.setTime(
                5,
                java.sql.Time.valueOf(horaFin)
        );

        sentencia.setTime(
                6,
                java.sql.Time.valueOf(horaInicio)
        );

        try (ResultSet resultado =
                     sentencia.executeQuery()) {

            return resultado.next()
                    && resultado.getInt("total") > 0;
        }
    }
}
}