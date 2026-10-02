USE sistema_citas_medicas;

SET NAMES utf8mb4;

START TRANSACTION;


-- ============================================================
-- 1. MIGRAR RELACIONES DE ESPECIALIDADES DUPLICADAS
-- ============================================================
--
-- 69 -> 2  Cardiología
-- 70 -> 3  Pediatría
-- 71 -> 4  Traumatología
-- 73 -> 72 Ginecología y Obstetricia
--
-- ============================================================

INSERT IGNORE INTO medico_especialidad (
    id_medico,
    id_especialidad
)
SELECT
    me.id_medico,

    CASE me.id_especialidad
        WHEN 69 THEN 2
        WHEN 70 THEN 3
        WHEN 71 THEN 4
        WHEN 73 THEN 72
    END

FROM medico_especialidad me

INNER JOIN medicos m
    ON me.id_medico = m.id_medico

INNER JOIN usuarios u
    ON m.id_usuario = u.id_usuario

WHERE u.correo LIKE 'medico.%@demo.local'
  AND me.id_especialidad IN (
      69,
      70,
      71,
      73
  );


DELETE me
FROM medico_especialidad me

INNER JOIN medicos m
    ON me.id_medico = m.id_medico

INNER JOIN usuarios u
    ON m.id_usuario = u.id_usuario

WHERE u.correo LIKE 'medico.%@demo.local'
  AND me.id_especialidad IN (
      69,
      70,
      71,
      73
  );


-- ============================================================
-- 2. RENOMBRAR REGISTROS DUPLICADOS
-- ============================================================
--
-- Esto evita conflictos con el UNIQUE de especialidades.
--
-- ============================================================

UPDATE especialidades
SET
    nombre = 'INACTIVA_DEMO_CARDIOLOGIA',
    estado = FALSE
WHERE id_especialidad = 69;


UPDATE especialidades
SET
    nombre = 'INACTIVA_DEMO_PEDIATRIA',
    estado = FALSE
WHERE id_especialidad = 70;


UPDATE especialidades
SET
    nombre = 'INACTIVA_DEMO_TRAUMATOLOGIA',
    estado = FALSE
WHERE id_especialidad = 71;


UPDATE especialidades
SET
    nombre = 'INACTIVA_DEMO_GINECOLOGIA',
    estado = FALSE
WHERE id_especialidad = 73;


-- ============================================================
-- 3. CORREGIR LAS 5 ESPECIALIDADES DE LA DEMOSTRACIÓN
-- ============================================================

UPDATE especialidades
SET nombre = CONVERT(
    UNHEX('43617264696F6C6F67C3AD61')
    USING utf8mb4
)
WHERE id_especialidad = 2;


UPDATE especialidades
SET nombre = CONVERT(
    UNHEX('50656469617472C3AD61')
    USING utf8mb4
)
WHERE id_especialidad = 3;


UPDATE especialidades
SET nombre = CONVERT(
    UNHEX('547261756D61746F6C6F67C3AD61')
    USING utf8mb4
)
WHERE id_especialidad = 4;


UPDATE especialidades
SET nombre = 'Medicina Interna'
WHERE id_especialidad = 68;


UPDATE especialidades
SET nombre = CONVERT(
    UNHEX(
        '47696E65636F6C6F67C3AD612079204F62737465747269636961'
    )
    USING utf8mb4
)
WHERE id_especialidad = 72;


-- ============================================================
-- 4. MOSTRAR SOLO LAS 5 ESPECIALIDADES CON MÉDICOS
-- ============================================================
--
-- Evitamos que el paciente seleccione Medicina General
-- o Dermatología y obtenga "No existen médicos".
--
-- ============================================================

UPDATE especialidades
SET estado = FALSE;


UPDATE especialidades
SET estado = TRUE
WHERE id_especialidad IN (
    2,
    3,
    4,
    68,
    72
);


-- ============================================================
-- 5. CORREGIR Y LIMPIAR ESTABLECIMIENTOS
-- ============================================================
--
-- Los hospitales definitivos son IDs 3 al 8.
--
-- Desactivamos:
--   Centro de Salud Central
--   Hospital Público Norte
--   duplicados accidentales
--
-- No eliminamos registros físicamente.
--
-- ============================================================

UPDATE establecimientos
SET estado = FALSE;


UPDATE establecimientos
SET estado = TRUE
WHERE id_establecimiento BETWEEN 3 AND 8;


-- HCAM

