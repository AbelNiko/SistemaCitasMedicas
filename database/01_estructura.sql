-- ============================================================
--                 PROYECTO DE INGENIERÍA DE SOFTWARE II
-- ============================================================
--
--   SISTEMA DE GESTIÓN Y AGENDAMIENTO DE CITAS MÉDICAS
--
--   ARCHIVO:
--   01_estructura.sql
--
--   DESCRIPCIÓN:
--   Script encargado de crear la base de datos y establecer
--   la estructura principal necesaria para el funcionamiento
--   del Sistema de Gestión y Agendamiento de Citas Médicas.
--
--   CONTENIDO:
--   • Creación de la base de datos.
--   • Selección de la base de datos de trabajo.
--   • Creación de las tablas principales del sistema.
--   • Definición de claves primarias y restricciones.
--
--   NOTA:
--   Este archivo debe ejecutarse antes de cargar los datos
--   iniciales o realizar consultas de prueba.
--
-- ============================================================


-- ============================================================
--                1. CREACIÓN DE LA BASE DE DATOS
-- ============================================================
--
-- PROPÓSITO:
-- Crear la base de datos principal utilizada por el sistema.
--
-- CONFIGURACIÓN:
-- • Codificación   : utf8mb4
-- • Intercalación : utf8mb4_unicode_ci
--
-- utf8mb4 permite almacenar correctamente caracteres
-- especiales, tildes, símbolos y caracteres Unicode.
--
-- ============================================================

CREATE DATABASE IF NOT EXISTS sistema_citas_medicas
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;


-- ============================================================
--              2. SELECCIÓN DE LA BASE DE DATOS
-- ============================================================
--
-- PROPÓSITO:
-- Establecer sistema_citas_medicas como la base de datos
-- activa sobre la cual se ejecutarán las instrucciones
-- posteriores de este script.
--
-- ============================================================

USE sistema_citas_medicas;


-- ============================================================
--                       3. TABLA: ROLES
-- ============================================================
--
-- PROPÓSITO:
-- Almacenar y administrar los diferentes perfiles de acceso
-- disponibles dentro del sistema.
--
-- DESCRIPCIÓN:
-- Cada usuario tendrá asociado un rol que permitirá determinar
-- sus responsabilidades y las funcionalidades a las que podrá
-- acceder dentro de la aplicación.
--
-- EJEMPLOS DE ROLES:
-- • Paciente
-- • Médico
-- • Administrador
-- • Personal administrativo
-- • Coordinador médico
--
-- CAMPOS:
--
-- • id_rol
--   Identificador único generado automáticamente.
--
-- • nombre
--   Nombre del rol. No puede repetirse.
--
-- • descripcion
--   Explicación general de las funciones asociadas al rol.
--
-- • estado
--   Determina si el rol se encuentra activo.
--   TRUE  = Activo
--   FALSE = Inactivo
--
-- • fecha_creacion
--   Registra automáticamente la fecha y hora en que fue
--   creado el registro.
--
-- RESTRICCIONES:
-- • PRIMARY KEY : id_rol
-- • UNIQUE      : nombre
-- • NOT NULL    : nombre, estado y fecha_creacion
--
-- ============================================================

