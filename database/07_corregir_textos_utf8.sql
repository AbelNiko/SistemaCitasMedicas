USE sistema_citas_medicas;

SET NAMES utf8mb4;

START TRANSACTION;


-- ============================================================
-- 1. CORREGIR ESPECIALIDADES PRINCIPALES
-- ============================================================

UPDATE especialidades
SET
    nombre = CONVERT(
        UNHEX('4D65646963696E612047656E6572616C')
        USING utf8mb4
    ),
    estado = TRUE
WHERE id_especialidad = 1;


UPDATE especialidades
SET
    nombre = CONVERT(
        UNHEX('43617264696F6C6F67C3AD61')
        USING utf8mb4
    ),
    estado = TRUE
WHERE id_especialidad = 2;


UPDATE especialidades
SET
    nombre = CONVERT(
        UNHEX('50656469617472C3AD61')
        USING utf8mb4
    ),
    estado = TRUE
WHERE id_especialidad = 3;


UPDATE especialidades
SET
    nombre = CONVERT(
        UNHEX('547261756D61746F6C6F67C3AD61')
        USING utf8mb4
    ),
    estado = TRUE
WHERE id_especialidad = 4;


UPDATE especialidades
SET
    nombre = CONVERT(
        UNHEX('4465726D61746F6C6F67C3AD61')
        USING utf8mb4
    ),
    estado = TRUE
WHERE id_especialidad = 5;


UPDATE especialidades
SET
    nombre = 'Medicina Interna',
    estado = TRUE
WHERE id_especialidad = 68;


UPDATE especialidades
SET
    nombre = CONVERT(
        UNHEX(
            '47696E65636F6C6F67C3AD612079204F62737465747269636961'
        )
        USING utf8mb4
    ),
    estado = TRUE
WHERE id_especialidad = 72;


-- ============================================================
-- 2. MOVER RELACIONES DEMO HACIA ESPECIALIDADES CANÓNICAS
-- ============================================================


-- Cardiología: 69 -> 2

INSERT IGNORE INTO medico_especialidad (
    id_medico,
    id_especialidad
)
SELECT
    me.id_medico,
    2
FROM medico_especialidad me
INNER JOIN medicos m
    ON me.id_medico = m.id_medico
INNER JOIN usuarios u
    ON m.id_usuario = u.id_usuario
WHERE u.correo LIKE 'medico.%@demo.local'
  AND me.id_especialidad = 69;


DELETE me
FROM medico_especialidad me
INNER JOIN medicos m
    ON me.id_medico = m.id_medico
INNER JOIN usuarios u
    ON m.id_usuario = u.id_usuario
WHERE u.correo LIKE 'medico.%@demo.local'
  AND me.id_especialidad = 69;


-- Pediatría: 70 -> 3

INSERT IGNORE INTO medico_especialidad (
    id_medico,
    id_especialidad
)
SELECT
    me.id_medico,
    3
FROM medico_especialidad me
INNER JOIN medicos m
    ON me.id_medico = m.id_medico
INNER JOIN usuarios u
    ON m.id_usuario = u.id_usuario
WHERE u.correo LIKE 'medico.%@demo.local'
  AND me.id_especialidad = 70;


DELETE me
FROM medico_especialidad me
INNER JOIN medicos m
    ON me.id_medico = m.id_medico
INNER JOIN usuarios u
    ON m.id_usuario = u.id_usuario
WHERE u.correo LIKE 'medico.%@demo.local'
  AND me.id_especialidad = 70;


-- Traumatología: 71 -> 4

INSERT IGNORE INTO medico_especialidad (
    id_medico,
    id_especialidad
)
SELECT
    me.id_medico,
    4
FROM medico_especialidad me
INNER JOIN medicos m
    ON me.id_medico = m.id_medico
INNER JOIN usuarios u
    ON m.id_usuario = u.id_usuario
WHERE u.correo LIKE 'medico.%@demo.local'
  AND me.id_especialidad = 71;


DELETE me
FROM medico_especialidad me
INNER JOIN medicos m
    ON me.id_medico = m.id_medico
