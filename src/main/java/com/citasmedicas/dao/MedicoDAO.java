package com.citasmedicas.dao;

import com.citasmedicas.model.Medico;
import com.citasmedicas.model.Rol;
import com.citasmedicas.model.Usuario;
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
 *                         DAO - MÉDICO
 * ================================================================
 *
 * Gestiona las consultas relacionadas con los médicos
 * registrados en MediAppoint.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public class MedicoDAO {

    /**
     * Obtiene los médicos activos que pertenecen a una determinada
     * especialidad y atienden en un establecimiento específico.
     *
     * @param idEspecialidad identificador de la especialidad.
     * @param idEstablecimiento identificador del establecimiento.
     * @return lista de médicos disponibles.
     * @throws SQLException si ocurre un error de acceso a datos.
     */
    public List<Medico> listarPorEspecialidadYEstablecimiento(
            int idEspecialidad,
            int idEstablecimiento
    ) throws SQLException {

        List<Medico> medicos =
                new ArrayList<>();

        String sql = """
                SELECT DISTINCT
                    m.id_medico,
                    m.cedula_profesional,
                    m.observacion,
                    m.estado AS medico_estado,
                    m.fecha_creacion AS medico_fecha_creacion,

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

                FROM medicos m

                INNER JOIN usuarios u
                    ON m.id_usuario = u.id_usuario

                INNER JOIN roles r
                    ON u.id_rol = r.id_rol

                INNER JOIN medico_especialidad me
                    ON m.id_medico = me.id_medico

                INNER JOIN medico_establecimiento mest
                    ON m.id_medico = mest.id_medico

                INNER JOIN especialidades e
                    ON me.id_especialidad = e.id_especialidad

                INNER JOIN establecimientos est
                    ON mest.id_establecimiento =
                       est.id_establecimiento

                WHERE me.id_especialidad = ?
                  AND mest.id_establecimiento = ?
                  AND m.estado = TRUE
                  AND u.estado = TRUE
                  AND r.estado = TRUE
                  AND e.estado = TRUE
                  AND est.estado = TRUE
                  AND mest.estado = TRUE

                ORDER BY
                    u.apellidos,
                    u.nombres
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

            sentencia.setInt(
                    2,
                    idEstablecimiento
            );

            try (
                    ResultSet resultado =
                            sentencia.executeQuery()
            ) {

                while (resultado.next()) {

                    medicos.add(
                            construirMedico(resultado)
                    );
                }
            }
        }

        return medicos;
    }

    /**
 * Obtiene el perfil médico asociado a un usuario.
 *
 * Se utiliza principalmente después de la autenticación
 * para identificar el id_medico correspondiente al
 * usuario que inició sesión.
 *
 * @param idUsuario identificador del usuario.
 * @return médico encontrado o null si no existe.
 * @throws SQLException si ocurre un error de acceso a datos.
 */
public Medico buscarPorIdUsuario(
        int idUsuario
) throws SQLException {

    if (idUsuario <= 0) {
        throw new IllegalArgumentException(
                "El usuario no es válido."
        );
    }

    String sql = """
            SELECT
                m.id_medico,
                m.cedula_profesional,
                m.observacion,
                m.estado AS medico_estado,
                m.fecha_creacion AS medico_fecha_creacion,

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

            FROM medicos m

            INNER JOIN usuarios u
                ON m.id_usuario = u.id_usuario

            INNER JOIN roles r
                ON u.id_rol = r.id_rol

            WHERE m.id_usuario = ?
              AND m.estado = TRUE
              AND u.estado = TRUE
              AND r.estado = TRUE
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

            if (resultado.next()) {
                return construirMedico(
                        resultado
                );
            }
        }
    }

    return null;
}

    /**
     * Construye un médico junto con su usuario y rol.
     */
    private Medico construirMedico(
            ResultSet resultado
    ) throws SQLException {

        Rol rol =
                new Rol();

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

        Timestamp fechaRol =
                resultado.getTimestamp(
                        "rol_fecha_creacion"
                );

        if (fechaRol != null) {
            rol.setFechaCreacion(
                    fechaRol.toLocalDateTime()
            );
        }

        Usuario usuario =
                new Usuario();

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

        Timestamp fechaUsuario =
                resultado.getTimestamp(
                        "usuario_fecha_creacion"
                );

        if (fechaUsuario != null) {
            usuario.setFechaCreacion(
                    fechaUsuario.toLocalDateTime()
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

        Medico medico =
                new Medico();

        medico.setIdMedico(
                resultado.getInt("id_medico")
        );

        medico.setUsuario(usuario);

        medico.setCedulaProfesional(
                resultado.getString(
                        "cedula_profesional"
                )
        );

        medico.setObservacion(
                resultado.getString(
                        "observacion"
                )
        );

        medico.setEstado(
                resultado.getBoolean(
                        "medico_estado"
                )
        );

        Timestamp fechaMedico =
                resultado.getTimestamp(
                        "medico_fecha_creacion"
                );

        if (fechaMedico != null) {
            medico.setFechaCreacion(
                    fechaMedico.toLocalDateTime()
            );
        }

        return medico;
    }
}