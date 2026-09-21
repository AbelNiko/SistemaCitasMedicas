package com.citasmedicas.dao;

import com.citasmedicas.model.Establecimiento;
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
 *                 DAO - ESTABLECIMIENTO
 * ================================================================
 *
 * Gestiona las consultas relacionadas con los establecimientos
 * de salud registrados en MediAppoint.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public class EstablecimientoDAO {

    /**
     * Obtiene todos los establecimientos activos.
     *
     * @return lista de establecimientos activos.
     * @throws SQLException si ocurre un error de acceso a datos.
     */
    public List<Establecimiento> listarActivos()
            throws SQLException {

        List<Establecimiento> establecimientos =
                new ArrayList<>();

        String sql = """
                SELECT
                    id_establecimiento,
                    nombre,
                    direccion,
                    telefono,
                    ciudad,
                    estado,
                    fecha_creacion
                FROM establecimientos
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

                establecimientos.add(
                        construirEstablecimiento(
                                resultado
                        )
                );
            }
        }

        return establecimientos;
    }

    /**
     * Busca un establecimiento mediante su identificador.
     *
     * @param idEstablecimiento identificador del establecimiento.
     * @return establecimiento encontrado o null.
     * @throws SQLException si ocurre un error de acceso a datos.
     */
    public Establecimiento buscarPorId(
            int idEstablecimiento
    ) throws SQLException {

        String sql = """
                SELECT
                    id_establecimiento,
                    nombre,
                    direccion,
                    telefono,
                    ciudad,
                    estado,
                    fecha_creacion
                FROM establecimientos
                WHERE id_establecimiento = ?
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
                    idEstablecimiento
            );

            try (
                    ResultSet resultado =
                            sentencia.executeQuery()
            ) {

                if (resultado.next()) {

                    return construirEstablecimiento(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    /**
     * Convierte un resultado SQL en un objeto Establecimiento.
     */
    private Establecimiento construirEstablecimiento(
            ResultSet resultado
    ) throws SQLException {

        Establecimiento establecimiento =
                new Establecimiento();

        establecimiento.setIdEstablecimiento(
                resultado.getInt(
                        "id_establecimiento"
                )
        );

        establecimiento.setNombre(
                resultado.getString(
                        "nombre"
                )
        );

        establecimiento.setDireccion(
                resultado.getString(
                        "direccion"
                )
        );

        establecimiento.setTelefono(
                resultado.getString(
                        "telefono"
                )
        );

        establecimiento.setCiudad(
                resultado.getString(
                        "ciudad"
                )
        );

        establecimiento.setEstado(
                resultado.getBoolean(
                        "estado"
                )
        );

        Timestamp fechaCreacion =
                resultado.getTimestamp(
                        "fecha_creacion"
                );

        if (fechaCreacion != null) {

            establecimiento.setFechaCreacion(
                    fechaCreacion.toLocalDateTime()
            );
        }

        return establecimiento;
    }
}