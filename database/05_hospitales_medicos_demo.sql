USE sistema_citas_medicas;

-- ============================================================
-- MEDIAPPOINT
-- DATOS DEMO: HOSPITALES, MÉDICOS, ESPECIALIDADES Y HORARIOS
-- ============================================================
--
-- IMPORTANTE:
-- Los médicos de este archivo son completamente ficticios.
-- Se utilizan exclusivamente para demostración académica.
--
-- Se crean:
--   6 hospitales
--   5 médicos por hospital
--   30 médicos en total
--   5 especialidades
--   horarios de lunes a viernes
--
-- ============================================================


-- ============================================================
-- 1. VERIFICAR ROL MÉDICO
-- ============================================================

SET @id_rol_medico = (
    SELECT id_rol
    FROM roles
    WHERE UPPER(nombre) = 'MEDICO'
    LIMIT 1
);


-- ============================================================
-- 2. OBTENER HASH BCrypt DE UNA CUENTA MÉDICA DEMO EXISTENTE
-- ============================================================
--
-- No se escribe ni se expone ninguna contraseña.
--
-- Las nuevas cuentas demo reutilizan únicamente el hash de
-- una cuenta MEDICO ya existente en el entorno académico.
--
-- ============================================================

SET @password_hash_demo = (
    SELECT u.password_hash
    FROM usuarios u
    INNER JOIN roles r
        ON u.id_rol = r.id_rol
    WHERE UPPER(r.nombre) = 'MEDICO'
      AND u.estado = TRUE
    ORDER BY u.id_usuario
    LIMIT 1
);


-- ============================================================
-- 3. ESPECIALIDADES
-- ============================================================

INSERT INTO especialidades (
    nombre,
    descripcion,
    estado
)
SELECT
    'Medicina Interna',
    'Atención integral de enfermedades del adulto.',
    TRUE
WHERE NOT EXISTS (
    SELECT 1
    FROM especialidades
    WHERE nombre = 'Medicina Interna'
);

INSERT INTO especialidades (
    nombre,
    descripcion,
    estado
)
SELECT
    'Cardiología',
    'Diagnóstico y tratamiento de enfermedades cardiovasculares.',
    TRUE
WHERE NOT EXISTS (
    SELECT 1
    FROM especialidades
    WHERE nombre = 'Cardiología'
);

INSERT INTO especialidades (
    nombre,
    descripcion,
    estado
)
SELECT
    'Pediatría',
    'Atención médica integral de niños y adolescentes.',
    TRUE
WHERE NOT EXISTS (
    SELECT 1
    FROM especialidades
    WHERE nombre = 'Pediatría'
);

INSERT INTO especialidades (
    nombre,
    descripcion,
    estado
)
SELECT
    'Traumatología',
    'Atención de lesiones y enfermedades del sistema musculoesquelético.',
    TRUE
WHERE NOT EXISTS (
    SELECT 1
    FROM especialidades
    WHERE nombre = 'Traumatología'
);

INSERT INTO especialidades (
    nombre,
    descripcion,
    estado
)
SELECT
    'Ginecología y Obstetricia',
    'Atención integral de la salud ginecológica y obstétrica.',
    TRUE
WHERE NOT EXISTS (
    SELECT 1
    FROM especialidades
    WHERE nombre = 'Ginecología y Obstetricia'
);


-- ============================================================
-- 4. HOSPITALES
-- ============================================================
--
-- La dirección se mantiene genérica para este entorno demo.
-- No se utilizan datos operativos sensibles.
--
-- ============================================================


-- QUITO

INSERT INTO establecimientos (
    nombre,
    direccion,
    telefono,
    ciudad,
    estado
)
SELECT
    'Hospital de Especialidades Carlos Andrade Marín (HCAM)',
    'Quito, Pichincha',
    NULL,
    'Quito',
    TRUE
WHERE NOT EXISTS (
    SELECT 1
    FROM establecimientos
    WHERE nombre =
        'Hospital de Especialidades Carlos Andrade Marín (HCAM)'
);

INSERT INTO establecimientos (
    nombre,
    direccion,
    telefono,
    ciudad,
    estado
)
SELECT
    'Hospital del IESS Quito Sur',
    'Quito, Pichincha',
    NULL,
    'Quito',
    TRUE