CREATE TABLE IF NOT EXISTS roles (
    id_rol INT AUTO_INCREMENT PRIMARY KEY,

    nombre VARCHAR(50) NOT NULL UNIQUE,

    descripcion VARCHAR(255),

    estado BOOLEAN NOT NULL DEFAULT TRUE,

    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
--                     4. TABLA: USUARIOS
-- ============================================================
--
-- PROPÓSITO:
-- Almacenar la información general y las credenciales de acceso
-- de los usuarios registrados en el sistema.
--
-- DESCRIPCIÓN:
-- Esta tabla centraliza los datos principales de autenticación
-- y permite relacionar cada usuario con un rol específico.
--
-- RELACIÓN:
-- Cada usuario debe pertenecer a un rol registrado previamente
-- en la tabla roles.
--
-- CAMPOS:
--
-- • id_usuario
--   Identificador único generado automáticamente.
--
-- • id_rol
--   Identificador del rol asignado al usuario.
--
-- • nombres
--   Nombres del usuario.
--
-- • apellidos
--   Apellidos del usuario.
--
-- • cedula
--   Número de identificación del usuario.
--   No puede repetirse.
--
-- • correo
--   Correo electrónico utilizado por el usuario.
--   No puede repetirse.
--
-- • password_hash
--   Almacena el hash de la contraseña.
--   Nunca debe almacenarse una contraseña en texto plano.
--
-- • telefono
--   Número telefónico de contacto.
--
-- • estado
--   Indica si el usuario se encuentra habilitado.
--
-- • fecha_creacion
--   Fecha y hora de creación del registro.
--
-- • fecha_actualizacion
--   Fecha y hora de la última modificación realizada.
--
-- RESTRICCIONES:
-- • PRIMARY KEY : id_usuario
-- • FOREIGN KEY : id_rol
-- • UNIQUE      : cedula
-- • UNIQUE      : correo
--
-- ============================================================

CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,

    id_rol INT NOT NULL,

    nombres VARCHAR(100) NOT NULL,

    apellidos VARCHAR(100) NOT NULL,

    cedula VARCHAR(10) NOT NULL UNIQUE,

    correo VARCHAR(150) NOT NULL UNIQUE,

    password_hash VARCHAR(255) NOT NULL,

    telefono VARCHAR(20),

pregunta_seguridad VARCHAR(50),

respuesta_seguridad_hash VARCHAR(255),

intentos_recuperacion TINYINT UNSIGNED
    NOT NULL DEFAULT 0,

bloqueado_recuperacion_hasta DATETIME,

estado BOOLEAN NOT NULL DEFAULT TRUE,

    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    fecha_actualizacion TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_usuario_rol
        FOREIGN KEY (id_rol)
        REFERENCES roles(id_rol),

    INDEX idx_usuarios_apellidos (apellidos)
);

-- ============================================================
--                     5. TABLA: PACIENTES
-- ============================================================
--
-- PROPÓSITO:
-- Almacenar la información específica de los pacientes
-- registrados dentro del sistema.
--
-- DESCRIPCIÓN:
-- Cada paciente debe estar relacionado con un usuario existente.
-- La información general como nombres, apellidos, cédula,
-- correo y contraseña se almacena en la tabla usuarios.
--
-- RELACIÓN:
-- usuarios 1 ----- 1 pacientes
--
-- CAMPOS:
--
-- • id_paciente
--   Identificador único del paciente.
--
-- • id_usuario
--   Relaciona al paciente con su cuenta de usuario.
--
-- • fecha_nacimiento
--   Fecha de nacimiento del paciente.
--
-- • direccion
--   Dirección domiciliaria.
--
-- • sexo
--   Sexo registrado del paciente.
--
-- • tipo_sangre
--   Tipo de sangre declarado por el paciente.
--
-- • alergias
--   Alergias declaradas por el paciente.
--
-- • condiciones_medicas
--   Condiciones médicas relevantes declaradas por el paciente.
--
-- • contacto_emergencia
--
-- • telefono_emergencia
--   Número telefónico del contacto de emergencia.
--
-- • fecha_creacion
--   Fecha y hora de creación del registro.
--
-- RESTRICCIONES:
-- • PRIMARY KEY : id_paciente
-- • FOREIGN KEY : id_usuario
-- • UNIQUE      : id_usuario
--
-- ============================================================

CREATE TABLE IF NOT EXISTS pacientes (
    id_paciente INT AUTO_INCREMENT PRIMARY KEY,

    id_usuario INT NOT NULL UNIQUE,

    fecha_nacimiento DATE,

    direccion VARCHAR(255),

    sexo VARCHAR(20),

    tipo_sangre VARCHAR(15),

    alergias VARCHAR(500),

    condiciones_medicas VARCHAR(500),

    contacto_emergencia VARCHAR(150),

    telefono_emergencia VARCHAR(20),

    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_paciente_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id_usuario)
);

