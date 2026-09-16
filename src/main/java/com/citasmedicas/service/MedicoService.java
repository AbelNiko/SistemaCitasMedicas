package com.citasmedicas.service;

import com.citasmedicas.dao.MedicoDAO;
import com.citasmedicas.model.Medico;

import java.sql.SQLException;
import java.util.List;

/**
 * ================================================================
 *                    SERVICE - MÉDICO
 * ================================================================
 *
 * Gestiona las reglas de negocio relacionadas con la consulta
 * de médicos disponibles para el agendamiento.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public class MedicoService {

    private final MedicoDAO medicoDAO;

    public MedicoService() {
        this.medicoDAO =
                new MedicoDAO();
    }

    /**
     * Obtiene médicos que atienden una especialidad determinada
     * dentro de un establecimiento específico.
     *
     * @param idEspecialidad identificador de especialidad.
     * @param idEstablecimiento identificador de establecimiento.
     * @return médicos que cumplen ambos criterios.
     * @throws SQLException si ocurre un error de acceso a datos.
     */
    public List<Medico> listarDisponibles(
            int idEspecialidad,
            int idEstablecimiento
    ) throws SQLException {

        if (idEspecialidad <= 0) {
            throw new IllegalArgumentException(
                    "La especialidad seleccionada no es válida."
            );
        }

        if (idEstablecimiento <= 0) {
            throw new IllegalArgumentException(
                    "El establecimiento seleccionado no es válido."
            );
        }

        return medicoDAO
                .listarPorEspecialidadYEstablecimiento(
                        idEspecialidad,
                        idEstablecimiento
                );
    }
}