package com.citasmedicas.service;

import com.citasmedicas.dao.UsuarioDAO;

import com.citasmedicas.model.PreguntaSeguridad;
import com.citasmedicas.model.Usuario;

import com.citasmedicas.util.PasswordUtil;
import com.citasmedicas.util.RespuestaSeguridadUtil;

import java.sql.SQLException;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * ================================================================
 *          SERVICE - RECUPERACIÓN DE CONTRASEÑA
 * ================================================================
 *
 * Gestiona el flujo seguro de recuperación de acceso.
 *
 * Proceso:
 *
 * 1. Validar correo y cédula.
 * 2. Verificar que exista una pregunta de seguridad.
 * 3. Controlar bloqueos temporales.
 * 4. Verificar la respuesta protegida con BCrypt.
 * 5. Limitar intentos fallidos.
 * 6. Restablecer la contraseña.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public class RecuperacionPasswordService {

    private static final int MAXIMO_INTENTOS =
            5;

    private static final int MINUTOS_BLOQUEO =
            15;

    private final UsuarioDAO usuarioDAO;

    public RecuperacionPasswordService() {

        usuarioDAO =
                new UsuarioDAO();
    }

    /**
     * Identifica la cuenta mediante correo y cédula.
     *
     * @return usuario válido para recuperación.
     */
    public Usuario identificarCuenta(
            String correo,
            String cedula
    ) throws SQLException {

        validarCorreoCedula(
                correo,
                cedula
        );

        String correoNormalizado =
                correo
                        .trim()
                        .toLowerCase();

        String cedulaNormalizada =
                cedula.trim();

        Usuario usuario =
                usuarioDAO.buscarPorCorreo(
                        correoNormalizado
                );

        /*
         * Usamos un mensaje genérico para evitar revelar
         * si un correo concreto existe en el sistema.
         */
        if (
                usuario == null
                        || !usuario.isEstado()
                        || usuario.getCedula() == null
                        || !usuario.getCedula()
                                .equals(
                                        cedulaNormalizada
                                )
        ) {

            throw new IllegalArgumentException(
                    "No fue posible verificar los datos ingresados."
            );
        }

        if (
                !usuario
                        .tienePreguntaSeguridadConfigurada()
        ) {

            throw new IllegalArgumentException(
                    "Esta cuenta todavía no tiene configurada "
                            + "una pregunta de seguridad."
            );
        }

        verificarBloqueo(
                usuario
        );

        return usuario;
    }

    /**
     * Devuelve la pregunta configurada.
     */
    public PreguntaSeguridad obtenerPregunta(
            Usuario usuario
    ) {

        if (usuario == null) {

            throw new IllegalArgumentException(
                    "No existe una cuenta para recuperar."
            );
        }

        PreguntaSeguridad pregunta =
                PreguntaSeguridad.desdeCodigo(
                        usuario.getPreguntaSeguridad()
                );

        if (pregunta == null) {

            throw new IllegalArgumentException(
                    "La pregunta de seguridad configurada no es válida."
            );
        }

        return pregunta;
    }

    /**
     * Verifica la respuesta de seguridad.
     *
     * Cada error incrementa los intentos.
     * Al llegar a 5, la recuperación se bloquea
     * temporalmente durante 15 minutos.
     */
    public void verificarRespuesta(
            Usuario usuario,
            String respuesta
    ) throws SQLException {

        if (
                usuario == null
                        || usuario.getIdUsuario() == null
        ) {

            throw new IllegalArgumentException(
                    "No existe una recuperación activa."
            );
        }

        if (
                respuesta == null
                        || respuesta.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Debes ingresar la respuesta de seguridad."
            );
        }

        /*
         * Volvemos a comprobar el bloqueo por seguridad.
         */
        verificarBloqueo(
                usuario
        );

        boolean correcta =
                RespuestaSeguridadUtil.verificar(
                        respuesta,
                        usuario.getRespuestaSeguridadHash()
                );

        if (correcta) {

            usuarioDAO.reiniciarRecuperacion(
                    usuario.getIdUsuario()
            );

            usuario.setIntentosRecuperacion(
                    0
            );

            usuario.setBloqueadoRecuperacionHasta(
                    null
            );

            return;
        }

        int nuevosIntentos =
                usuario.getIntentosRecuperacion()
                        + 1;

        /*
         * ========================================================
         * SE ALCANZÓ EL MÁXIMO DE INTENTOS
         * ========================================================
         */

        if (
                nuevosIntentos
                        >= MAXIMO_INTENTOS
        ) {

            LocalDateTime bloqueadoHasta =
                    LocalDateTime
                            .now()
                            .plusMinutes(
                                    MINUTOS_BLOQUEO
                            );

            usuarioDAO
                    .actualizarIntentosRecuperacion(
                            usuario.getIdUsuario(),
                            nuevosIntentos,
                            bloqueadoHasta
                    );

            usuario.setIntentosRecuperacion(
                    nuevosIntentos
            );

            usuario.setBloqueadoRecuperacionHasta(
                    bloqueadoHasta
            );

            throw new IllegalArgumentException(
                    "Se alcanzó el máximo de intentos. "
                            + "La recuperación fue bloqueada "
                            + "durante 15 minutos."
            );
        }

        /*
         * ========================================================
         * INTENTO INCORRECTO
         * ========================================================
         */

        usuarioDAO
                .actualizarIntentosRecuperacion(
                        usuario.getIdUsuario(),
                        nuevosIntentos,
                        null
                );

        usuario.setIntentosRecuperacion(
                nuevosIntentos
        );

        int restantes =
                MAXIMO_INTENTOS
                        - nuevosIntentos;

        throw new IllegalArgumentException(
                "La respuesta de seguridad no es correcta. "
                        + "Intentos restantes: "
                        + restantes
                        + "."
        );
    }

    /**
     * Cambia finalmente la contraseña.
     */
    public void restablecerPassword(
            Usuario usuario,
            String nuevaPassword,
            String confirmarPassword
    ) throws SQLException {

        if (
                usuario == null
                        || usuario.getIdUsuario() == null
        ) {

            throw new IllegalArgumentException(
                    "No existe una recuperación activa."
            );
        }

        validarNuevaPassword(
                nuevaPassword,
                confirmarPassword
        );

        /*
         * Evitamos establecer exactamente
         * la misma contraseña anterior.
         */
        if (
                usuario.getPasswordHash() != null
                        && PasswordUtil.verificar(
                                nuevaPassword,
                                usuario.getPasswordHash()
                        )
        ) {

            throw new IllegalArgumentException(
                    "La nueva contraseña debe ser diferente "
                            + "a la contraseña anterior."
            );
        }

        String nuevoHash =
                PasswordUtil.generarHash(
                        nuevaPassword
                );

        boolean actualizado =
                usuarioDAO.restablecerPassword(
                        usuario.getIdUsuario(),
                        nuevoHash
                );

        if (!actualizado) {

            throw new SQLException(
                    "No fue posible restablecer la contraseña."
            );
        }

        usuario.setPasswordHash(
                nuevoHash
        );

        usuario.setIntentosRecuperacion(
                0
        );

        usuario.setBloqueadoRecuperacionHasta(
                null
        );
    }

    /**
     * Controla un bloqueo existente.
     *
     * Si el bloqueo ya venció, los intentos se
     * reinician automáticamente.
     */
    private void verificarBloqueo(
            Usuario usuario
    ) throws SQLException {

        LocalDateTime bloqueadoHasta =
                usuario
                        .getBloqueadoRecuperacionHasta();

        if (bloqueadoHasta == null) {
            return;
        }

        LocalDateTime ahora =
                LocalDateTime.now();

        if (
                bloqueadoHasta.isAfter(
                        ahora
                )
        ) {

            long segundos =
                    Duration.between(
                                    ahora,
                                    bloqueadoHasta
                            )
                            .getSeconds();

            long minutos =
                    Math.max(
                            1,
                            (segundos + 59) / 60
                    );

            throw new IllegalArgumentException(
                    "La recuperación está temporalmente bloqueada. "
                            + "Inténtalo nuevamente en aproximadamente "
                            + minutos
                            + " minuto(s)."
            );
        }

        /*
         * El bloqueo venció.
         */
        usuarioDAO.reiniciarRecuperacion(
                usuario.getIdUsuario()
        );

        usuario.setIntentosRecuperacion(
                0
        );

        usuario.setBloqueadoRecuperacionHasta(
                null
        );
    }

    /**
     * Valida los datos utilizados para identificar
     * la cuenta.
     */
    private void validarCorreoCedula(
            String correo,
            String cedula
    ) {

        if (
                correo == null
                        || correo.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "El correo electrónico es obligatorio."
            );
        }

        if (
                cedula == null
                        || cedula.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "La cédula es obligatoria."
            );
        }

        if (
                !cedula
                        .trim()
                        .matches("\\d{10}")
        ) {

            throw new IllegalArgumentException(
                    "La cédula debe contener exactamente 10 dígitos."
            );
        }
    }

    /**
     * Valida la nueva contraseña.
     */
    private void validarNuevaPassword(
            String nuevaPassword,
            String confirmarPassword
    ) {

        if (
                nuevaPassword == null
                        || nuevaPassword.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "La nueva contraseña es obligatoria."
            );
        }

        if (
                nuevaPassword.length()
                        < 8
        ) {

            throw new IllegalArgumentException(
                    "La nueva contraseña debe contener "
                            + "al menos 8 caracteres."
            );
        }

        if (
                !nuevaPassword.equals(
                        confirmarPassword
                )
        ) {

            throw new IllegalArgumentException(
                    "Las nuevas contraseñas no coinciden."
            );
        }
    }
}