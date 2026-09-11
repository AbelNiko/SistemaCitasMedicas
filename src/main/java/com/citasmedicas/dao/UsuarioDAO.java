package com.citasmedicas.dao;

import com.citasmedicas.model.Rol;
import com.citasmedicas.model.Usuario;
import com.citasmedicas.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * ================================================================
 *                     DAO - USUARIO
 * ================================================================
 *
 * Clase encargada de realizar operaciones de acceso a datos
 * relacionadas con la tabla "usuarios".
 *
 * Permite consultar información de usuarios junto con el rol
 * asociado a cada registro.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public class UsuarioDAO {

    /**
     * Busca un usuario mediante su correo electrónico.
     *
     * Este método será utilizado posteriormente para el proceso
     * de autenticación del sistema.
     *
     * @param correo correo electrónico del usuario.
     * @return usuario encontrado o null si no existe.
     * @throws SQLException si ocurre un error de acceso a datos.
     */
    public Usuario buscarPorCorreo(String correo)
            throws SQLException {

        String sql = """
                SELECT
                    u.id_usuario,
                    u.nombres,
                    u.apellidos,
                    u.cedula,
                    u.correo,
                    u.password_hash,
                    u.telefono,
                    u.estado AS usuario_estado,
                    u.fecha_creacion AS usuario_fecha_creacion,
                    u.fecha_actualizacion,
                    r.id_rol,
                    r.nombre AS rol_nombre,
                    r.descripcion AS rol_descripcion,
                    r.estado AS rol_estado,
                    r.fecha_creacion AS rol_fecha_creacion
                FROM usuarios u
                INNER JOIN roles r
                    ON u.id_rol = r.id_rol
                WHERE u.correo = ?
                LIMIT 1
                """;

        try (
                Connection conexion =
                        ConexionBD.obtenerConexion();

                PreparedStatement sentencia =
                        conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    correo
            );

            try (
                    ResultSet resultado =
                            sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return construirUsuario(resultado);
                }
            }
        }

        return null;
    }

    /**
     * Busca un usuario mediante su número de cédula.
     *
     * @param cedula número de cédula.
     * @return usuario encontrado o null si no existe.
     * @throws SQLException si ocurre un error de acceso a datos.
     */
    public Usuario buscarPorCedula(String cedula)
            throws SQLException {

        String sql = """
                SELECT
                    u.id_usuario,
                    u.nombres,
                    u.apellidos,
                    u.cedula,
                    u.correo,
                    u.password_hash,
                    u.telefono,
                    u.estado AS usuario_estado,
                    u.fecha_creacion AS usuario_fecha_creacion,
                    u.fecha_actualizacion,
                    r.id_rol,
                    r.nombre AS rol_nombre,
                    r.descripcion AS rol_descripcion,
                    r.estado AS rol_estado,
                    r.fecha_creacion AS rol_fecha_creacion
                FROM usuarios u
                INNER JOIN roles r
                    ON u.id_rol = r.id_rol
                WHERE u.cedula = ?
                LIMIT 1
                """;

        try (
                Connection conexion =
                        ConexionBD.obtenerConexion();

                PreparedStatement sentencia =
                        conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    cedula
            );

            try (
                    ResultSet resultado =
                            sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return construirUsuario(resultado);
                }
            }
        }

        return null;
    }

    /**
     * Construye un objeto Usuario a partir de un resultado SQL.
     *
     * @param resultado resultado obtenido desde MySQL.
     * @return usuario construido.
     * @throws SQLException si ocurre un error al leer los datos.
     */
    private Usuario construirUsuario(
            ResultSet resultado
    ) throws SQLException {

        Rol rol = new Rol();

        rol.setIdRol(
                resultado.getInt("id_rol")
        );

        rol.setNombre(
                resultado.getString("rol_nombre")
        );

        rol.setDescripcion(
                resultado.getString("rol_descripcion")
        );

        rol.setEstado(
                resultado.getBoolean("rol_estado")
        );

        rol.setFechaCreacion(
                resultado
                        .getTimestamp("rol_fecha_creacion")
                        .toLocalDateTime()
        );

        Usuario usuario = new Usuario();

        usuario.setIdUsuario(
                resultado.getInt("id_usuario")
        );

        usuario.setRol(rol);

        usuario.setNombres(
                resultado.getString("nombres")
        );

        usuario.setApellidos(
                resultado.getString("apellidos")
        );

        usuario.setCedula(
                resultado.getString("cedula")
        );

        usuario.setCorreo(
                resultado.getString("correo")
        );

        usuario.setPasswordHash(
                resultado.getString("password_hash")
        );

        usuario.setTelefono(
                resultado.getString("telefono")
        );

        usuario.setEstado(
                resultado.getBoolean("usuario_estado")
        );

        usuario.setFechaCreacion(
                resultado
                        .getTimestamp("usuario_fecha_creacion")
                        .toLocalDateTime()
        );

        usuario.setFechaActualizacion(
                resultado
                        .getTimestamp("fecha_actualizacion")
                        .toLocalDateTime()
        );

        return usuario;
    }
}