INNER JOIN usuarios u
    ON m.id_usuario = u.id_usuario
WHERE u.correo LIKE 'medico.%@demo.local'
  AND me.id_especialidad = 71;


-- Si existiera alguna relación con la especialidad duplicada 73,
-- se mueve hacia la especialidad 72.

INSERT IGNORE INTO medico_especialidad (
    id_medico,
    id_especialidad
)
SELECT
    me.id_medico,
    72
FROM medico_especialidad me
INNER JOIN medicos m
    ON me.id_medico = m.id_medico
INNER JOIN usuarios u
    ON m.id_usuario = u.id_usuario
WHERE u.correo LIKE 'medico.%@demo.local'
  AND me.id_especialidad = 73;


DELETE me
FROM medico_especialidad me
INNER JOIN medicos m
    ON me.id_medico = m.id_medico
INNER JOIN usuarios u
    ON m.id_usuario = u.id_usuario
WHERE u.correo LIKE 'medico.%@demo.local'
  AND me.id_especialidad = 73;


-- Desactivar especialidades duplicadas creadas durante
-- la carga anterior.

UPDATE especialidades
SET estado = FALSE
WHERE id_especialidad IN (
    69,
    70,
    71,
    73
);


-- ============================================================
-- 3. CORREGIR LOS 6 HOSPITALES
-- ============================================================


-- HCAM

UPDATE establecimientos
SET
    nombre = CONVERT(
        UNHEX(
            '486F73706974616C20646520457370656369616C696461646573204361726C6F7320416E6472616465204D6172C3AD6E20284843414D29'
        )
        USING utf8mb4
    ),
    ciudad = 'Quito'
WHERE id_establecimiento = 3;


-- IESS Quito Sur

UPDATE establecimientos
SET
    nombre = 'Hospital del IESS Quito Sur',
    ciudad = 'Quito'
WHERE id_establecimiento = 4;


-- San Francisco de Quito

UPDATE establecimientos
SET
    nombre = 'Hospital San Francisco de Quito',
    ciudad = 'Quito'
WHERE id_establecimiento = 5;


-- IESS Los Ceibos

UPDATE establecimientos
SET
    nombre = 'Hospital del IESS Los Ceibos',
    ciudad = 'Guayaquil'
WHERE id_establecimiento = 6;


-- Teodoro Maldonado Carbo

UPDATE establecimientos
SET
    nombre =
        'Hospital de Especialidades Teodoro Maldonado Carbo',
    ciudad = 'Guayaquil'
WHERE id_establecimiento = 7;


-- IESS Martha de Roldós

UPDATE establecimientos
SET
    nombre = CONVERT(
        UNHEX(
            '49455353204D617274686120646520526F6C64C3B373'
        )
        USING utf8mb4
    ),
    ciudad = 'Guayaquil'
WHERE id_establecimiento = 8;


-- ============================================================
-- 4. CORREGIR PACIENTES DE PRUEBA
-- ============================================================


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
WHERE correo = 'registro.paciente@prueba.local';


-- Juan Pérez

UPDATE usuarios
SET
    nombres = 'Juan',
    apellidos = CONVERT(
        UNHEX('50C3A972657A')
        USING utf8mb4
    )
WHERE correo = 'juan.perez@prueba.local';


-- ============================================================
-- 5. CORREGIR MÉDICOS CON CARACTERES ACENTUADOS
-- ============================================================


-- Sebastián Naranjo

UPDATE usuarios
SET nombres = CONVERT(
    UNHEX('53656261737469C3A16E')
    USING utf8mb4
)
WHERE correo = 'medico.hcam04@demo.local';


-- Sofía Jaramillo

UPDATE usuarios
SET nombres = CONVERT(
    UNHEX('536F66C3AD61')
    USING utf8mb4
)
WHERE correo = 'medico.quitosur02@demo.local';


-- Nicolás Andrade

UPDATE usuarios
SET nombres = CONVERT(
    UNHEX('4E69636F6CC3A173')
    USING utf8mb4
)
WHERE correo = 'medico.quitosur03@demo.local';


