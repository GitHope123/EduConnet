# 🎓 EduConnect  
### Sistema de Registro de Incidencias Escolares

**EduConnect** es una solución móvil integral para instituciones educativas que buscan **gestionar, registrar y dar seguimiento a incidencias escolares** de forma **eficiente, digital y en tiempo real**.

<p align="center">
  <img src="https://img.shields.io/badge/estado-en%20desarrollo%20avanzado-yellow" />
  <img src="https://img.shields.io/badge/Firebase-integrado-orange" />
  <img src="https://img.shields.io/badge/Plataforma-Android-blue" />
  <img src="https://img.shields.io/badge/Licencia-Privada-lightgrey" />
</p>

---

## 🚀 Funcionalidades destacadas

### 📝 Registro de incidencias
- 📌 Registro detallado de comportamientos escolares.
- 👦 Asociación con estudiantes por nombre, grado y sección.
- 🏷️ Categorización por:
  - Tipo: `✅ Positiva` | `⚠️ Negativa`
  - Estado: `Pendiente` → `Revisado` → `Notificado` → `Citado` → `Completado`
  - Gravedad: (en casos negativos)
- 🕓 Fecha y hora automáticas.
- 👨‍🏫 Asignación del profesor responsable.
- 📷 Evidencia fotográfica integrada (Firebase Storage).

### 🧑‍🏫 Gestión docente y tutoría
- 📲 Docentes registran incidencias desde su dispositivo móvil.
- 👩‍🏫 Tutores gestionan incidencias de sus estudiantes asignados.
- 📂 Visualización por pestañas: `Todos`, `Pendientes`, `Revisados`
- 💬 Retroalimentación y seguimiento en tiempo real.

### 📅 Gestión de citas con apoderados
- 🗓️ Programación de reuniones desde la app.
- Campos registrados:
  - 👤 Nombre del apoderado
  - 🕒 Fecha y hora de la cita
  - 🧾 Fecha de creación del caso

### 📄 Informes institucionales
- 📑 Generación automática de informes PDF.
- Contenido del informe:
  - 🧩 Detalles del caso
  - 🧬 Relación familiar
  - 📆 Fecha de emisión

### 👥 Gestión de estudiantes
- 📋 Visualización de datos personales:
  - Nombre completo, grado, sección, celular del apoderado.
- 📈 Historial individual de incidencias.

### 🔐 Seguridad y autenticación
- 🔑 Login por correo y contraseña (Firebase Auth).
- 🎭 Roles diferenciados:
  - `👨‍🏫 Profesor`, `🧑‍🏫 Tutor`, `🏛️ Administrador`

---

## 🔧 Tecnologías utilizadas

| 🔧 Tecnología        | ⚙️ Uso principal                          |
|----------------------|-------------------------------------------|
| **Kotlin**           | Lógica y desarrollo Android               |
| **ViewBinding**      | Vinculación segura con layouts XML        |
| **Firebase Auth**    | Sistema de autenticación                  |
| **Cloud Firestore**  | Base de datos NoSQL en tiempo real        |
| **Firebase Storage** | Almacenamiento de imágenes                |
| **Crashlytics**      | Monitoreo de errores                      |
| **Analytics**        | Análisis de uso y métricas                |
| **Glide**            | Carga optimizada de imágenes              |
| **iTextPDF / Jsoup** | Generación de informes PDF                |

---

## 🏫 Aplicación en contexto escolar

> Actualmente en uso en una institución de **educación secundaria**, donde:

- 🧑‍🏫 **Docentes** registran incidencias desde aula.
- 👨‍🏫 **Tutores** acceden a incidencias filtradas por sección.
- 📊 **Directivos** generan reportes para gestión institucional.

---

## 🗃️ Estructura de base de datos (Firestore)

📁 Estudiante
 ┣ idEstudiante
 ┣ nombres, apellidos
 ┣ grado, sección
 ┣ celularApoderado
 ┗ cantidadIncidencias

📁 Incidencia
 ┣ tipo, estado, atencion
 ┣ fecha, hora
 ┣ grado, sección
 ┣ nombreEstudiante, nombreProfesor
 ┣ urlImagen, idProfesor
 ┗ ...

📁 Cita
 ┣ apoderado
 ┣ fechaCita, hora
 ┣ createFecha
 ┗ idCita

📁 Informe
 ┣ detalles, apoderado
 ┣ relacionFamiliar
 ┣ createFecha
 ┗ idInforme

📁 Profesor
 ┣ idProfesor, nombres, apellidos
 ┣ correo, celular, cargo
 ┣ grado, sección, dni, password
 ┗ ...
 
## 📌 Estado actual del proyecto

> 🟡 **En desarrollo avanzado, listo para pruebas en producción real.**  
> Actualmente en etapa de evaluación por los operadores para su despliegue completo.

---

## 👥 Equipo de desarrollo

| Nombre         | Rol                    |
|----------------|-------------------------|
| [Karol]     | Operador  |
| [Melisa]     | Desarrollador      |
| [Sebastian]     | Operador |
| [Adrian]     | Desarrollador      |

