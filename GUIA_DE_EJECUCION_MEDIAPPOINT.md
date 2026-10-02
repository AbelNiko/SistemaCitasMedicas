# MediAppoint
## Sistema de Gestión y Agendamiento de Citas Médicas

MediAppoint es una aplicación de escritorio desarrollada como proyecto académico para la materia **Ingeniería de Software II**. Su objetivo es demostrar el funcionamiento de un sistema de gestión y agendamiento de citas médicas para establecimientos de salud públicos del Ecuador.

---

## Requisitos

- Windows 10 u 11.
- Conexión a Internet.
- No es necesario instalar Java si se utiliza la versión empaquetada de MediAppoint.
- Mantener completa la carpeta de la aplicación. No ejecutar ni copiar únicamente `MediAppoint.exe`, ya que el programa también utiliza las carpetas `app` y `runtime`.

---

## Ejecución

1. Abrir la carpeta `MediAppoint`.
2. Ejecutar `MediAppoint.exe`.
3. Esperar a que aparezca la pantalla de inicio de sesión.
4. Ingresar con una de las cuentas de demostración.
5. Mantener conexión a Internet durante el uso de la aplicación, ya que la base de datos se encuentra alojada remotamente.

---

## Cuentas de demostración

### Paciente

**Correo:** `registro.paciente@prueba.local`
**Contraseña:** `Paciente123*`

Con esta cuenta se pueden probar las funciones del portal del paciente.

### Médico

**Correo:** `carlos.mendoza@prueba.local`
**Contraseña:** `Medico123*`

Carlos Mendoza se encuentra configurado como médico de **Medicina Interna** en el **Hospital de Especialidades Carlos Andrade Marín (HCAM)**.

> Las cuentas y datos utilizados en la demostración son ficticios y se usan únicamente con fines académicos.

---

## Funcionalidades para probar

### Portal del paciente

- Iniciar y cerrar sesión.
- Registrar una nueva cuenta de paciente.
- Recuperar la contraseña mediante pregunta de seguridad.
- Consultar el panel principal.
- Agendar una cita médica.
- Seleccionar especialidad, establecimiento, médico, fecha y horario.
- Consultar citas programadas.
- Reprogramar citas.
- Cancelar citas.
- Consultar historial de atenciones.
- Consultar y actualizar información del perfil.

### Portal del médico

- Iniciar y cerrar sesión.
- Consultar la agenda médica.
- Consultar citas asignadas.
- Confirmar citas.
- Registrar una atención.
- Registrar observaciones.
- Marcar una cita como atendida o no asistida según corresponda.
- Consultar el historial de atenciones.
- Consultar el perfil profesional.

---

## Flujo recomendado para la demostración

Para demostrar la integración entre el portal del paciente y el portal del médico se recomienda realizar el siguiente recorrido:

1. Iniciar sesión como paciente.
2. Seleccionar **Agendar cita**.
3. Elegir la especialidad **Medicina Interna**.
4. Elegir el **Hospital de Especialidades Carlos Andrade Marín (HCAM)**.
5. Seleccionar al médico **Carlos Mendoza**.
6. Elegir una fecha y un horario disponible.
7. Ingresar el motivo de la consulta.
8. Confirmar la cita.
9. Cerrar sesión.
10. Iniciar sesión con la cuenta de **Carlos Mendoza**.
11. Abrir la agenda médica.
12. Comprobar que la cita creada por el paciente se encuentre disponible para su gestión.

Este recorrido permite demostrar el flujo completo:

`Paciente → Agendamiento → Base de datos → Agenda del médico → Gestión de la atención`

---

## Establecimientos disponibles

### Quito

- Hospital de Especialidades Carlos Andrade Marín (HCAM).
- Hospital del IESS Quito Sur.
- Hospital San Francisco de Quito.

### Guayaquil

- Hospital del IESS Los Ceibos.
- Hospital de Especialidades Teodoro Maldonado Carbo.
- IESS Martha de Roldós.

Cada establecimiento dispone de médicos de diferentes especialidades para las pruebas de agendamiento.

---

## Especialidades disponibles

- Cardiología.
- Ginecología y Obstetricia.
- Medicina Interna.
- Pediatría.
- Traumatología.

---

## Consideraciones

- La aplicación utiliza una base de datos MySQL remota, por lo que necesita conexión a Internet.
- Los tiempos de respuesta pueden variar según la conexión disponible.
- No se deben modificar ni eliminar los archivos incluidos dentro de la carpeta empaquetada de MediAppoint.
- Los datos de pacientes, médicos y establecimientos utilizados para la demostración tienen fines exclusivamente académicos.
- MediAppoint no reemplaza ningún sistema institucional del IESS ni de otra entidad pública de salud.

---

## Proyecto académico

**Universidad:** Universidad de Especialidades Espíritu Santo (UEES)
**Materia:** Ingeniería de Software II
**Proyecto:** Sistema de Gestión y Agendamiento de Citas Médicas
