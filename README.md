# 🎓 EduConnect  
## Sistema Integral de Registro y Seguimiento de Incidencias Escolares

**EduConnect** es una aplicación móvil diseñada para instituciones educativas que desean modernizar la gestión de incidencias escolares. Permite registrar, monitorear y notificar comportamientos estudiantiles de forma **rápida, digital y en tiempo real**, facilitando el trabajo colaborativo entre docentes, tutores y directivos.

<p align="center">
  <img src="https://img.shields.io/badge/estado-en%20desarrollo%20avanzado-yellow"  />
  <img src="https://img.shields.io/badge/Firebase-integrado-orange"  />
  <img src="https://img.shields.io/badge/Plataforma-Android-blue"  />
  <img src="https://img.shields.io/badge/Licencia-Privada-lightgrey"  />
</p>

---

## 🚀 Funcionalidades Principales

### 📝 Registro Completo de Incidencias
- ✅ Descripción detallada de cada caso.
- 👦 Relación con estudiantes por nombre, grado y sección.
- 🏷️ Clasificación avanzada:
  - **Tipo:** `Positiva` | `Negativa`
  - **Estado:** `Pendiente` → `Revisado` → `Notificado` → `Citado` → `Completado`
  - **Gravedad:** (en casos negativos)
- 🕓 Fecha y hora automáticas.
- 👨‍🏫 Asignación de responsable docente.
- 📷 Carga de evidencia fotográfica mediante Firebase Storage.

### 🧑‍🏫 Gestión Docente y Tutoría
- 📲 Registro desde dispositivos móviles.
- 👩‍🏫 Acceso personalizado para tutores según grupo asignado.
- 📂 Filtros visuales: `Todas`, `Pendientes`, `Resueltas`.
- 💬 Actualización en tiempo real del estado y comentarios.

### 🗓️ Programación de Citas con Apoderados
- 📆 Agenda integrada para coordinar reuniones.
- Campos registrados:
  - 👤 Nombre del apoderado
  - 🕒 Fecha y hora de la cita
  - 🧾 Fecha de creación del caso

### 📄 Generación Automática de Informes PDF
- 📑 Exportación de informes completos en formato PDF.
- Contenido incluido:
  - 🧩 Detalles del caso
  - 🧬 Información familiar
  - 📆 Fecha de emisión

### 👥 Gestión de Estudiantes
- 📋 Datos personales centralizados: nombre, grado, sección, contacto del apoderado.
- 📈 Historial completo de incidencias por estudiante.

### 🔐 Autenticación Segura y Roles Diferenciados
- 🔑 Inicio de sesión seguro con correo y contraseña (Firebase Auth).
- 🎭 Tipos de usuarios definidos:
  - `👨‍🏫 Profesor`
  - `🧑‍🏫 Tutor`
  - `🏛️ Administrador`

---

## 🔧 Tecnologías Utilizadas

| 🔧 Tecnología        | ⚙️ Uso Principal                          |
|----------------------|-------------------------------------------|
| **Kotlin**           | Lógica principal y desarrollo Android     |
| **ViewBinding**      | Vinculación segura entre vistas y código  |
| **Firebase Auth**    | Sistema de autenticación                  |
| **Cloud Firestore**  | Base de datos NoSQL en tiempo real        |
| **Firebase Storage** | Almacenamiento de imágenes                |
| **Crashlytics**      | Monitoreo de errores                      |
| **Analytics**        | Análisis de uso y comportamiento          |
| **Glide**            | Carga eficiente de imágenes               |
| **iTextPDF / Jsoup** | Generación de informes PDF                |

---

## 🏫 Aplicación en el Contexto Escolar

> 📌 En uso piloto en una institución de **educación secundaria**, con los siguientes beneficios:

- 🧑‍🏫 **Docentes:** Registra incidencias desde el aula en segundos.
- 👨‍🏫 **Tutores:** Supervisa casos específicos de su grupo.
- 📊 **Directivos:** Genera reportes institucionales automatizados.

---

## 🗃️ Estructura de Colecciones en Firebase

📁 Estudiante { id, nombre, grado, sección, celular_apoderado }
📁 Incidencia { id, tipo, gravedad, descripcion, fecha, profesor_id, estado, evidencia_url }
📁 Cita { id, incidencia_id, apoderado_nombre, fecha_cita, fecha_registro }
📁 Informe { id, incidencia_id, contenido_html, pdf_url, fecha_emision }
📁 Profesor { id, nombre, rol, seccion_asignada }

---

## 📌 Estado Actual del Proyecto

> 🟡 **En desarrollo avanzado, listo para pruebas en entorno real.**  
> ✔️ Pruebas internas finalizadas.  
> ✔️ Preparado para despliegue oficial tras evaluación operativa.

---

## 👥 Equipo de Desarrollo

| Nombre         | Rol                    |
|----------------|------------------------|
| Karol          | Operador               |
| Melisa         | Desarrollador Android  |
| Sebastián      | Operador               |
| Adrián         | Desarrollador Android  |