WHERE NOT EXISTS (
    SELECT 1
    FROM establecimientos
    WHERE nombre =
        'Hospital del IESS Quito Sur'
);

INSERT INTO establecimientos (
    nombre,
    direccion,
    telefono,
    ciudad,
    estado
)
SELECT
    'Hospital San Francisco de Quito',
    'Quito, Pichincha',
    NULL,
    'Quito',
    TRUE
WHERE NOT EXISTS (
    SELECT 1
    FROM establecimientos
    WHERE nombre =
        'Hospital San Francisco de Quito'
);


-- GUAYAQUIL

INSERT INTO establecimientos (
    nombre,
    direccion,
    telefono,
    ciudad,
    estado
)
SELECT
    'Hospital del IESS Los Ceibos',
    'Guayaquil, Guayas',
    NULL,
    'Guayaquil',
    TRUE
WHERE NOT EXISTS (
    SELECT 1
    FROM establecimientos
    WHERE nombre =
        'Hospital del IESS Los Ceibos'
);

INSERT INTO establecimientos (
    nombre,
    direccion,
    telefono,
    ciudad,
    estado
)
SELECT
    'Hospital de Especialidades Teodoro Maldonado Carbo',
    'Guayaquil, Guayas',
    NULL,
    'Guayaquil',
    TRUE
WHERE NOT EXISTS (
    SELECT 1
    FROM establecimientos
    WHERE nombre =
        'Hospital de Especialidades Teodoro Maldonado Carbo'
);

INSERT INTO establecimientos (
    nombre,
    direccion,
    telefono,
    ciudad,
    estado
)
SELECT
    'IESS Martha de Roldós',
    'Guayaquil, Guayas',
    NULL,
    'Guayaquil',
    TRUE
WHERE NOT EXISTS (
    SELECT 1
    FROM establecimientos
    WHERE nombre =
        'IESS Martha de Roldós'
);


-- ============================================================
-- 5. TABLA TEMPORAL DE MÉDICOS FICTICIOS
-- ============================================================

DROP TEMPORARY TABLE IF EXISTS tmp_medicos_demo;

CREATE TEMPORARY TABLE tmp_medicos_demo (

    hospital VARCHAR(150) NOT NULL,

    nombres VARCHAR(100) NOT NULL,

    apellidos VARCHAR(100) NOT NULL,

    cedula VARCHAR(10) NOT NULL,

    correo VARCHAR(150) NOT NULL,

    cedula_profesional VARCHAR(50) NOT NULL,

    especialidad VARCHAR(100) NOT NULL,

    PRIMARY KEY (correo)

)
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;


-- ============================================================
-- 6. 5 MÉDICOS - HCAM
-- ============================================================

INSERT INTO tmp_medicos_demo VALUES

(
    'Hospital de Especialidades Carlos Andrade Marín (HCAM)',
    'Mateo',
    'Cevallos',
    '1790000001',
    'medico.hcam01@demo.local',
    'DEMO-HCAM-001',
    'Cardiología'
),

(
    'Hospital de Especialidades Carlos Andrade Marín (HCAM)',
    'Andrea',
    'Paredes',
    '1790000002',
    'medico.hcam02@demo.local',
    'DEMO-HCAM-002',
    'Medicina Interna'
),

(
    'Hospital de Especialidades Carlos Andrade Marín (HCAM)',
    'Valeria',
    'Montalvo',
    '1790000003',
    'medico.hcam03@demo.local',
    'DEMO-HCAM-003',
    'Pediatría'
),

(
    'Hospital de Especialidades Carlos Andrade Marín (HCAM)',
    'Sebastián',
    'Naranjo',
    '1790000004',
    'medico.hcam04@demo.local',
    'DEMO-HCAM-004',
    'Traumatología'
),

(
    'Hospital de Especialidades Carlos Andrade Marín (HCAM)',
    'Camila',
    'Herrera',
    '1790000005',
    'medico.hcam05@demo.local',
    'DEMO-HCAM-005',
    'Ginecología y Obstetricia'
);


-- ============================================================
-- 7. 5 MÉDICOS - IESS QUITO SUR
-- ============================================================

INSERT INTO tmp_medicos_demo VALUES