-- Lucía Salazar

UPDATE usuarios
SET nombres = CONVERT(
    UNHEX('4C7563C3AD61')
    USING utf8mb4
)
WHERE correo = 'medico.sfquito02@demo.local';


-- Martín Vallejo

UPDATE usuarios
SET nombres = CONVERT(
    UNHEX('4D617274C3AD6E')
    USING utf8mb4
)
WHERE correo = 'medico.sfquito03@demo.local';


-- Javier Alcívar

UPDATE usuarios
SET apellidos = CONVERT(
    UNHEX('416C63C3AD766172')
    USING utf8mb4
)
WHERE correo = 'medico.ceibos01@demo.local';


-- María Fernanda Cedeño

UPDATE usuarios
SET
    nombres = CONVERT(
        UNHEX('4D6172C3AD61204665726E616E6461')
        USING utf8mb4
    ),
    apellidos = CONVERT(
        UNHEX('43656465C3B16F')
        USING utf8mb4
    )
WHERE correo = 'medico.ceibos02@demo.local';


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
WHERE correo = 'medico.ceibos03@demo.local';


-- Cristina Villacís

UPDATE usuarios
SET apellidos = CONVERT(
    UNHEX('56696C6C6163C3AD73')
    USING utf8mb4
)
WHERE correo = 'medico.teodoro05@demo.local';


-- Patricia León

UPDATE usuarios
SET apellidos = CONVERT(
    UNHEX('4C65C3B36E')
    USING utf8mb4
)
WHERE correo = 'medico.martha02@demo.local';


-- Mónica Cabrera

UPDATE usuarios
SET nombres = CONVERT(
    UNHEX('4DC3B36E696361')
    USING utf8mb4
)
WHERE correo = 'medico.martha04@demo.local';


-- Lorena Macías

UPDATE usuarios
SET apellidos = CONVERT(
    UNHEX('4D6163C3AD6173')
    USING utf8mb4
)
WHERE correo = 'medico.martha05@demo.local';


COMMIT;


-- ============================================================
-- 6. VERIFICACIÓN DE HOSPITALES
-- ============================================================

SELECT
    id_establecimiento,
    nombre,
    ciudad,
    estado
FROM establecimientos
WHERE id_establecimiento BETWEEN 3 AND 8
ORDER BY id_establecimiento;


-- ============================================================
-- 7. VERIFICACIÓN DE ESPECIALIDADES ACTIVAS
-- ============================================================

SELECT
    id_especialidad,
    nombre,
    estado
FROM especialidades
WHERE id_especialidad IN (
    1,
    2,
    3,
    4,
    5,
    68,
    69,
    70,
    71,
    72,
    73
)
ORDER BY id_especialidad;


-- ============================================================
-- 8. VERIFICACIÓN DE PACIENTES
-- ============================================================

SELECT
    correo,
    nombres,
    apellidos
FROM usuarios
WHERE correo IN (
    'registro.paciente@prueba.local',
    'juan.perez@prueba.local'
)
ORDER BY correo;


-- ============================================================
-- 9. VERIFICACIÓN DE MÉDICOS
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

ORDER BY
    est.id_establecimiento,
    esp.nombre;


-- ============================================================
-- 10. VERIFICACIÓN FINAL DE INTEGRIDAD
-- ============================================================

SELECT
    COUNT(*) AS relaciones_especialidad
FROM medico_especialidad me

INNER JOIN medicos m
    ON me.id_medico =
       m.id_medico

INNER JOIN usuarios u
    ON m.id_usuario =
       u.id_usuario

WHERE u.correo LIKE
      'medico.%@demo.local';


SELECT
    COUNT(*) AS relaciones_hospital
FROM medico_establecimiento mest

INNER JOIN medicos m
    ON mest.id_medico =
       m.id_medico

INNER JOIN usuarios u
    ON m.id_usuario =
       u.id_usuario

WHERE u.correo LIKE
      'medico.%@demo.local';


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