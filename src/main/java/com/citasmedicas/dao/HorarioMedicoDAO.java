package com.citasmedicas.dao;

import com.citasmedicas.model.HorarioMedico;
import com.citasmedicas.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * ================================================================
 *                    DAO - HORARIO MÉDICO
 * ================================================================
 *
 * Gestiona las consultas de horarios de atención de los médicos.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public class HorarioMedicoDAO {

    /**
     * Busca los horarios activos de un médico en un establecimiento
     * para un día específico de la semana.
     *
     * @param idMedico identificador del médico.
     * @param idEstablecimiento identificador del establecimiento.
     * @param diaSemana día entre 1 (lunes) y 7 (domingo).
     * @return lista de horarios configurados.
     * @throws SQLException si ocurre un error de acceso a datos.
     */
    public List<HorarioMedico> listarPorMedicoEstablecimientoYDia(
            int idMedico,
            int idEstablecimiento,
            int diaSemana
    ) throws SQLException {

        List<HorarioMedico> horarios =
                new ArrayList<>();

        String sql = """
                SELECT
                    id_horario,
                    id_medico,
                    id_establecimiento,
                    dia_semana,
                    hora_inicio,
                    hora_fin,
                    duracion_cita_minutos,
                    estado,
                    fecha_creacion
                FROM horarios_medicos
                WHERE id_medico = ?
                  AND id_establecimiento = ?
                  AND dia_semana = ?
                  AND estado = TRUE
                ORDER BY hora_inicio
                """;

        try (
                Connection conexion =
                        ConexionBD.obtenerConexion();

                PreparedStatement sentencia =
                        conexion.prepareStatement(sql)
        ) {

            sentencia.setInt(1, idMedico);
            sentencia.setInt(2, idEstablecimiento);
            sentencia.setInt(3, diaSemana);

            try (
                    ResultSet resultado =
                            sentencia.executeQuery()
            ) {

                while (resultado.next()) {

                    horarios.add(
                            construirHorario(resultado)
                    );
                }
            }
        }

        return horarios;
    }

    private HorarioMedico construirHorario(
            ResultSet resultado
    ) throws SQLException {

        HorarioMedico horario =
                new HorarioMedico();

        horario.setIdHorario(
                resultado.getInt("id_horario")
        );

        horario.setIdMedico(
                resultado.getInt("id_medico")
        );

        horario.setIdEstablecimiento(
                resultado.getInt(
                        "id_establecimiento"
                )
        );

        horario.setDiaSemana(
                resultado.getInt("dia_semana")
        );

        horario.setHoraInicio(
                resultado.getTime(
                        "hora_inicio"
                ).toLocalTime()
        );

        horario.setHoraFin(
                resultado.getTime(
                        "hora_fin"
                ).toLocalTime()
        );

        horario.setDuracionCitaMinutos(
                resultado.getInt(
                        "duracion_cita_minutos"
                )
        );

        horario.setEstado(
                resultado.getBoolean("estado")
        );

        Timestamp fecha =
                resultado.getTimestamp(
                        "fecha_creacion"
                );

        if (fecha != null) {
            horario.setFechaCreacion(
                    fecha.toLocalDateTime()
            );
        }

        return horario;
    }
}