(
    'Hospital del IESS Quito Sur',
    'Daniel',
    'Ledesma',
    '1790000006',
    'medico.quitosur01@demo.local',
    'DEMO-QSUR-001',
    'Cardiología'
),

(
    'Hospital del IESS Quito Sur',
    'Sofía',
    'Jaramillo',
    '1790000007',
    'medico.quitosur02@demo.local',
    'DEMO-QSUR-002',
    'Medicina Interna'
),

(
    'Hospital del IESS Quito Sur',
    'Nicolás',
    'Andrade',
    '1790000008',
    'medico.quitosur03@demo.local',
    'DEMO-QSUR-003',
    'Pediatría'
),

(
    'Hospital del IESS Quito Sur',
    'Paula',
    'Guerrero',
    '1790000009',
    'medico.quitosur04@demo.local',
    'DEMO-QSUR-004',
    'Traumatología'
),

(
    'Hospital del IESS Quito Sur',
    'Renata',
    'Cordero',
    '1790000010',
    'medico.quitosur05@demo.local',
    'DEMO-QSUR-005',
    'Ginecología y Obstetricia'
);


-- ============================================================
-- 8. 5 MÉDICOS - SAN FRANCISCO DE QUITO
-- ============================================================

INSERT INTO tmp_medicos_demo VALUES

(
    'Hospital San Francisco de Quito',
    'Diego',
    'Rosales',
    '1790000011',
    'medico.sfquito01@demo.local',
    'DEMO-SFQ-001',
    'Cardiología'
),

(
    'Hospital San Francisco de Quito',
    'Lucía',
    'Salazar',
    '1790000012',
    'medico.sfquito02@demo.local',
    'DEMO-SFQ-002',
    'Medicina Interna'
),

(
    'Hospital San Francisco de Quito',
    'Martín',
    'Vallejo',
    '1790000013',
    'medico.sfquito03@demo.local',
    'DEMO-SFQ-003',
    'Pediatría'
),

(
    'Hospital San Francisco de Quito',
    'Carolina',
    'Viteri',
    '1790000014',
    'medico.sfquito04@demo.local',
    'DEMO-SFQ-004',
    'Traumatología'
),

(
    'Hospital San Francisco de Quito',
    'Gabriela',
    'Zambrano',
    '1790000015',
    'medico.sfquito05@demo.local',
    'DEMO-SFQ-005',
    'Ginecología y Obstetricia'
);


-- ============================================================
-- 9. 5 MÉDICOS - IESS LOS CEIBOS
-- ============================================================

INSERT INTO tmp_medicos_demo VALUES

(
    'Hospital del IESS Los Ceibos',
    'Javier',
    'Alcívar',
    '0990000016',
    'medico.ceibos01@demo.local',
    'DEMO-CEI-001',
    'Cardiología'
),

(
    'Hospital del IESS Los Ceibos',
    'María Fernanda',
    'Cedeño',
    '0990000017',
    'medico.ceibos02@demo.local',
    'DEMO-CEI-002',
    'Medicina Interna'
),

(
    'Hospital del IESS Los Ceibos',
    'Andrés',
    'Peñafiel',
    '0990000018',
    'medico.ceibos03@demo.local',
    'DEMO-CEI-003',
    'Pediatría'
),

(
    'Hospital del IESS Los Ceibos',
    'Daniela',
    'Vera',
    '0990000019',
    'medico.ceibos04@demo.local',
    'DEMO-CEI-004',
    'Traumatología'
),

(
    'Hospital del IESS Los Ceibos',
    'Alejandra',
    'Mera',
    '0990000020',
    'medico.ceibos05@demo.local',
    'DEMO-CEI-005',
    'Ginecología y Obstetricia'
);


-- ============================================================
-- 10. 5 MÉDICOS - TEODORO MALDONADO CARBO
-- ============================================================

INSERT INTO tmp_medicos_demo VALUES

(
    'Hospital de Especialidades Teodoro Maldonado Carbo',
    'Carlos',
    'Zamora',
    '0990000021',
    'medico.teodoro01@demo.local',
    'DEMO-TMC-001',
    'Cardiología'
),