-- ============================================================
--                      6. TABLA: MEDICOS
-- ============================================================
--
-- PROPÓSITO:
-- Almacenar la información profesional específica de los
-- médicos registrados en el sistema.
--
-- DESCRIPCIÓN:
-- Los datos generales del médico se almacenan en usuarios,
-- mientras que esta tabla conserva información relacionada
-- con su perfil profesional.
--
-- RELACIÓN:
-- usuarios 1 ----- 1 medicos
--
-- CAMPOS:
--
-- • id_medico
--   Identificador único del médico.
--
-- • id_usuario
--   Relaciona al médico con su cuenta de usuario.
--
-- • cedula_profesional
--   Identificador o registro profesional del médico.
--
-- • observacion
--   Información adicional relacionada con el profesional.
--
-- • estado
--   Determina si el médico se encuentra habilitado.
--
-- • fecha_creacion
--   Fecha y hora de creación del registro.
--
-- RESTRICCIONES:
-- • PRIMARY KEY : id_medico
-- • FOREIGN KEY : id_usuario
-- • UNIQUE      : id_usuario
-- • UNIQUE      : cedula_profesional
--
-- ============================================================

CREATE TABLE IF NOT EXISTS medicos (
    id_medico INT AUTO_INCREMENT PRIMARY KEY,

    id_usuario INT NOT NULL UNIQUE,

    cedula_profesional VARCHAR(50) NOT NULL UNIQUE,

    observacion VARCHAR(255),

    estado BOOLEAN NOT NULL DEFAULT TRUE,

    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_medico_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id_usuario)
);

-- ============================================================
--             7. TABLA: PERSONAL_ADMINISTRATIVO
-- ============================================================
--
-- PROPÓSITO:
-- Almacenar información específica del personal encargado
-- de apoyar los procesos administrativos del sistema.
--
-- DESCRIPCIÓN:
-- Los datos personales y de autenticación permanecen en la
-- tabla usuarios. Aquí se conserva únicamente información
-- asociada con sus funciones administrativas.
--
-- RELACIÓN:
-- usuarios 1 ----- 1 personal_administrativo
--
-- CAMPOS:
--
-- • id_personal
--   Identificador único del personal administrativo.
--
-- • id_usuario
--   Relaciona al personal con su cuenta de usuario.
--
-- • cargo
--   Cargo o función desempeñada.
--
-- • estado
--   Determina si el registro se encuentra habilitado.
--
-- • fecha_creacion
--   Fecha y hora de creación del registro.
--
-- RESTRICCIONES:
-- • PRIMARY KEY : id_personal
-- • FOREIGN KEY : id_usuario
-- • UNIQUE      : id_usuario
--
-- ============================================================

CREATE TABLE IF NOT EXISTS personal_administrativo (
    id_personal INT AUTO_INCREMENT PRIMARY KEY,

    id_usuario INT NOT NULL UNIQUE,

    cargo VARCHAR(100),

    estado BOOLEAN NOT NULL DEFAULT TRUE,

    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_personal_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id_usuario)
);

-- ============================================================
--                  8. TABLA: ESPECIALIDADES
-- ============================================================
--
-- PROPÓSITO:
-- Almacenar las diferentes especialidades médicas disponibles
-- dentro del sistema.
--
-- DESCRIPCIÓN:
-- Esta tabla funciona como un catálogo de especialidades que
-- posteriormente podrán ser asignadas a uno o varios médicos.
--
-- EJEMPLOS:
-- • Medicina General
-- • Cardiología
-- • Pediatría
-- • Traumatología
-- • Dermatología
--
-- CAMPOS:
--
-- • id_especialidad
--   Identificador único de la especialidad.
--
-- • nombre
--   Nombre de la especialidad médica.
--   No puede repetirse.
--
-- • descripcion
--   Descripción general de la especialidad.
--
-- • estado
--   Determina si la especialidad está disponible.
--
-- • fecha_creacion
--   Fecha y hora de creación del registro.
--
-- RESTRICCIONES:
-- • PRIMARY KEY : id_especialidad
-- • UNIQUE      : nombre
--
-- ============================================================

CREATE TABLE IF NOT EXISTS especialidades (
    id_especialidad INT AUTO_INCREMENT PRIMARY KEY,

    nombre VARCHAR(100) NOT NULL UNIQUE,

    descripcion VARCHAR(255),

    estado BOOLEAN NOT NULL DEFAULT TRUE,

    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);


