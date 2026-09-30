-- ============================================================
-- MEDIAPPOINT
-- MIGRACIÓN 04 - SEGURIDAD Y RECUPERACIÓN DE CUENTA
-- ============================================================
--
-- Incorpora una pregunta de seguridad para verificar
-- la identidad del usuario durante la recuperación
-- de contraseña.
--
-- La respuesta nunca se almacena como texto plano.
-- Únicamente se guarda su hash BCrypt.
--
-- También se preparan campos para controlar intentos
-- fallidos de recuperación.
-- ============================================================

ALTER TABLE usuarios

    ADD COLUMN pregunta_seguridad VARCHAR(50) NULL
        AFTER telefono,

    ADD COLUMN respuesta_seguridad_hash VARCHAR(255) NULL
        AFTER pregunta_seguridad,

    ADD COLUMN intentos_recuperacion TINYINT UNSIGNED
        NOT NULL DEFAULT 0
        AFTER respuesta_seguridad_hash,

    ADD COLUMN bloqueado_recuperacion_hasta DATETIME NULL
        AFTER intentos_recuperacion;