package com.citasmedicas.service;

import com.citasmedicas.dao.EspecialidadDAO;
import com.citasmedicas.model.Especialidad;

import java.sql.SQLException;
import java.util.List;

/**
 * ================================================================
 *                 SERVICE - ESPECIALIDAD
 * ================================================================
 *
 * Gestiona las reglas de negocio relacionadas con las
 * especialidades médicas.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public class EspecialidadService {

    private final EspecialidadDAO especialidadDAO;

    public EspecialidadService() {
        this.especialidadDAO =
                new EspecialidadDAO();
    }

    /**
     * Obtiene las especialidades disponibles para
     * el agendamiento de citas.
     *
     * @return lista de especialidades activas.
     * @throws SQLException si ocurre un error de acceso a datos.
     */
    public List<Especialidad> listarDisponibles()
            throws SQLException {

        return especialidadDAO.listarActivas();
    }
}