-- ============================================================
--              9. TABLA: MEDICO_ESPECIALIDAD
-- ============================================================
--
-- PROPÓSITO:
-- Relacionar a los médicos con las especialidades médicas
-- que pueden ejercer dentro del sistema.
--
-- DESCRIPCIÓN:
-- Esta tabla resuelve una relación de tipo muchos-a-muchos.
--
-- Un médico puede tener varias especialidades.
-- Una especialidad puede estar asignada a varios médicos.
--
-- RELACIÓN:
--
-- medicos N ----- M especialidades
--
-- CAMPOS:
--
-- • id_medico
--   Identificador del médico.
--
-- • id_especialidad
--   Identificador de la especialidad.
--
-- • fecha_asignacion
--   Fecha en la que se realizó la asignación.
--
-- RESTRICCIONES:
-- • PRIMARY KEY compuesta:
--   id_medico + id_especialidad
--
-- • FOREIGN KEY:
--   id_medico       -> medicos
--   id_especialidad -> especialidades
--
-- ============================================================

CREATE TABLE IF NOT EXISTS medico_especialidad (
    id_medico INT NOT NULL,

    id_especialidad INT NOT NULL,

    fecha_asignacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (
        id_medico,
        id_especialidad
    ),

    CONSTRAINT fk_medico_especialidad_medico
        FOREIGN KEY (id_medico)
        REFERENCES medicos(id_medico),

    CONSTRAINT fk_medico_especialidad_especialidad
        FOREIGN KEY (id_especialidad)
        REFERENCES especialidades(id_especialidad)
);


-- ============================================================
--                10. TABLA: ESTABLECIMIENTOS
-- ============================================================
--
-- PROPÓSITO:
-- Registrar los establecimientos de salud disponibles dentro
-- del sistema.
--
-- DESCRIPCIÓN:
-- Puede representar hospitales, centros de salud u otras
-- instituciones donde los médicos brindan atención.
--
-- CAMPOS:
--
-- • id_establecimiento
--   Identificador único del establecimiento.
--
-- • nombre
--   Nombre del establecimiento de salud.
--
-- • direccion
--   Dirección física del establecimiento.
--
-- • telefono
--   Número telefónico de contacto.
--
-- • ciudad
--   Ciudad donde se encuentra ubicado.
--
-- • estado
--   Determina si el establecimiento se encuentra habilitado.
--
-- • fecha_creacion
--   Fecha y hora de creación del registro.
--
-- ============================================================

CREATE TABLE IF NOT EXISTS establecimientos (
    id_establecimiento INT AUTO_INCREMENT PRIMARY KEY,

    nombre VARCHAR(150) NOT NULL,

    direccion VARCHAR(255) NOT NULL,

    telefono VARCHAR(20),

    ciudad VARCHAR(100) NOT NULL DEFAULT 'Guayaquil',

    estado BOOLEAN NOT NULL DEFAULT TRUE,

    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);


-- ============================================================
--           11. TABLA: MEDICO_ESTABLECIMIENTO
-- ============================================================
--
-- PROPÓSITO:
-- Relacionar los médicos con los establecimientos de salud
-- en los que se encuentran autorizados para atender.
--
-- DESCRIPCIÓN:
-- Un médico puede trabajar en varios establecimientos y un
-- establecimiento puede contar con varios médicos.
--
-- RELACIÓN:
--
-- medicos N ----- M establecimientos
--
-- CAMPOS:
--
-- • id_medico
--   Identificador del médico.
--
-- • id_establecimiento
--   Identificador del establecimiento.
--
-- • estado
--   Indica si la relación entre médico y establecimiento
--   se encuentra activa.
--
-- • fecha_asignacion
--   Fecha en que el médico fue asignado al establecimiento.
--
-- ============================================================

CREATE TABLE IF NOT EXISTS medico_establecimiento (
    id_medico INT NOT NULL,

    id_establecimiento INT NOT NULL,

    estado BOOLEAN NOT NULL DEFAULT TRUE,

    fecha_asignacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (
        id_medico,
        id_establecimiento
    ),

    CONSTRAINT fk_medico_establecimiento_medico
        FOREIGN KEY (id_medico)
        REFERENCES medicos(id_medico),

    CONSTRAINT fk_medico_establecimiento_establecimiento
        FOREIGN KEY (id_establecimiento)
        REFERENCES establecimientos(id_establecimiento)
);

