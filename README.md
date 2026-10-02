# MediAppoint

**Sistema de Gestión y Agendamiento de Citas Médicas**

MediAppoint es una aplicación de escritorio desarrollada en Java para gestionar el proceso de agendamiento y seguimiento de citas médicas. El proyecto fue desarrollado con fines académicos para la materia **Ingeniería de Software II** y toma como contexto establecimientos públicos de salud del Ecuador.

La aplicación integra dos portales principales: **paciente** y **médico**, conectados a una base de datos MySQL remota. El paciente puede gestionar sus citas y el médico puede consultar su agenda, actualizar estados y registrar información de la atención.

---

## Características principales

### Paciente

- Registro de cuenta.
- Inicio y cierre de sesión.
- Recuperación de contraseña mediante pregunta de seguridad.
- Consulta del panel principal.
- Agendamiento de citas.
- Selección de especialidad, establecimiento, médico, fecha y horario.
- Consulta de citas.
- Reprogramación de citas.
- Cancelación de citas.
- Consulta del historial de atenciones.
- Consulta y actualización del perfil.

### Médico

- Inicio y cierre de sesión.
- Consulta de agenda.
- Confirmación de citas.
- Registro de atención.
- Registro de observaciones.
- Gestión de estados de la cita.
- Consulta del historial de atenciones.
- Consulta del perfil profesional.

---

## Tecnologías utilizadas

- **Java 21 LTS**
- **JavaFX 21**
- **Maven**
- **MySQL**
- **Aiven** para alojamiento remoto de la base de datos
- **HikariCP** para el pool de conexiones JDBC
- **BCrypt** para protección de contraseñas y respuestas de seguridad
- **FXML y CSS** para la interfaz gráfica
- **Git y GitHub** para control de versiones
- **Scrum y Jira** para organización del trabajo

---

## Arquitectura

MediAppoint utiliza una arquitectura por capas:

```text
JavaFX / FXML
      ↓
Controllers
      ↓
Services
      ↓
DAO
      ↓
HikariCP / JDBC
      ↓
MySQL
```

Esta separación permite mantener la lógica de interfaz, reglas de negocio y acceso a datos en componentes independientes.

---

## Seguridad

La aplicación incorpora:

- Contraseñas protegidas mediante BCrypt.
- Pregunta y respuesta de seguridad para recuperación de contraseña.
- Respuesta de seguridad almacenada mediante hash.
- Control de intentos de recuperación.
- Roles diferenciados para pacientes y médicos.
- Validación de datos tanto en la interfaz como en la capa de servicio.
- Cuenta de base de datos restringida para la ejecución de la aplicación.
- Configuración de conexión externa al código fuente.

> Las credenciales de administración de la base de datos no forman parte del proyecto ni del ejecutable distribuido.

---

## Base de datos

La base de datos administra, entre otras, las siguientes entidades:

- Roles.
- Usuarios.
- Pacientes.
- Médicos.
- Especialidades.
- Establecimientos.
- Relación médico–especialidad.
- Relación médico–establecimiento.
- Horarios médicos.
- Citas.
- Bitácora de cambios.

La versión de demostración utiliza seis establecimientos y cinco especialidades activas.

### Establecimientos

**Quito**

- Hospital de Especialidades Carlos Andrade Marín (HCAM).
- Hospital del IESS Quito Sur.
- Hospital San Francisco de Quito.

**Guayaquil**

- Hospital del IESS Los Ceibos.
- Hospital de Especialidades Teodoro Maldonado Carbo.
- IESS Martha de Roldós.

### Especialidades

- Cardiología.
- Ginecología y Obstetricia.
- Medicina Interna.
- Pediatría.
- Traumatología.

---

## Requisitos para ejecutar la versión empaquetada

- Windows 10 u 11.
- Conexión a Internet.

La versión generada con `jpackage` incluye su propio runtime de Java, por lo que no requiere una instalación independiente del JDK o JRE.

---

## Ejecución del programa

1. Mantener completa la carpeta `MediAppoint`.
2. Ejecutar `MediAppoint.exe`.
3. Esperar la pantalla de inicio de sesión.
4. Ingresar con una cuenta de demostración.

> No debe copiarse únicamente `MediAppoint.exe`, ya que el ejecutable necesita las carpetas `app` y `runtime` generadas durante el empaquetado.

---

## Cuentas de demostración

### Paciente

**Correo:** `registro.paciente@prueba.local`
**Contraseña:** `Paciente123*`

### Médico

**Correo:** `carlos.mendoza@prueba.local`
**Contraseña:** `Medico123*`

La cuenta médica de Carlos Mendoza está asociada a **Medicina Interna** en el **Hospital de Especialidades Carlos Andrade Marín (HCAM)**.

Estas credenciales corresponden exclusivamente al entorno académico de demostración y no deben reutilizarse en un entorno real.

---

## Flujo recomendado de prueba

```text
Paciente
   ↓
Agendar cita
   ↓
Medicina Interna
   ↓
Hospital de Especialidades Carlos Andrade Marín (HCAM)
   ↓
Carlos Mendoza
   ↓
Fecha y horario
   ↓
Confirmar cita
   ↓
Cerrar sesión
   ↓
Ingresar como Carlos Mendoza
   ↓
Consultar la nueva cita en la agenda médica
```

Este flujo permite comprobar la comunicación entre ambos portales y la persistencia de información en MySQL.

---

## Ejecución desde código fuente

### Requisitos de desarrollo

- JDK 21.
- Maven.
- Visual Studio Code u otro IDE compatible.
- Acceso autorizado a la base de datos.

Compilación:

```powershell
mvn clean compile
```

Ejecución:

```powershell
mvn javafx:run
```

Las credenciales y parámetros de conexión no deben escribirse directamente en el código fuente. MediAppoint obtiene la configuración desde un archivo externo o variables de entorno.

---

## Estructura general del proyecto

```text
SistemaCitasMedicas/
├── database/
│   └── scripts SQL
├── src/
│   └── main/
│       ├── java/
│       │   └── com/citasmedicas/
│       │       ├── app/
│       │       ├── controller/
│       │       ├── dao/
│       │       ├── model/
│       │       ├── service/
│       │       └── util/
│       └── resources/
│           ├── css/
│           └── fxml/
├── pom.xml
└── README.md
```

---

## Equipo de desarrollo

- Pablo Nicolás Álvarez Molestina
- José Moisés Arias Zavala
- Josselyne Michelle Calderón Plúa
- Anthony Jahir Sarmiento Ronquillo
- Fernando Andrés Vargas Jara
- Julio César Yépez Galán

**Docente:** Victoria de Lourdes García Velásquez
**Materia:** Ingeniería de Software II
**Universidad:** Universidad de Especialidades Espíritu Santo (UEES)

---

## Alcance académico

MediAppoint es una solución académica de demostración. No pretende reemplazar los sistemas institucionales del IESS ni de otras entidades de salud.

No incluye funcionalidades como:

- Facturación.
- Procesamiento de pagos.
- Historia clínica electrónica completa.
- Prescripción electrónica.
- Telemedicina.
- Diagnóstico automatizado.
- Interoperabilidad nacional entre instituciones sanitarias.

Los datos utilizados para las pruebas y demostraciones son ficticios.
