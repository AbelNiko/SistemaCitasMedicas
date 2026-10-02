package com.citasmedicas.util;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

/**
 * ================================================================
 *               UTIL - CONTROLES DE TEXTO
 * ================================================================
 *
 * Configura filtros de entrada reutilizables para formularios.
 *
 * @author Equipo de Ingeniería de Software II
 * @version 1.0
 */
public final class CampoTextoUtil {

    private CampoTextoUtil() {
    }

    /**
     * Permite únicamente letras Unicode y espacios.
     */
    public static void soloLetrasYEspacios(
            TextField campo,
            int longitudMaxima
    ) {

        campo.setTextFormatter(
                new TextFormatter<String>(
                        cambio -> {

                            String nuevoTexto =
                                    cambio.getControlNewText();

                            if (
                                    nuevoTexto.length()
                                            > longitudMaxima
                            ) {

                                return null;
                            }

                            if (
                                    !nuevoTexto.matches(
                                            "[\\p{L} ]*"
                                    )
                            ) {

                                return null;
                            }

                            return cambio;
                        }
                )
        );
    }

    /**
     * Permite únicamente números.
     */
    public static void soloDigitos(
            TextField campo,
            int longitudMaxima
    ) {

        campo.setTextFormatter(
                new TextFormatter<String>(
                        cambio -> {

                            String nuevoTexto =
                                    cambio.getControlNewText();

                            if (
                                    nuevoTexto.length()
                                            > longitudMaxima
                            ) {

                                return null;
                            }

                            if (
                                    !nuevoTexto.matches(
                                            "\\d*"
                                    )
                            ) {

                                return null;
                            }

                            return cambio;
                        }
                )
        );
    }

    /**
     * Limita la longitud de un campo sin alterar
     * los caracteres permitidos.
     */
    public static void limitarLongitud(
            TextField campo,
            int longitudMaxima
    ) {

        campo.setTextFormatter(
                new TextFormatter<String>(
                        cambio -> {

                            if (
                                    cambio
                                            .getControlNewText()
                                            .length()
                                            > longitudMaxima
                            ) {

                                return null;
                            }

                            return cambio;
                        }
                )
        );
    }
}