-- ============================================================
--                12. TABLA: HORARIOS_MEDICOS
-- ============================================================
--
-- PROPÓSITO:
-- Definir los días y rangos de atención disponibles para
-- cada médico dentro de un establecimiento específico.
--
-- DESCRIPCIÓN:
-- Esta tabla permitirá conocer cuándo un médico se encuentra
-- disponible para atender citas.
--
-- EJEMPLO:
-- Médico: Juan Pérez
-- Día: Lunes
-- Hora inicio: 08:00
-- Hora fin: 12:00
-- Duración cita: 30 minutos
--
-- RELACIONES:
-- medicos 1 ----- N horarios_medicos
-- establecimientos 1 ----- N horarios_medicos
--
-- CAMPOS:
--
-- • id_horario
--   Identificador único del horario.
--
-- • id_medico
--   Médico propietario del horario.
--
-- • id_establecimiento
--   Establecimiento donde atenderá.
--
-- • dia_semana
--   Día de atención:
--   1 = Lunes
--   2 = Martes
--   3 = Miércoles
--   4 = Jueves
--   5 = Viernes
--   6 = Sábado
--   7 = Domingo
--
-- • hora_inicio
--   Hora en que comienza la atención.
--
-- • hora_fin
--   Hora en que finaliza la atención.
--
-- • duracion_cita_minutos
--   Duración estándar de cada cita.
--
-- • estado
--   Determina si el horario está habilitado.
--
-- • fecha_creacion
--   Fecha de creación del registro.
--
-- VALIDACIONES:
-- • El día debe estar entre 1 y 7.
-- • La hora final debe ser mayor que la inicial.
-- • La duración debe ser mayor que cero.
--
-- ============================================================

CREATE TABLE IF NOT EXISTS horarios_medicos (
    id_horario INT AUTO_INCREMENT PRIMARY KEY,

    id_medico INT NOT NULL,

    id_establecimiento INT NOT NULL,

    dia_semana TINYINT NOT NULL,

    hora_inicio TIME NOT NULL,

    hora_fin TIME NOT NULL,

    duracion_cita_minutos SMALLINT UNSIGNED NOT NULL DEFAULT 30,

    estado BOOLEAN NOT NULL DEFAULT TRUE,

    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_horario_dia_semana
        CHECK (dia_semana BETWEEN 1 AND 7),

    CONSTRAINT chk_horario_horas
        CHECK (hora_fin > hora_inicio),

    CONSTRAINT chk_horario_duracion
        CHECK (duracion_cita_minutos > 0),

    CONSTRAINT fk_horario_medico
        FOREIGN KEY (id_medico)
        REFERENCES medicos(id_medico),

    CONSTRAINT fk_horario_establecimiento
        FOREIGN KEY (id_establecimiento)
        REFERENCES establecimientos(id_establecimiento)
);


-- ============================================================
--                     13. TABLA: CITAS
-- ============================================================
--
-- PROPÓSITO:
-- Registrar y administrar las citas médicas programadas
-- dentro del sistema.
--
-- DESCRIPCIÓN:
-- Esta es una de las tablas principales del proyecto.
-- Cada cita relaciona un paciente con un médico, una
-- especialidad y un establecimiento.
--
-- RELACIONES:
--
-- pacientes         1 ----- N citas
-- medicos           1 ----- N citas
-- especialidades    1 ----- N citas
-- establecimientos  1 ----- N citas
--
-- CAMPOS:
--
-- • id_cita
--   Identificador único de la cita.
--
-- • id_paciente
--   Paciente que solicita la atención.
--
-- • id_medico
--   Médico encargado de atender la cita.
--
-- • id_especialidad
--   Especialidad relacionada con la consulta.
--
-- • id_establecimiento
--   Lugar donde se realizará la atención.
--
-- • fecha_cita
--   Fecha programada para la atención.
--
-- • hora_inicio
--   Hora inicial de la cita.
--
-- • hora_fin
--   Hora estimada de finalización.
--
-- • estado
--   Estado actual de la cita.
--
-- • motivo_consulta
--   Motivo general indicado por el paciente.
--
-- • observacion
--   Información adicional relacionada con la cita.
--
-- • motivo_cancelacion
--   Razón registrada en caso de cancelación.
--
-- • fecha_creacion
--   Fecha de creación de la cita.
--
-- • fecha_actualizacion
--   Fecha de la última modificación.
--
-- ESTADOS DISPONIBLES:
-- • PROGRAMADA
-- • CONFIRMADA
-- • ATENDIDA
-- • CANCELADA
-- • NO_ASISTIO
--
-- ============================================================

