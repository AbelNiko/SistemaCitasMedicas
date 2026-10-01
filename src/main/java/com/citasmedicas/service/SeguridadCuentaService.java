package com.citasmedicas.service;

import com.citasmedicas.dao.UsuarioDAO;
import com.citasmedicas.model.PreguntaSeguridad;
import com.citasmedicas.model.Usuario;
import com.citasmedicas.util.PasswordUtil;
import com.citasmedicas.util.RespuestaSeguridadUtil;

import java.sql.SQLException;

/**
 * ================================================================
 *              SERVICE - SEGURIDAD DE CUENTA
 * ================================================================
 *
 * Gestiona la configuración de la pregunta de seguridad
 * utilizada posteriormente durante la recuperación de acceso.
 *
 * Para modificar la seguridad se requiere:
 *
 * - sesión válida;
 * - contraseña actual correcta;
 * - pregunta seleccionada;
 * - respuesta válida.
 *
 * La respuesta nunca se almacena en texto plano.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.1
 */
public class SeguridadCuentaService {

    private final UsuarioDAO usuarioDAO;

    public SeguridadCuentaService() {

        this.usuarioDAO =
                new UsuarioDAO();
    }

    /**
     * Configura o reemplaza la pregunta de seguridad.
     *
     * @param usuario usuario autenticado.
     * @param passwordActual contraseña actual.
     * @param pregunta pregunta elegida.
     * @param respuesta respuesta de seguridad.
     * @throws SQLException si ocurre un error de datos.
     */
    public void configurarPreguntaSeguridad(
            Usuario usuario,
            String passwordActual,
            PreguntaSeguridad pregunta,
            String respuesta
    ) throws SQLException {

        validarUsuario(
                usuario
        );

        /*
         * Confirmamos primero la contraseña actual.
         */
        validarPasswordActual(
                usuario,
                passwordActual
        );

        if (pregunta == null) {

            throw new IllegalArgumentException(
                    "Debes seleccionar una pregunta de seguridad."
            );
        }

        RespuestaSeguridadUtil.validarParaRegistro(
                respuesta
        );

        String hashRespuesta =
                RespuestaSeguridadUtil.generarHash(
                        respuesta
                );

        boolean actualizado =
                usuarioDAO.actualizarSeguridadCuenta(
                        usuario.getIdUsuario(),
                        pregunta.getCodigo(),
                        hashRespuesta
                );

        if (!actualizado) {

            throw new SQLException(
                    "No fue posible actualizar la seguridad de la cuenta."
            );
        }

        /*
         * Sincronizamos el usuario mantenido en memoria.
         */
        usuario.setPreguntaSeguridad(
                pregunta.getCodigo()
        );

        usuario.setRespuestaSeguridadHash(
                hashRespuesta
        );

        usuario.setIntentosRecuperacion(
                0
        );

        usuario.setBloqueadoRecuperacionHasta(
                null
        );
    }

    /**
     * Valida que exista una sesión válida.
     */
    private void validarUsuario(
            Usuario usuario
    ) {

        if (
                usuario == null
                        || usuario.getIdUsuario() == null
                        || usuario.getIdUsuario() <= 0
        ) {

            throw new IllegalArgumentException(
                    "No existe una sesión de usuario válida."
            );
        }
    }

    /**
     * Verifica que la contraseña ingresada corresponda
     * al usuario autenticado.
     */
    private void validarPasswordActual(
            Usuario usuario,
            String passwordActual
    ) {

        if (
                passwordActual == null
                        || passwordActual.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Debes ingresar tu contraseña actual."
            );
        }

        if (
                usuario.getPasswordHash() == null
                        || !PasswordUtil.verificar(
                                passwordActual,
                                usuario.getPasswordHash()
                        )
        ) {

            throw new IllegalArgumentException(
                    "La contraseña actual no es correcta."
            );
        }
    }
}