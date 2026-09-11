package com.citasmedicas.dao;

import com.citasmedicas.model.Rol;
import com.citasmedicas.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

/**
 * ================================================================
 *                       DAO - ROL
 * ================================================================
 *
 * Clase encargada de realizar operaciones de acceso a datos
 * relacionadas con la tabla "roles".
 *
 * Su responsabilidad es consultar la base de datos y transformar
 * los resultados obtenidos en objetos de tipo Rol.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public class RolDAO {

    /**
     * Obtiene todos los roles registrados en la base de datos.
     *
     * @return lista de roles.
     * @throws SQLException si ocurre un error de acceso a datos.
     */
    public List<Rol> listarTodos() throws SQLException {

        List<Rol> roles = new ArrayList<>();

        String sql = """
                SELECT
                    id_rol,
                    nombre,
                    descripcion,
                    estado,
                    fecha_creacion
                FROM roles
                ORDER BY id_rol
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

                Rol rol = new Rol();

                rol.setIdRol(
                        resultado.getInt("id_rol")
                );

                rol.setNombre(
                        resultado.getString("nombre")
                );

                rol.setDescripcion(
                        resultado.getString("descripcion")
                );

                rol.setEstado(
                        resultado.getBoolean("estado")
                );

                rol.setFechaCreacion(
                        resultado
                                .getTimestamp("fecha_creacion")
                                .toLocalDateTime()
                );

                roles.add(rol);
            }
        }

        return roles;
    }

    /**
     * Busca un rol mediante su identificador.
     *
     * @param idRol identificador del rol.
     * @return rol encontrado o null si no existe.
     * @throws SQLException si ocurre un error de acceso a datos.
     */
    public Rol buscarPorId(Integer idRol)
            throws SQLException {

        String sql = """
                SELECT
                    id_rol,
                    nombre,
                    descripcion,
                    estado,
                    fecha_creacion
                FROM roles
                WHERE id_rol = ?
                """;

        try (
                Connection conexion =
                        ConexionBD.obtenerConexion();

                PreparedStatement sentencia =
                        conexion.prepareStatement(sql)
        ) {

            sentencia.setInt(1, idRol);

            try (
                    ResultSet resultado =
                            sentencia.executeQuery()
            ) {

                if (resultado.next()) {

                    Rol rol = new Rol();

                    rol.setIdRol(
                            resultado.getInt("id_rol")
                    );

                    rol.setNombre(
                            resultado.getString("nombre")
                    );

                    rol.setDescripcion(
                            resultado.getString("descripcion")
                    );

                    rol.setEstado(
                            resultado.getBoolean("estado")
                    );

                    rol.setFechaCreacion(
                            resultado
                                    .getTimestamp("fecha_creacion")
                                    .toLocalDateTime()
                    );

                    return rol;
                }
            }
        }

        return null;
    }
}