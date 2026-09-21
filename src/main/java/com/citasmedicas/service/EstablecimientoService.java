package com.citasmedicas.service;

import com.citasmedicas.dao.EstablecimientoDAO;
import com.citasmedicas.model.Establecimiento;

import java.sql.SQLException;
import java.util.List;

/**
 * ================================================================
 *               SERVICE - ESTABLECIMIENTO
 * ================================================================
 *
 * Gestiona las reglas de negocio relacionadas con los
 * establecimientos de salud.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public class EstablecimientoService {

    private final EstablecimientoDAO establecimientoDAO;

    public EstablecimientoService() {

        this.establecimientoDAO =
                new EstablecimientoDAO();
    }

    /**
     * Obtiene los establecimientos disponibles
     * para el agendamiento de citas.
     *
     * @return lista de establecimientos activos.
     * @throws SQLException si ocurre un error de acceso a datos.
     */
    public List<Establecimiento> listarDisponibles()
            throws SQLException {

        return establecimientoDAO.listarActivos();
    }
}