package com.citasmedicas.dao;

import com.citasmedicas.model.Especialidad;
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
 *                   DAO - ESPECIALIDAD
 * ================================================================
 *
 * Gestiona las consultas relacionadas con las especialidades
 * médicas registradas en el sistema.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public class EspecialidadDAO {

    /**
     * Obtiene todas las especialidades activas.
     *
     * @return lista de especialidades activas.
     * @throws SQLException si ocurre un error de acceso a datos.
     */
    public List<Especialidad> listarActivas()
            throws SQLException {

        List<Especialidad> especialidades =
                new ArrayList<>();

        String sql = """
                SELECT
                    id_especialidad,
                    nombre,
                    descripcion,
                    estado,
                    fecha_creacion
                FROM especialidades
                WHERE estado = TRUE
                ORDER BY nombre
                """;

        try (
                Connection conexion =
                        ConexionBD.obtenerConexion();

                PreparedStatement sentencia =
                        conexion.prepareStatement(sql);

                ResultSet resultado =
                        sentencia.executeQuery()
        ) {

            while (resultado.next()) {

                especialidades.add(
                        construirEspecialidad(resultado)
                );
            }
        }

        return especialidades;
    }

    /**
     * Busca una especialidad mediante su identificador.
     *
     * @param idEspecialidad identificador de la especialidad.
     * @return especialidad encontrada o null.
     * @throws SQLException si ocurre un error.
     */
    public Especialidad buscarPorId(
            int idEspecialidad
    ) throws SQLException {

        String sql = """
                SELECT
                    id_especialidad,
                    nombre,
                    descripcion,
                    estado,
                    fecha_creacion
                FROM especialidades
                WHERE id_especialidad = ?
                LIMIT 1
                """;

        try (
                Connection conexion =
                        ConexionBD.obtenerConexion();

                PreparedStatement sentencia =
                        conexion.prepareStatement(sql)
        ) {

            sentencia.setInt(
                    1,
                    idEspecialidad
            );

            try (
                    ResultSet resultado =
                            sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return construirEspecialidad(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    /**
     * Convierte un resultado SQL en un objeto Especialidad.
     */
    private Especialidad construirEspecialidad(
            ResultSet resultado
    ) throws SQLException {

        Especialidad especialidad =
                new Especialidad();

        especialidad.setIdEspecialidad(
                resultado.getInt(
                        "id_especialidad"
                )
        );

        especialidad.setNombre(
                resultado.getString(
                        "nombre"
                )
        );

        especialidad.setDescripcion(
                resultado.getString(
                        "descripcion"
                )
        );

        especialidad.setEstado(
                resultado.getBoolean(
                        "estado"
                )
        );

        Timestamp fechaCreacion =
                resultado.getTimestamp(
                        "fecha_creacion"
                );

        if (fechaCreacion != null) {
            especialidad.setFechaCreacion(
                    fechaCreacion.toLocalDateTime()
            );
        }

        return especialidad;
    }
}