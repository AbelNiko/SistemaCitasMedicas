-- ============================================================
--                 PROYECTO DE INGENIERÍA DE SOFTWARE II
-- ============================================================
--
--   SISTEMA DE GESTIÓN Y AGENDAMIENTO DE CITAS MÉDICAS
--
--   ARCHIVO:
--   03_consultas_prueba.sql
--
--   DESCRIPCIÓN:
--   Script destinado a comprobar el correcto funcionamiento
--   de la estructura y los datos almacenados en la base de
--   datos durante el desarrollo del sistema.
--
--   CONTENIDO:
--   • Verificación de la base de datos activa.
--   • Visualización de las tablas existentes.
--   • Verificación de la estructura de las tablas.
--   • Consulta de los datos registrados.
--
-- ============================================================


-- ============================================================
--              1. SELECCIÓN DE LA BASE DE DATOS
-- ============================================================

USE sistema_citas_medicas;


-- ============================================================
--             2. VERIFICAR BASE DE DATOS ACTIVA
-- ============================================================
--
-- RESULTADO ESPERADO:
-- sistema_citas_medicas
--
-- ============================================================

SELECT DATABASE() AS base_datos_activa;


-- ============================================================
--                3. VISUALIZAR LAS TABLAS
-- ============================================================
--
-- PROPÓSITO:
-- Mostrar todas las tablas creadas actualmente dentro de
-- la base de datos.
--
-- ============================================================

SHOW TABLES;


-- ============================================================
--            4. VERIFICAR ESTRUCTURA DE ROLES
-- ============================================================
--
-- PROPÓSITO:
-- Comprobar los campos, tipos de datos y restricciones
-- definidos para la tabla roles.
--
-- ============================================================

DESCRIBE roles;


-- ============================================================
--                 5. CONSULTAR LOS ROLES
-- ============================================================
--
-- PROPÓSITO:
-- Verificar que los roles iniciales hayan sido almacenados
-- correctamente.
--
-- ============================================================

SELECT
    id_rol,
    nombre,
    descripcion,
    estado,
    fecha_creacion
FROM roles
ORDER BY id_rol;


-- ============================================================
--                 6. CONTAR ROLES ACTIVOS
-- ============================================================
--
-- RESULTADO ESPERADO:
-- 5 roles activos.
--
-- ============================================================

SELECT
    COUNT(*) AS total_roles_activos
FROM roles
WHERE estado = TRUE;

-- ============================================================
--             7. VERIFICAR ESTRUCTURA DE USUARIOS
-- ============================================================

DESCRIBE usuarios;


-- ============================================================
--             8. VERIFICAR RELACIÓN USUARIO - ROL
-- ============================================================

SHOW CREATE TABLE usuarios;

-- ============================================================
--             9. VERIFICAR TABLA PACIENTES
-- ============================================================

DESCRIBE pacientes;


-- ============================================================
--              10. VERIFICAR TABLA MEDICOS
-- ============================================================

DESCRIBE medicos;


-- ============================================================
--      11. VERIFICAR TABLA PERSONAL ADMINISTRATIVO
-- ============================================================

DESCRIBE personal_administrativo;


-- ============================================================
--        12. VERIFICAR RELACIONES CON USUARIOS
-- ============================================================

SHOW CREATE TABLE pacientes;

SHOW CREATE TABLE medicos;

SHOW CREATE TABLE personal_administrativo;

-- ============================================================
--          13. VERIFICAR TABLA ESPECIALIDADES
-- ============================================================

DESCRIBE especialidades;


-- ============================================================
--       14. VERIFICAR TABLA MEDICO_ESPECIALIDAD
-- ============================================================

DESCRIBE medico_especialidad;


-- ============================================================
--        15. VERIFICAR TABLA ESTABLECIMIENTOS
-- ============================================================

DESCRIBE establecimientos;


-- ============================================================
--     16. VERIFICAR TABLA MEDICO_ESTABLECIMIENTO
-- ============================================================

DESCRIBE medico_establecimiento;


-- ============================================================
--              17. CONSULTAR ESPECIALIDADES
-- ============================================================

SELECT
    id_especialidad,
    nombre,
    descripcion,
    estado
FROM especialidades
ORDER BY nombre;


-- ============================================================
--             18. CONSULTAR ESTABLECIMIENTOS
-- ============================================================

SELECT
    id_establecimiento,
    nombre,
    direccion,
    telefono,
    ciudad,
    estado
FROM establecimientos
ORDER BY nombre;