CREATE TABLE IF NOT EXISTS citas (
    id_cita INT AUTO_INCREMENT PRIMARY KEY,

    id_paciente INT NOT NULL,

    id_medico INT NOT NULL,

    id_especialidad INT NOT NULL,

    id_establecimiento INT NOT NULL,

    fecha_cita DATE NOT NULL,

    hora_inicio TIME NOT NULL,

    hora_fin TIME NOT NULL,

    estado ENUM(
        'PROGRAMADA',
        'CONFIRMADA',
        'ATENDIDA',
        'CANCELADA',
        'NO_ASISTIO'
    ) NOT NULL DEFAULT 'PROGRAMADA',

    motivo_consulta VARCHAR(255),

    observacion VARCHAR(500),

    motivo_cancelacion VARCHAR(255),

    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    fecha_actualizacion TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT chk_cita_horas
        CHECK (hora_fin > hora_inicio),

    CONSTRAINT fk_cita_paciente
        FOREIGN KEY (id_paciente)
        REFERENCES pacientes(id_paciente),

    CONSTRAINT fk_cita_medico
        FOREIGN KEY (id_medico)
        REFERENCES medicos(id_medico),

    CONSTRAINT fk_cita_especialidad
        FOREIGN KEY (id_especialidad)
        REFERENCES especialidades(id_especialidad),

    CONSTRAINT fk_cita_establecimiento
        FOREIGN KEY (id_establecimiento)
        REFERENCES establecimientos(id_establecimiento),

    CONSTRAINT uk_cita_medico_horario
        UNIQUE (
            id_medico,
            fecha_cita,
            hora_inicio
        ),

    INDEX idx_citas_fecha (fecha_cita),

    INDEX idx_citas_estado (estado)
);

-- ============================================================
--                14. TABLA: BITACORA_CAMBIOS
-- ============================================================
--
-- PROPÓSITO:
-- Registrar las acciones importantes realizadas por los
-- usuarios dentro del sistema.
--
-- DESCRIPCIÓN:
-- Esta tabla permitirá mantener un historial básico de
-- operaciones relevantes, facilitando la auditoría y el
-- seguimiento de cambios realizados en la aplicación.
--
-- EJEMPLOS DE ACCIONES:
-- • Creación de una cita.
-- • Modificación de una cita.
-- • Cancelación de una cita.
-- • Actualización de datos de un paciente.
-- • Activación o desactivación de usuarios.
--
-- RELACIÓN:
-- usuarios 1 ----- N bitacora_cambios
--
-- CAMPOS:
--
-- • id_bitacora
--   Identificador único del registro.
--
-- • id_usuario
--   Usuario responsable de la acción.
--
-- • accion
--   Tipo de operación realizada.
--
-- • tabla_afectada
--   Tabla sobre la cual se realizó la operación.
--
-- • id_registro
--   Identificador del registro afectado.
--
-- • descripcion
--   Información adicional sobre el cambio realizado.
--
-- • fecha
--   Fecha y hora en que ocurrió la acción.
--
-- ============================================================

CREATE TABLE IF NOT EXISTS bitacora_cambios (
    id_bitacora BIGINT AUTO_INCREMENT PRIMARY KEY,

    id_usuario INT,

    accion VARCHAR(100) NOT NULL,

    tabla_afectada VARCHAR(100),

    id_registro INT,

    descripcion VARCHAR(500),

    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_bitacora_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id_usuario)
        ON DELETE SET NULL,

    INDEX idx_bitacora_fecha (fecha)
);


-- ============================================================
--                  FIN DEL ARCHIVO 01
-- ============================================================