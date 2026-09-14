package com.citasmedicas.service;

import com.citasmedicas.dao.UsuarioDAO;
import com.citasmedicas.model.Usuario;
import com.citasmedicas.util.PasswordUtil;

import java.sql.SQLException;

/**
 * ================================================================
 *                SERVICE - AUTENTICACIÓN
 * ================================================================
 *
 * Gestiona el proceso de inicio de sesión de los usuarios.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public class AutenticacionService {

    private final UsuarioDAO usuarioDAO;

    public AutenticacionService() {
        this.usuarioDAO = new UsuarioDAO();
    }

    public Usuario autenticar(
            String correo,
            String password
    ) throws SQLException {

        if (correo == null || correo.isBlank()) {

            throw new IllegalArgumentException(
                    "El correo electrónico es obligatorio."
            );
        }

        if (password == null || password.isBlank()) {

            throw new IllegalArgumentException(
                    "La contraseña es obligatoria."
            );
        }

        Usuario usuario =
                usuarioDAO.buscarPorCorreo(
                        correo.trim().toLowerCase()
                );

        if (usuario == null) {
            return null;
        }

        if (!usuario.isEstado()) {
            return null;
        }

        if (usuario.getRol() == null
                || !usuario.getRol().isEstado()) {

            return null;
        }

        if (!PasswordUtil.verificar(
                password,
                usuario.getPasswordHash()
        )) {

            return null;
        }

        return usuario;
    }
}