(
    'Hospital de Especialidades Teodoro Maldonado Carbo',
    'Elena',
    'Burgos',
    '0990000022',
    'medico.teodoro02@demo.local',
    'DEMO-TMC-002',
    'Medicina Interna'
),

(
    'Hospital de Especialidades Teodoro Maldonado Carbo',
    'Felipe',
    'Plaza',
    '0990000023',
    'medico.teodoro03@demo.local',
    'DEMO-TMC-003',
    'Pediatría'
),

(
    'Hospital de Especialidades Teodoro Maldonado Carbo',
    'Natalia',
    'Castro',
    '0990000024',
    'medico.teodoro04@demo.local',
    'DEMO-TMC-004',
    'Traumatología'
),

(
    'Hospital de Especialidades Teodoro Maldonado Carbo',
    'Cristina',
    'Villacís',
    '0990000025',
    'medico.teodoro05@demo.local',
    'DEMO-TMC-005',
    'Ginecología y Obstetricia'
);


-- ============================================================
-- 11. 5 MÉDICOS - MARTHA DE ROLDÓS
-- ============================================================

INSERT INTO tmp_medicos_demo VALUES

(
    'IESS Martha de Roldós',
    'Ricardo',
    'Molina',
    '0990000026',
    'medico.martha01@demo.local',
    'DEMO-MAR-001',
    'Cardiología'
),

(
    'IESS Martha de Roldós',
    'Patricia',
    'León',
    '0990000027',
    'medico.martha02@demo.local',
    'DEMO-MAR-002',
    'Medicina Interna'
),

(
    'IESS Martha de Roldós',
    'Esteban',
    'Barreiro',
    '0990000028',
    'medico.martha03@demo.local',
    'DEMO-MAR-003',
    'Pediatría'
),

(
    'IESS Martha de Roldós',
    'Mónica',
    'Cabrera',
    '0990000029',
    'medico.martha04@demo.local',
    'DEMO-MAR-004',
    'Traumatología'
),

(
    'IESS Martha de Roldós',
    'Lorena',
    'Macías',
    '0990000030',
    'medico.martha05@demo.local',
    'DEMO-MAR-005',
    'Ginecología y Obstetricia'
);


-- ============================================================
-- 12. CREAR USUARIOS MÉDICOS
-- ============================================================
--
-- Si el script se ejecuta nuevamente no duplica usuarios.
--
-- ============================================================

INSERT INTO usuarios (
    id_rol,
    nombres,
    apellidos,
    cedula,
    correo,
    password_hash,
    telefono,
    estado
)
SELECT
    @id_rol_medico,
    d.nombres,
    d.apellidos,
    d.cedula,
    d.correo,
    @password_hash_demo,
    NULL,
    TRUE

FROM tmp_medicos_demo d

WHERE @id_rol_medico IS NOT NULL
  AND @password_hash_demo IS NOT NULL

  AND NOT EXISTS (
      SELECT 1
      FROM usuarios u
      WHERE u.correo = d.correo COLLATE utf8mb4_unicode_ci
         OR u.cedula = d.cedula
  );


-- ============================================================
-- 13. CREAR PERFILES MÉDICOS
-- ============================================================

INSERT INTO medicos (
    id_usuario,
    cedula_profesional,
    observacion,
    estado
)
SELECT
    u.id_usuario,
    d.cedula_profesional,
    'Médico ficticio para demostración académica de MediAppoint.',
    TRUE

FROM tmp_medicos_demo d

INNER JOIN usuarios u
    ON u.correo = d.correo COLLATE utf8mb4_unicode_ci

WHERE NOT EXISTS (
    SELECT 1
    FROM medicos m
    WHERE m.id_usuario = u.id_usuario
       OR m.cedula_profesional = d.cedula_profesional
);


-- ============================================================
-- 14. ASIGNAR ESPECIALIDAD
-- ============================================================

INSERT IGNORE INTO medico_especialidad (
    id_medico,
    id_especialidad
)
SELECT
    m.id_medico,
    e.id_especialidad

FROM tmp_medicos_demo d

INNER JOIN usuarios u
    ON u.correo = d.correo COLLATE utf8mb4_unicode_ci

INNER JOIN medicos m
    ON m.id_usuario = u.id_usuario

INNER JOIN especialidades e
    ON e.nombre = d.especialidad COLLATE utf8mb4_unicode_ci


