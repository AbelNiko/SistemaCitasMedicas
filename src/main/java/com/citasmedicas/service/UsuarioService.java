package com.citasmedicas.service;

import com.citasmedicas.dao.UsuarioDAO;
import com.citasmedicas.model.Usuario;

import java.sql.SQLException;
import java.util.regex.Pattern;

/**
 * ================================================================
 *                   SERVICE - USUARIO
 * ================================================================
 *
 * Clase encargada de aplicar las reglas de negocio relacionadas
 * con los usuarios del sistema.
 *
 * Esta capa se encuentra entre los controladores y la capa DAO.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public class UsuarioService {

    private static final Pattern PATRON_CORREO =
            Pattern.compile(
                    "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
            );

    private final UsuarioDAO usuarioDAO;

    /**
     * Constructor principal.
     */
    public UsuarioService() {
        this.usuarioDAO = new UsuarioDAO();
    }

    /**
     * Busca un usuario mediante su correo electrónico.
     *
     * @param correo correo electrónico.
     * @return usuario encontrado o null.
     * @throws SQLException si ocurre un error de acceso a datos.
     */
    public Usuario buscarPorCorreo(String correo)
            throws SQLException {

        validarCorreo(correo);

        return usuarioDAO.buscarPorCorreo(
                correo.trim().toLowerCase()
        );
    }

    /**
     * Busca un usuario mediante su número de cédula.
     *
     * @param cedula cédula del usuario.
     * @return usuario encontrado o null.
     * @throws SQLException si ocurre un error de acceso a datos.
     */
    public Usuario buscarPorCedula(String cedula)
            throws SQLException {

        validarCedula(cedula);

        return usuarioDAO.buscarPorCedula(
                cedula.trim()
        );
    }

    /**
     * Valida el formato básico del correo electrónico.
     *
     * @param correo correo a validar.
     */
    private void validarCorreo(String correo) {

        if (correo == null || correo.isBlank()) {

            throw new IllegalArgumentException(
                    "El correo electrónico es obligatorio."
            );
        }

        String correoNormalizado = correo.trim();

        if (!PATRON_CORREO
                .matcher(correoNormalizado)
                .matches()) {

            throw new IllegalArgumentException(
                    "El formato del correo electrónico no es válido."
            );
        }
    }

    /**
     * Valida que la cédula tenga diez dígitos.
     *
     * @param cedula cédula a validar.
     */
    private void validarCedula(String cedula) {

        if (cedula == null || cedula.isBlank()) {

            throw new IllegalArgumentException(
                    "La cédula es obligatoria."
            );
        }

        String cedulaNormalizada = cedula.trim();

        if (!cedulaNormalizada.matches("\\d{10}")) {

            throw new IllegalArgumentException(
                    "La cédula debe contener exactamente 10 dígitos."
            );
        }
    }
}