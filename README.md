# 🎓 EduConnet  
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
- 📂 Filtros visuales: `Todas`, `Pendientes`, `Revisados`.
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

```plaintext
📁 Estudiante {
  "idEstudiante": "string", // ID generado por Firebase
  "nombres": "string",
  "apellidos": "string",
  "celularApoderado": "int",
  "grado": "int",
  "seccion": "string",
  "cantidadIncidencias": "int"
}

📁 Incidencia {
  "id": "string", // ID generado por Firebase
  "nombreEstudiante": "string",
  "apellidoEstudiante": "string",
  "celularApoderado": "int",
  "grado": "int",
  "nivel": "string", // Equivalente a grado académico
  "seccion": "string", // Ejemplo: A, B, C, D
  "fecha": "string", // Formato ISO (YYYY-MM-DD)
  "hora": "string", // HH:mm:ss
  "tipo": "string", // "Positiva" | "Negativa"
  "estado": "string", // "Pendiente" | "Revisado" | "Notificado" | "Citado" | "Completado"
  "atencion": "string", // Gravedad de la incidencia (solo si es negativa)
  "urlImagen": "string", // URL desde Firebase Storage
  "idProfesor": "string",
  "nombreProfesor": "string",
  "apellidoProfesor": "string",
  "cargo": "string"
}

📁 Cita {
  "idCita": "string",
  "createFecha": "string", // Fecha de creación de la cita (ISO)
  "apoderado": "string",
  "fechaCita": "string", // Fecha programada de la cita (ISO)
  "hora": "string" // Hora programada de la cita
}

📁 Informe {
  "idInforme": "string",
  "createFecha": "string", // Fecha de generación del informe (ISO)
  "detalles": "string",
  "apoderado": "string",
  "relacionFamiliar": "string"
}

📁 Profesor {
  "idProfesor": "string", // ID generado por Firebase
  "nombres": "string",
  "apellidos": "string",
  "celular": "int",
  "correo": "string",
  "dni": "int",
  "grado": "int",
  "seccion": "string", // Puede ser null
  "cargo": "string",
  "password": "string", // Hash almacenado en Firebase
  "tutor": "boolean" // true o false
}
```
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