-- ============================================================
-- 15. ASIGNAR HOSPITAL
-- ============================================================

INSERT IGNORE INTO medico_establecimiento (
    id_medico,
    id_establecimiento,
    estado
)
SELECT
    m.id_medico,
    est.id_establecimiento,
    TRUE

FROM tmp_medicos_demo d

INNER JOIN usuarios u
    ON u.correo = d.correo COLLATE utf8mb4_unicode_ci

INNER JOIN medicos m
    ON m.id_usuario = u.id_usuario

INNER JOIN (
    SELECT
        nombre,
        MIN(id_establecimiento) AS id_establecimiento
    FROM establecimientos
    GROUP BY nombre
) est
    ON est.nombre = d.hospital COLLATE utf8mb4_unicode_ci


-- ============================================================
-- 16. CREAR HORARIOS
-- ============================================================
--
-- Lunes a viernes
-- 08:00 - 16:00
-- Duración: 30 minutos
--
-- Esto permite que todos los médicos tengan disponibilidad
-- inmediata dentro del módulo Agendar cita.
--
-- ============================================================

INSERT INTO horarios_medicos (
    id_medico,
    id_establecimiento,
    dia_semana,
    hora_inicio,
    hora_fin,
    duracion_cita_minutos,
    estado
)

SELECT
    m.id_medico,
    est.id_establecimiento,
    dias.dia_semana,
    '08:00:00',
    '16:00:00',
    30,
    TRUE

FROM tmp_medicos_demo d

INNER JOIN usuarios u
    ON u.correo = d.correo COLLATE utf8mb4_unicode_ci

INNER JOIN medicos m
    ON m.id_usuario = u.id_usuario

INNER JOIN (
    SELECT
        nombre,
        MIN(id_establecimiento) AS id_establecimiento
    FROM establecimientos
    GROUP BY nombre
) est
    ON est.nombre = d.hospital COLLATE utf8mb4_unicode_ci

CROSS JOIN (
    SELECT 1 AS dia_semana
    UNION ALL SELECT 2
    UNION ALL SELECT 3
    UNION ALL SELECT 4
    UNION ALL SELECT 5
) dias

WHERE NOT EXISTS (

    SELECT 1

    FROM horarios_medicos hm

    WHERE hm.id_medico =
            m.id_medico

      AND hm.id_establecimiento =
            est.id_establecimiento

      AND hm.dia_semana =
            dias.dia_semana

      AND hm.hora_inicio =
            '08:00:00'

      AND hm.hora_fin =
            '16:00:00'
);


-- ============================================================
-- 17. VERIFICACIÓN - HOSPITALES Y CANTIDAD DE MÉDICOS
-- ============================================================

SELECT
    est.ciudad,
    est.nombre AS hospital,
    COUNT(
        DISTINCT me.id_medico
    ) AS total_medicos

FROM establecimientos est

LEFT JOIN medico_establecimiento me
    ON me.id_establecimiento =
       est.id_establecimiento

WHERE est.nombre IN (

    'Hospital de Especialidades Carlos Andrade Marín (HCAM)',

    'Hospital del IESS Quito Sur',

    'Hospital San Francisco de Quito',

    'Hospital del IESS Los Ceibos',

    'Hospital de Especialidades Teodoro Maldonado Carbo',

    'IESS Martha de Roldós'
)

GROUP BY
    est.id_establecimiento,
    est.ciudad,
    est.nombre

ORDER BY
    est.ciudad DESC,
    est.nombre;


-- ============================================================
-- 18. VERIFICACIÓN - MÉDICOS Y ESPECIALIDADES
-- ============================================================

SELECT
    est.ciudad,

    est.nombre AS hospital,

    CONCAT(
        u.nombres,
        ' ',
        u.apellidos
    ) AS medico,

    esp.nombre AS especialidad

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

ORDER BY
    est.ciudad,
    est.nombre,
    esp.nombre;


-- ============================================================
-- 19. VERIFICACIÓN - HORARIOS
-- ============================================================

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


-- ============================================================
-- 20. LIMPIEZA TEMPORAL
-- ============================================================

DROP TEMPORARY TABLE IF EXISTS tmp_medicos_demo;