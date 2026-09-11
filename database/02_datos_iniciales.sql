-- ============================================================
--                 PROYECTO DE INGENIERÍA DE SOFTWARE II
-- ============================================================
--
--   SISTEMA DE GESTIÓN Y AGENDAMIENTO DE CITAS MÉDICAS
--
--   ARCHIVO:
--   02_datos_iniciales.sql
--
--   DESCRIPCIÓN:
--   Script encargado de registrar los datos iniciales
--   necesarios para el funcionamiento del sistema.
--
--   CONTENIDO:
--   • Selección de la base de datos.
--   • Registro de los roles iniciales del sistema.
--
--   IMPORTANTE:
--   Este archivo debe ejecutarse después de:
--
--   01_estructura.sql
--
-- ============================================================


-- ============================================================
--              1. SELECCIÓN DE LA BASE DE DATOS
-- ============================================================

USE sistema_citas_medicas;


-- ============================================================
--                 2. DATOS INICIALES: ROLES
-- ============================================================
--
-- PROPÓSITO:
-- Registrar los perfiles de usuario definidos para el sistema.
--
-- ROLES INICIALES:
--
-- • PACIENTE
--   Puede consultar y gestionar sus citas médicas.
--
-- • MEDICO
--   Puede consultar su agenda y gestionar la atención de citas.
--
-- • ADMINISTRADOR
--   Administra usuarios, roles y configuraciones del sistema.
--
-- • PERSONAL_ADMINISTRATIVO
--   Apoya la gestión de pacientes y citas médicas.
--
-- • COORDINADOR_MEDICO
--   Supervisa agendas y disponibilidad de los médicos.
--
-- ============================================================

INSERT IGNORE INTO roles (
    nombre,
    descripcion
)
VALUES
(
    'PACIENTE',
    'Usuario que consulta y gestiona sus citas médicas'
),
(
    'MEDICO',
    'Profesional que consulta y gestiona su agenda médica'
),
(
    'ADMINISTRADOR',
    'Usuario responsable de la administración del sistema'
),
(
    'PERSONAL_ADMINISTRATIVO',
    'Personal encargado de apoyar la gestión de pacientes y citas'
),
(
    'COORDINADOR_MEDICO',
    'Usuario encargado de supervisar agendas y disponibilidad médica'
);

-- ============================================================
--            3. DATOS INICIALES: ESPECIALIDADES
-- ============================================================

INSERT IGNORE INTO especialidades (
    nombre,
    descripcion
)
VALUES
(
    'Medicina General',
    'Atención médica general y evaluación inicial del paciente'
),
(
    'Cardiología',
    'Especialidad encargada del diagnóstico y tratamiento de enfermedades cardiovasculares'
),
(
    'Pediatría',
    'Atención médica especializada para niños y adolescentes'
),
(
    'Traumatología',
    'Atención de lesiones y enfermedades del sistema musculoesquelético'
),
(
    'Dermatología',
    'Diagnóstico y tratamiento de enfermedades relacionadas con la piel'
);


-- ============================================================
--           4. DATOS INICIALES: ESTABLECIMIENTOS
-- ============================================================

INSERT INTO establecimientos (
    nombre,
    direccion,
    telefono,
    ciudad
)
SELECT
    'Hospital Público Norte',
    'Av. Principal 123',
    '042000001',
    'Guayaquil'
WHERE NOT EXISTS (
    SELECT 1
    FROM establecimientos
    WHERE nombre = 'Hospital Público Norte'
);

INSERT INTO establecimientos (
    nombre,
    direccion,
    telefono,
    ciudad
)
SELECT
    'Centro de Salud Central',
    'Av. Central 456',
    '042000002',
    'Guayaquil'
WHERE NOT EXISTS (
    SELECT 1
    FROM establecimientos
    WHERE nombre = 'Centro de Salud Central'
);

-- ============================================================
--              5. USUARIO MÉDICO DE PRUEBA
-- ============================================================
--
-- NOTA:
-- La contraseña utilizada aquí es únicamente ficticia.
-- Cuando Java esté implementado, las contraseñas reales
-- deberán generarse mediante un algoritmo de hash seguro.
--
-- ============================================================

INSERT INTO usuarios (
    id_rol,
    nombres,
    apellidos,
    cedula,
    correo,
    password_hash,
    telefono
)
SELECT
    r.id_rol,
    'Carlos',
    'Mendoza',
    '0912345678',
    'carlos.mendoza@prueba.local',
    'HASH_PRUEBA_NO_USAR_EN_PRODUCCION',
    '0990000001'
FROM roles r
WHERE r.nombre = 'MEDICO'
AND NOT EXISTS (
    SELECT 1
    FROM usuarios
    WHERE cedula = '0912345678'
);


-- ============================================================
--                  6. MÉDICO DE PRUEBA
-- ============================================================

INSERT INTO medicos (
    id_usuario,
    cedula_profesional,
    observacion
)
SELECT
    u.id_usuario,
    'MED-EC-0001',
    'Médico creado para pruebas del sistema'
