package com.citasmedicas.dao;

import com.citasmedicas.model.Rol;
import com.citasmedicas.model.Usuario;
import com.citasmedicas.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;

/**
 * ================================================================
 *                       DAO - USUARIO
 * ================================================================
 *
 * Gestiona las operaciones de acceso a datos relacionadas
 * con la tabla usuarios.
 *
 * Permite:
 *
 * - Buscar usuarios por correo.
 * - Buscar usuarios por cédula.
 * - Registrar nuevos usuarios.
 * - Construir objetos Usuario desde MySQL.
 * - Actualizar datos personales.
 *
 * Incluye los campos utilizados para la seguridad y
 * recuperación de contraseña.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.1
 */
public class UsuarioDAO {

    /**
     * Busca un usuario mediante su correo electrónico.
     *
     * @param correo correo electrónico.
     * @return usuario encontrado o null.
     * @throws SQLException si ocurre un error.
     */
    public Usuario buscarPorCorreo(
            String correo
    ) throws SQLException {

        String sql = """
                SELECT
                    u.id_usuario,
                    u.nombres,
                    u.apellidos,
                    u.cedula,
                    u.correo,
                    u.password_hash,
                    u.telefono,
                    u.pregunta_seguridad,
                    u.respuesta_seguridad_hash,
                    u.intentos_recuperacion,
                    u.bloqueado_recuperacion_hasta,
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

                    return construirUsuario(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    /**
     * Busca un usuario mediante su número de cédula.
     *
     * @param cedula número de cédula.
     * @return usuario encontrado o null.
     * @throws SQLException si ocurre un error.
     */
    public Usuario buscarPorCedula(
            String cedula
    ) throws SQLException {

        String sql = """
                SELECT
                    u.id_usuario,
                    u.nombres,
                    u.apellidos,
                    u.cedula,
                    u.correo,
                    u.password_hash,
                    u.telefono,
                    u.pregunta_seguridad,
                    u.respuesta_seguridad_hash,
                    u.intentos_recuperacion,
                    u.bloqueado_recuperacion_hasta,
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

                    return construirUsuario(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    /**
     * Inserta un nuevo usuario utilizando una conexión
     * existente.
     *
     * La conexión se recibe como parámetro para permitir
     * registrar Usuario y Paciente dentro de una misma
     * transacción.
     *
     * @param conexion conexión activa.
     * @param usuario usuario a registrar.
     * @return identificador generado.
     * @throws SQLException si ocurre un error.
     */
    public int insertar(
            Connection conexion,
            Usuario usuario
    ) throws SQLException {

        String sql = """
                INSERT INTO usuarios (
                    id_rol,
                    nombres,
                    apellidos,
                    cedula,
                    correo,
                    password_hash,
                    telefono,
                    pregunta_seguridad,
                    respuesta_seguridad_hash,
                    estado
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
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
                    usuario.getRol().getIdRol()
            );

            sentencia.setString(
                    2,
                    usuario.getNombres()
            );

            sentencia.setString(
                    3,
                    usuario.getApellidos()
            );

            sentencia.setString(
                    4,
                    usuario.getCedula()
            );

            sentencia.setString(
                    5,
                    usuario.getCorreo()
            );

            sentencia.setString(
                    6,
                    usuario.getPasswordHash()
            );

            if (
                    usuario.getTelefono() == null
                            || usuario.getTelefono().isBlank()
            ) {

                sentencia.setNull(
                        7,
                        Types.VARCHAR
                );

            } else {

                sentencia.setString(
                        7,
                        usuario.getTelefono()
                );
            }

            /*
             * ====================================================
             * SEGURIDAD DE LA CUENTA
             * ====================================================
             */

            if (
                    usuario.getPreguntaSeguridad() == null
                            || usuario.getPreguntaSeguridad().isBlank()
            ) {

                sentencia.setNull(
                        8,
                        Types.VARCHAR
                );

            } else {

                sentencia.setString(
                        8,
                        usuario.getPreguntaSeguridad()
                );
            }

            if (
                    usuario.getRespuestaSeguridadHash() == null
                            || usuario.getRespuestaSeguridadHash().isBlank()
            ) {

                sentencia.setNull(
                        9,
                        Types.VARCHAR
                );

            } else {

                sentencia.setString(
                        9,
                        usuario.getRespuestaSeguridadHash()
                );
            }

            sentencia.setBoolean(
                    10,
                    usuario.isEstado()
            );

            int filasAfectadas =
                    sentencia.executeUpdate();

            if (filasAfectadas == 0) {

                throw new SQLException(
                        "No fue posible registrar el usuario."
                );
            }

            try (
                    ResultSet claves =
                            sentencia.getGeneratedKeys()
            ) {

                if (claves.next()) {

                    int idUsuario =
                            claves.getInt(1);

                    usuario.setIdUsuario(
                            idUsuario
                    );

                    return idUsuario;
                }
            }

            throw new SQLException(
                    "No fue posible obtener el identificador del usuario."
            );
        }
    }

    /**
     * Construye un objeto Usuario a partir
     * de un resultado obtenido desde MySQL.
     */
    private Usuario construirUsuario(
            ResultSet resultado
    ) throws SQLException {

        /*
         * ========================================================
         * ROL
         * ========================================================
         */

        Rol rol =
                new Rol();

        rol.setIdRol(
                resultado.getInt(
                        "id_rol"
                )
        );

        rol.setNombre(
                resultado.getString(
                        "rol_nombre"
                )
        );

        rol.setDescripcion(
                resultado.getString(
                        "rol_descripcion"
                )
        );

        rol.setEstado(
                resultado.getBoolean(
                        "rol_estado"
                )
        );

        Timestamp fechaRol =
                resultado.getTimestamp(
                        "rol_fecha_creacion"
                );

        if (fechaRol != null) {

            rol.setFechaCreacion(
                    fechaRol.toLocalDateTime()
            );
        }

        /*
         * ========================================================
         * USUARIO
         * ========================================================
         */

        Usuario usuario =
                new Usuario();

        usuario.setIdUsuario(
                resultado.getInt(
                        "id_usuario"
                )
        );

        usuario.setRol(
                rol
        );

        usuario.setNombres(
                resultado.getString(
                        "nombres"
                )
        );

        usuario.setApellidos(
                resultado.getString(
                        "apellidos"
                )
        );

        usuario.setCedula(
                resultado.getString(
                        "cedula"
                )
        );

        usuario.setCorreo(
                resultado.getString(
                        "correo"
                )
        );

        usuario.setPasswordHash(
                resultado.getString(
                        "password_hash"
                )
        );

        usuario.setTelefono(
                resultado.getString(
                        "telefono"
                )
        );

        /*
         * ========================================================
         * SEGURIDAD Y RECUPERACIÓN
         * ========================================================
         */

        usuario.setPreguntaSeguridad(
                resultado.getString(
                        "pregunta_seguridad"
                )
        );

        usuario.setRespuestaSeguridadHash(
                resultado.getString(
                        "respuesta_seguridad_hash"
                )
        );

        usuario.setIntentosRecuperacion(
                resultado.getInt(
                        "intentos_recuperacion"
                )
        );

        Timestamp bloqueo =
                resultado.getTimestamp(
                        "bloqueado_recuperacion_hasta"
                );

        if (bloqueo != null) {

            usuario.setBloqueadoRecuperacionHasta(
                    bloqueo.toLocalDateTime()
            );

        } else {

            usuario.setBloqueadoRecuperacionHasta(
                    null
            );
        }

        /*
         * ========================================================
         * ESTADO Y AUDITORÍA
         * ========================================================
         */

        usuario.setEstado(
                resultado.getBoolean(
                        "usuario_estado"
                )
        );

        Timestamp fechaCreacion =
                resultado.getTimestamp(
                        "usuario_fecha_creacion"
                );

        if (fechaCreacion != null) {

            usuario.setFechaCreacion(
                    fechaCreacion.toLocalDateTime()
            );
        }

        Timestamp fechaActualizacion =
                resultado.getTimestamp(
                        "fecha_actualizacion"
                );

        if (fechaActualizacion != null) {

            usuario.setFechaActualizacion(
                    fechaActualizacion.toLocalDateTime()
            );
        }

        return usuario;
    }

    /**
     * Actualiza los datos personales editables
     * de un usuario.
     *
     * No modifica:
     *
     * - cédula;
     * - contraseña;
     * - pregunta de seguridad;
     * - rol;
     * - estado.
     *
     * @return true si el usuario fue actualizado.
     */
    public boolean actualizarPerfil(
            int idUsuario,
            String nombres,
            String apellidos,
            String correo,
            String telefono
    ) throws SQLException {

        String sql = """
                UPDATE usuarios
                SET
                    nombres = ?,
                    apellidos = ?,
                    correo = ?,
                    telefono = ?
                WHERE id_usuario = ?
                """;

        try (
                Connection conexion =
                        ConexionBD.obtenerConexion();

                PreparedStatement sentencia =
                        conexion.prepareStatement(
                                sql
                        )
        ) {

            sentencia.setString(
                    1,
                    nombres
            );

            sentencia.setString(
                    2,
                    apellidos
            );

            sentencia.setString(
                    3,
                    correo
            );

            if (
                    telefono == null
                            || telefono.isBlank()
            ) {

                sentencia.setNull(
                        4,
                        Types.VARCHAR
                );

            } else {

                sentencia.setString(
                        4,
                        telefono
                );
            }

            sentencia.setInt(
                    5,
                    idUsuario
            );

            return sentencia.executeUpdate() > 0;
        }
    }
    /**
 * Configura o actualiza la pregunta de seguridad
 * de un usuario.
 *
 * Al actualizar la configuración se reinician los
 * intentos y cualquier bloqueo previo de recuperación.
 *
 * @param idUsuario identificador del usuario.
 * @param preguntaSeguridad código de la pregunta.
 * @param respuestaSeguridadHash hash BCrypt de la respuesta.
 * @return true si se realizó la actualización.
 * @throws SQLException si ocurre un error.
 */
public boolean actualizarSeguridadCuenta(
        int idUsuario,
        String preguntaSeguridad,
        String respuestaSeguridadHash
) throws SQLException {

    String sql = """
            UPDATE usuarios
            SET
                pregunta_seguridad = ?,
                respuesta_seguridad_hash = ?,
                intentos_recuperacion = 0,
                bloqueado_recuperacion_hasta = NULL
            WHERE id_usuario = ?
              AND estado = TRUE
            """;

    try (
            Connection conexion =
                    ConexionBD.obtenerConexion();

            PreparedStatement sentencia =
                    conexion.prepareStatement(sql)
    ) {

        sentencia.setString(
                1,
                preguntaSeguridad
        );

        sentencia.setString(
                2,
                respuestaSeguridadHash
        );

        sentencia.setInt(
                3,
                idUsuario
        );

        return sentencia.executeUpdate() > 0;
    }
}
}