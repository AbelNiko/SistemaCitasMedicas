package com.citasmedicas.service;

import com.citasmedicas.dao.RolDAO;
import com.citasmedicas.model.Rol;

import java.sql.SQLException;
import java.util.List;

/**
 * ================================================================
 *                     SERVICE - ROL
 * ================================================================
 *
 * Clase encargada de gestionar las reglas de negocio relacionadas
 * con los roles del sistema.
 *
 * Esta capa se encuentra entre los controladores y la capa DAO.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public class RolService {

    private final RolDAO rolDAO;

    /**
     * Constructor principal.
     */
    public RolService() {
        this.rolDAO = new RolDAO();
    }

    /**
     * Obtiene todos los roles registrados.
     *
     * @return lista de roles.
     * @throws SQLException si ocurre un error de acceso a datos.
     */
    public List<Rol> listarTodos()
            throws SQLException {

        return rolDAO.listarTodos();
    }

    /**
     * Busca un rol mediante su identificador.
     *
     * @param idRol identificador del rol.
     * @return rol encontrado o null.
     * @throws SQLException si ocurre un error de acceso a datos.
     */
    public Rol buscarPorId(Integer idRol)
            throws SQLException {

        if (idRol == null || idRol <= 0) {

            throw new IllegalArgumentException(
                    "El identificador del rol no es válido."
            );
        }

        return rolDAO.buscarPorId(idRol);
    }
}