FROM usuarios u
WHERE u.cedula = '0912345678'
AND NOT EXISTS (
    SELECT 1
    FROM medicos
    WHERE cedula_profesional = 'MED-EC-0001'
);


-- ============================================================
--          7. ASIGNACIÓN DE ESPECIALIDAD AL MÉDICO
-- ============================================================

INSERT IGNORE INTO medico_especialidad (
    id_medico,
    id_especialidad
)
SELECT
    m.id_medico,
    e.id_especialidad
FROM medicos m
INNER JOIN usuarios u
    ON u.id_usuario = m.id_usuario
INNER JOIN especialidades e
    ON e.nombre = 'Medicina General'
WHERE u.cedula = '0912345678';


-- ============================================================
--         8. ASIGNACIÓN DE ESTABLECIMIENTO AL MÉDICO
-- ============================================================

INSERT IGNORE INTO medico_establecimiento (
    id_medico,
    id_establecimiento
)
SELECT
    m.id_medico,
    est.id_establecimiento
FROM medicos m
INNER JOIN usuarios u
    ON u.id_usuario = m.id_usuario
INNER JOIN establecimientos est
    ON est.nombre = 'Hospital Público Norte'
WHERE u.cedula = '0912345678';


-- ============================================================
--               9. HORARIO MÉDICO DE PRUEBA
-- ============================================================
--
-- Horario:
-- Lunes
-- 08:00 - 12:00
-- Citas de 30 minutos
--
-- ============================================================

INSERT INTO horarios_medicos (
    id_medico,
    id_establecimiento,
    dia_semana,
    hora_inicio,
    hora_fin,
    duracion_cita_minutos
)
SELECT
    m.id_medico,
    est.id_establecimiento,
    1,
    '08:00:00',
    '12:00:00',
    30
FROM medicos m
INNER JOIN usuarios u
    ON u.id_usuario = m.id_usuario
INNER JOIN establecimientos est
    ON est.nombre = 'Hospital Público Norte'
WHERE u.cedula = '0912345678'
AND NOT EXISTS (
    SELECT 1
    FROM horarios_medicos hm
    WHERE hm.id_medico = m.id_medico
      AND hm.id_establecimiento = est.id_establecimiento
      AND hm.dia_semana = 1
      AND hm.hora_inicio = '08:00:00'
);


-- ============================================================
--              10. USUARIO PACIENTE DE PRUEBA
-- ============================================================

INSERT INTO usuarios (
    id_rol,
    nombres,
    apellidos,
    cedula,
    correo,
    password_hash,
    telefono
)
SELECT
    r.id_rol,
    'Ana',
    'Torres',
    '0923456789',
    'ana.torres@prueba.local',
    'HASH_PRUEBA_NO_USAR_EN_PRODUCCION',
    '0990000002'
FROM roles r
WHERE r.nombre = 'PACIENTE'
AND NOT EXISTS (
    SELECT 1
    FROM usuarios
    WHERE cedula = '0923456789'
);


-- ============================================================
--                 11. PACIENTE DE PRUEBA
-- ============================================================

INSERT INTO pacientes (
    id_usuario,
    fecha_nacimiento,
    direccion,
    sexo,
    contacto_emergencia,
    telefono_emergencia
)
SELECT
    u.id_usuario,
    '1995-05-10',
    'Guayaquil',
    'Femenino',
    'María Torres',
    '0990000003'
FROM usuarios u
WHERE u.cedula = '0923456789'
AND NOT EXISTS (
    SELECT 1
    FROM pacientes
    WHERE id_usuario = u.id_usuario
);

-- ============================================================
--                  12. CITA MÉDICA DE PRUEBA
-- ============================================================

INSERT INTO citas (
    id_paciente,
    id_medico,
    id_especialidad,
    id_establecimiento,
    fecha_cita,
    hora_inicio,
    hora_fin,
    motivo_consulta
)
SELECT
    p.id_paciente,
    m.id_medico,
    e.id_especialidad,
    est.id_establecimiento,
    '2026-09-14',
    '08:00:00',
    '08:30:00',
    'Consulta médica general'
FROM pacientes p

INNER JOIN usuarios up
    ON up.id_usuario = p.id_usuario

CROSS JOIN medicos m

INNER JOIN usuarios um
    ON um.id_usuario = m.id_usuario

INNER JOIN especialidades e
    ON e.nombre = 'Medicina General'

INNER JOIN establecimientos est
    ON est.nombre = 'Hospital Público Norte'

WHERE up.cedula = '0923456789'
  AND um.cedula = '0912345678'

  AND NOT EXISTS (
      SELECT 1
      FROM citas c
      WHERE c.id_medico = m.id_medico
        AND c.fecha_cita = '2026-09-14'
        AND c.hora_inicio = '08:00:00'
  );

-- ============================================================
--                  FIN DEL ARCHIVO 02
-- ============================================================
