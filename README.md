# 🎓 EduConnect - Sistema de Registro de Incidencias Escolares

**EduConnect** es una solución móvil diseñada para instituciones educativas que desean gestionar, registrar y dar seguimiento a incidencias escolares de manera **eficiente, digital y en tiempo real**.

![Status](https://img.shields.io/badge/estado-en%20desarrollo%20avanzado-yellow)
![Firebase](https://img.shields.io/badge/Firebase-integrado-orange)
![Android](https://img.shields.io/badge/Plataforma-Android-blue)
![License](https://img.shields.io/badge/Licencia-Privada-lightgrey)

---

## 🚀 Funcionalidades destacadas

### 📝 Registro de incidencias
- Registro detallado de comportamientos y situaciones escolares.
- Asociación con estudiantes específicos mediante nombres y grado/sección.
- Categorización por:
  - Tipo: `Positiva` (elogio) o `Negativa` (falta).
  - Estado de avance: `Pendiente` → `Revisado` → `Notificado` → `Citado` → `Completado`.
  - Gravedad (solo para negativas).
- Fecha y hora automáticas, asignación del profesor responsable.
- Adjuntos: evidencia fotográfica vía **Firebase Storage**.

### 🧑‍🏫 Gestión docente y tutoría
- **Docentes** pueden registrar incidencias directamente desde su dispositivo.
- **Tutores** visualizan y gestionan incidencias de sus estudiantes asignados.
- Revisión y retroalimentación de casos desde una interfaz intuitiva con pestañas:
  - `Todos`, `Pendientes`, `Revisados`.

### 📅 Citas con apoderados
- Solicitud y programación de reuniones con responsables familiares.
- Registro de:
  - Nombre del apoderado.
  - Fecha y hora de la cita.
  - Fecha de creación del caso.

### 📄 Informes institucionales
- Generación de informes oficiales post-cita o post-revisión.
- Campos incluidos:
  - Detalles del caso.
  - Relación familiar.
  - Fecha de emisión.

### 👥 Gestión de estudiantes
- Lista completa de estudiantes con datos personales:
  - Nombres, apellidos, celular de apoderado, grado, sección.
- Seguimiento individual: historial de incidencias y estado general.

### 🔐 Seguridad y autenticación
- Inicio de sesión mediante:
  - Correo institucional (Firebase Auth).
  - Google Sign-In (OAuth 2.0).
- Diferenciación de roles: `Profesor`, `Tutor`, `Administrador`.

---

## 🔧 Tecnologías utilizadas

| Tecnología        | Uso principal                                |
|-------------------|-----------------------------------------------|
| **Kotlin**        | Lógica y desarrollo nativo de Android         |
| **ViewBinding**   | Vinculación segura con componentes XML        |
| **Firebase Auth** | Autenticación y gestión de usuarios           |
| **Cloud Firestore** | Base de datos en tiempo real                |
| **Firebase Storage** | Almacenamiento de imágenes y evidencias   |
| **Crashlytics & Analytics** | Monitoreo de fallos y métricas     |
| **Glide**         | Carga eficiente de imágenes                   |
| **iTextPDF** / **Jsoup** | Generación de informes PDF             |

---

## 🏫 Aplicación en contexto escolar

Este sistema está siendo implementado en un **colegio de nivel secundario**, donde:

- 📌 **Docentes** registran incidencias conforme ocurren en aula.
- 🧑‍🏫 **Tutores** revisan solo incidencias de su sección y contactan con apoderados.
- 📊 **Directivos** pueden analizar datos y generar reportes para la mejora institucional.

---

## 🗃️ Estructura de base de datos (Firestore)

### 🔹 Colección: `Estudiante`
- `idEstudiante`, `nombres`, `apellidos`, `grado`, `seccion`, `celularApoderado`, `cantidadIncidencias`

### 🔹 Colección: `Incidencia`
- `tipo`, `estado`, `atencion`, `fecha`, `hora`, `grado`, `seccion`, `urlImagen`, `nombreEstudiante`, `nombreProfesor`, `idProfesor`, etc.

### 🔹 Colección: `Cita`
- `apoderado`, `fechaCita`, `hora`, `createFecha`, `idCita`

### 🔹 Colección: `Informe`
- `detalles`, `apoderado`, `relacionFamiliar`, `createFecha`, `idInforme`

### 🔹 Colección: `Profesor`
- `idProfesor`, `nombres`, `apellidos`, `correo`, `celular`, `cargo`, `grado`, `seccion`, `dni`, `password`

---

## 🖼️ Capturas de pantalla

> Las capturas reales se encuentran disponibles en la documentación interna de QA.

---

## 📌 Estado actual del proyecto

> 🟡 **En desarrollo avanzado, listo para pruebas en producción real.**  
> Actualmente en etapa de evaluación institucional para su despliegue completo.

---

## 👥 Equipo de desarrollo

| Nombre         | Rol                    |
|----------------|-------------------------|
| [Karol]     | Operador  |
| [Melisa]     | Desarrollador      |
| [Sebastian]     | Operador |
| [Adrian]     | Desarrollador      |