UPDATE establecimientos
SET
    nombre = CONVERT(
        UNHEX(
            '486F73706974616C20646520457370656369616C696461646573204361726C6F7320416E6472616465204D6172C3AD6E20284843414D29'
        )
        USING utf8mb4
    ),
    ciudad = 'Quito',
    estado = TRUE
WHERE id_establecimiento = 3;


-- Quito Sur

UPDATE establecimientos
SET
    nombre = 'Hospital del IESS Quito Sur',
    ciudad = 'Quito',
    estado = TRUE
WHERE id_establecimiento = 4;


-- San Francisco de Quito

UPDATE establecimientos
SET
    nombre = 'Hospital San Francisco de Quito',
    ciudad = 'Quito',
    estado = TRUE
WHERE id_establecimiento = 5;


-- Los Ceibos

UPDATE establecimientos
SET
    nombre = 'Hospital del IESS Los Ceibos',
    ciudad = 'Guayaquil',
    estado = TRUE
WHERE id_establecimiento = 6;


-- Teodoro Maldonado Carbo

UPDATE establecimientos
SET
    nombre =
        'Hospital de Especialidades Teodoro Maldonado Carbo',
    ciudad = 'Guayaquil',
    estado = TRUE
WHERE id_establecimiento = 7;


-- Martha de Roldós

UPDATE establecimientos
SET
    nombre = CONVERT(
        UNHEX(
            '49455353204D617274686120646520526F6C64C3B373'
        )
        USING utf8mb4
    ),
    ciudad = 'Guayaquil',
    estado = TRUE
WHERE id_establecimiento = 8;


-- ============================================================
-- 6. CORREGIR PACIENTES DE PRUEBA
-- ============================================================


-- Juan Pérez

UPDATE usuarios
SET
    nombres = 'Juan',

    apellidos = CONVERT(
        UNHEX('50C3A972657A')
        USING utf8mb4
    )

WHERE correo =
      'juan.perez@prueba.local';


-- María Gómez

UPDATE usuarios
SET
    nombres = CONVERT(
        UNHEX('4D6172C3AD61')
        USING utf8mb4
    ),

    apellidos = CONVERT(
        UNHEX('47C3B36D657A')
        USING utf8mb4
    )

WHERE correo =
      'registro.paciente@prueba.local';


-- ============================================================
-- 7. CORREGIR NOMBRES DE MÉDICOS CON ACENTOS
-- ============================================================


-- Sebastián Naranjo

UPDATE usuarios
SET nombres = CONVERT(
    UNHEX('53656261737469C3A16E')
    USING utf8mb4
)
WHERE correo =
      'medico.hcam04@demo.local';


-- Sofía Jaramillo

UPDATE usuarios
SET nombres = CONVERT(
    UNHEX('536F66C3AD61')
    USING utf8mb4
)
WHERE correo =
      'medico.quitosur02@demo.local';


-- Nicolás Andrade

UPDATE usuarios
SET nombres = CONVERT(
    UNHEX('4E69636F6CC3A173')
    USING utf8mb4
)
WHERE correo =
      'medico.quitosur03@demo.local';


-- Lucía Salazar

UPDATE usuarios
SET nombres = CONVERT(
    UNHEX('4C7563C3AD61')
    USING utf8mb4
)
WHERE correo =
      'medico.sfquito02@demo.local';


-- Martín Vallejo

UPDATE usuarios
SET nombres = CONVERT(
    UNHEX('4D617274C3AD6E')
    USING utf8mb4
)
WHERE correo =
      'medico.sfquito03@demo.local';


-- Javier Alcívar

UPDATE usuarios
SET apellidos = CONVERT(
    UNHEX('416C63C3AD766172')
    USING utf8mb4
)
WHERE correo =
      'medico.ceibos01@demo.local';


-- María Fernanda Cedeño

UPDATE usuarios
SET
    nombres = CONVERT(
        UNHEX(
            '4D6172C3AD61204665726E616E6461'
        )
        USING utf8mb4
    ),

    apellidos = CONVERT(
        UNHEX('43656465C3B16F')
        USING utf8mb4
    )

WHERE correo =
      'medico.ceibos02@demo.local';


-- Andrés Peñafiel

UPDATE usuarios
SET
    nombres = CONVERT(
        UNHEX('416E6472C3A973')
        USING utf8mb4
    ),

    apellidos = CONVERT(
        UNHEX('5065C3B1616669656C')
        USING utf8mb4
    )

WHERE correo =
      'medico.ceibos03@demo.local';


-- Cristina Villacís

