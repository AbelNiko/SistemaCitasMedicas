package com.citasmedicas.model;

/**
 * ================================================================
 *                PREGUNTAS DE SEGURIDAD
 * ================================================================
 *
 * Catálogo de preguntas utilizadas para verificar la identidad
 * del usuario durante la recuperación de su cuenta.
 *
 * En MySQL se almacena únicamente el código de la pregunta.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public enum PreguntaSeguridad {

    MATERIA_FAVORITA(
            "¿Cuál era tu materia favorita en la escuela?"
    ),

    NACIMIENTO_PADRES(
            "¿Dónde nacieron tus padres?"
    ),

    MARCA_AUTOS(
            "¿Cuál es tu marca favorita de autos?"
    ),

    COMIDA_FAVORITA(
            "¿Cuál es tu comida favorita?"
    );

    private final String texto;

    PreguntaSeguridad(
            String texto
    ) {

        this.texto = texto;
    }

    /**
     * Código almacenado en la base de datos.
     */
    public String getCodigo() {

        return name();
    }

    /**
     * Texto mostrado al usuario.
     */
    public String getTexto() {

        return texto;
    }

    /**
     * Permite mostrar directamente el texto
     * dentro de un ComboBox.
     */
    @Override
    public String toString() {

        return texto;
    }

    /**
     * Convierte un código almacenado en MySQL
     * nuevamente en una pregunta.
     */
    public static PreguntaSeguridad desdeCodigo(
            String codigo
    ) {

        if (
                codigo == null
                        || codigo.isBlank()
        ) {

            return null;
        }

        try {

            return valueOf(
                    codigo.trim()
            );

        } catch (IllegalArgumentException e) {

            return null;
        }
    }
}