-- ============================================================
--      19. VERIFICAR RELACIONES MUCHOS-A-MUCHOS
-- ============================================================

SHOW CREATE TABLE medico_especialidad;

SHOW CREATE TABLE medico_establecimiento;

-- ============================================================
--          20. VERIFICAR TABLA HORARIOS_MEDICOS
-- ============================================================

DESCRIBE horarios_medicos;


-- ============================================================
--               21. VERIFICAR TABLA CITAS
-- ============================================================

DESCRIBE citas;


-- ============================================================
--       22. VERIFICAR RELACIONES DE HORARIOS
-- ============================================================

SHOW CREATE TABLE horarios_medicos;


-- ============================================================
--         23. VERIFICAR RELACIONES DE CITAS
-- ============================================================

SHOW CREATE TABLE citas;


-- ============================================================
--                 24. VERIFICAR ÍNDICES
-- ============================================================

SHOW INDEX FROM horarios_medicos;

SHOW INDEX FROM citas;

-- ============================================================
--               25. CONSULTAR USUARIOS Y ROLES
-- ============================================================

SELECT
    u.id_usuario,
    u.nombres,
    u.apellidos,
    u.cedula,
    u.correo,
    r.nombre AS rol,
    u.estado
FROM usuarios u

INNER JOIN roles r
    ON r.id_rol = u.id_rol

ORDER BY u.id_usuario;


-- ============================================================
--               26. CONSULTAR MÉDICOS
-- ============================================================

SELECT
    m.id_medico,
    u.nombres,
    u.apellidos,
    m.cedula_profesional,
    m.estado
FROM medicos m

INNER JOIN usuarios u
    ON u.id_usuario = m.id_usuario;


-- ============================================================
--          27. MÉDICOS CON SUS ESPECIALIDADES
-- ============================================================

SELECT
    m.id_medico,
    CONCAT(u.nombres, ' ', u.apellidos) AS medico,
    e.nombre AS especialidad
FROM medico_especialidad me

INNER JOIN medicos m
    ON m.id_medico = me.id_medico

INNER JOIN usuarios u
    ON u.id_usuario = m.id_usuario

INNER JOIN especialidades e
    ON e.id_especialidad = me.id_especialidad;


-- ============================================================
--             28. HORARIOS DE LOS MÉDICOS
-- ============================================================

SELECT
    CONCAT(u.nombres, ' ', u.apellidos) AS medico,

    CASE hm.dia_semana
        WHEN 1 THEN 'Lunes'
        WHEN 2 THEN 'Martes'
        WHEN 3 THEN 'Miércoles'
        WHEN 4 THEN 'Jueves'
        WHEN 5 THEN 'Viernes'
        WHEN 6 THEN 'Sábado'
        WHEN 7 THEN 'Domingo'
    END AS dia,

    hm.hora_inicio,
    hm.hora_fin,
    hm.duracion_cita_minutos,
    est.nombre AS establecimiento

FROM horarios_medicos hm

INNER JOIN medicos m
    ON m.id_medico = hm.id_medico

INNER JOIN usuarios u
    ON u.id_usuario = m.id_usuario

INNER JOIN establecimientos est
    ON est.id_establecimiento = hm.id_establecimiento;


-- ============================================================
--               29. CONSULTAR CITAS COMPLETAS
-- ============================================================

SELECT
    c.id_cita,

    CONCAT(
        up.nombres,
        ' ',
        up.apellidos
    ) AS paciente,

    CONCAT(
        um.nombres,
        ' ',
        um.apellidos
    ) AS medico,

    e.nombre AS especialidad,

    est.nombre AS establecimiento,

    c.fecha_cita,

    c.hora_inicio,

    c.hora_fin,

    c.estado,

    c.motivo_consulta

FROM citas c

INNER JOIN pacientes p
    ON p.id_paciente = c.id_paciente

INNER JOIN usuarios up
    ON up.id_usuario = p.id_usuario

INNER JOIN medicos m
    ON m.id_medico = c.id_medico

INNER JOIN usuarios um
    ON um.id_usuario = m.id_usuario

INNER JOIN especialidades e
    ON e.id_especialidad = c.id_especialidad

INNER JOIN establecimientos est
    ON est.id_establecimiento = c.id_establecimiento

ORDER BY
    c.fecha_cita,
    c.hora_inicio;


-- ============================================================
--             30. VERIFICAR TABLA BITÁCORA
-- ============================================================

DESCRIBE bitacora_cambios;

-- ============================================================
--                  FIN DEL ARCHIVO 03
-- ============================================================