UPDATE usuarios
SET apellidos = CONVERT(
    UNHEX('56696C6C6163C3AD73')
    USING utf8mb4
)
WHERE correo =
      'medico.teodoro05@demo.local';


-- Patricia León

UPDATE usuarios
SET apellidos = CONVERT(
    UNHEX('4C65C3B36E')
    USING utf8mb4
)
WHERE correo =
      'medico.martha02@demo.local';


-- Mónica Cabrera

UPDATE usuarios
SET nombres = CONVERT(
    UNHEX('4DC3B36E696361')
    USING utf8mb4
)
WHERE correo =
      'medico.martha04@demo.local';


-- Lorena Macías

UPDATE usuarios
SET apellidos = CONVERT(
    UNHEX('4D6163C3AD6173')
    USING utf8mb4
)
WHERE correo =
      'medico.martha05@demo.local';


COMMIT;


-- ============================================================
-- 8. VERIFICAR HOSPITALES ACTIVOS
-- ============================================================

SELECT
    id_establecimiento,
    nombre,
    ciudad
FROM establecimientos
WHERE estado = TRUE
ORDER BY
    ciudad,
    nombre;


-- ============================================================
-- 9. VERIFICAR ESPECIALIDADES ACTIVAS
-- ============================================================

SELECT
    id_especialidad,
    nombre
FROM especialidades
WHERE estado = TRUE
ORDER BY nombre;


-- ============================================================
-- 10. VERIFICAR PACIENTES
-- ============================================================

SELECT
    correo,
    nombres,
    apellidos
FROM usuarios
WHERE correo IN (
    'juan.perez@prueba.local',
    'registro.paciente@prueba.local'
)
ORDER BY correo;


-- ============================================================
-- 11. VERIFICAR TODOS LOS MÉDICOS DEMO
-- ============================================================

SELECT
    est.nombre AS hospital,

    esp.nombre AS especialidad,

    CONCAT(
        u.nombres,
        ' ',
        u.apellidos
    ) AS medico

FROM medicos m

INNER JOIN usuarios u
    ON m.id_usuario =
       u.id_usuario

INNER JOIN medico_especialidad me
    ON m.id_medico =
       me.id_medico

INNER JOIN especialidades esp
    ON me.id_especialidad =
       esp.id_especialidad

INNER JOIN medico_establecimiento mest
    ON m.id_medico =
       mest.id_medico

INNER JOIN establecimientos est
    ON mest.id_establecimiento =
       est.id_establecimiento

WHERE u.correo LIKE
      'medico.%@demo.local'

  AND esp.estado = TRUE

  AND est.estado = TRUE

ORDER BY
    est.id_establecimiento,
    esp.nombre;


-- ============================================================
-- 12. VERIFICAR COBERTURA
-- ============================================================
--
-- Deben salir 30 filas:
-- 6 hospitales x 5 especialidades.
--
-- Cada combinación debe tener exactamente 1 médico.
--
-- ============================================================

SELECT
    est.nombre AS hospital,

    esp.nombre AS especialidad,

    COUNT(
        DISTINCT m.id_medico
    ) AS medicos_disponibles

FROM medicos m

INNER JOIN usuarios u
    ON m.id_usuario =
       u.id_usuario

INNER JOIN medico_especialidad me
    ON m.id_medico =
       me.id_medico

INNER JOIN especialidades esp
    ON me.id_especialidad =
       esp.id_especialidad

INNER JOIN medico_establecimiento mest
    ON m.id_medico =
       mest.id_medico

INNER JOIN establecimientos est
    ON mest.id_establecimiento =
       est.id_establecimiento

WHERE u.correo LIKE
      'medico.%@demo.local'

  AND esp.estado = TRUE

  AND est.estado = TRUE

GROUP BY
    est.id_establecimiento,
    est.nombre,
    esp.id_especialidad,
    esp.nombre

ORDER BY
    est.id_establecimiento,
    esp.nombre;


-- ============================================================
-- 13. VERIFICACIÓN FINAL
-- ============================================================

SELECT
    COUNT(*) AS hospitales_activos
FROM establecimientos
WHERE estado = TRUE;


SELECT
    COUNT(*) AS especialidades_activas
FROM especialidades
WHERE estado = TRUE;


SELECT
    COUNT(*) AS horarios_demo
FROM horarios_medicos hm

INNER JOIN medicos m
    ON hm.id_medico =
       m.id_medico

INNER JOIN usuarios u
    ON m.id_usuario =
       u.id_usuario

WHERE u.correo LIKE
      'medico.%@demo.local';