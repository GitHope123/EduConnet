# 📚 EduConnect - Registro de Incidencias Escolares

![platform](https://img.shields.io/badge/Plataforma-Android-blue?logo=android)
![status](https://img.shields.io/badge/Estado-Producción-brightgreen)
![firebase](https://img.shields.io/badge/Backend-Firebase-orange?logo=firebase)
![license](https://img.shields.io/badge/Licencia-MIT-lightgrey)

**EduConnect** es una aplicación Android diseñada especialmente para instituciones educativas con el fin de **gestionar, registrar y dar seguimiento a las incidencias escolares** de forma eficiente y centralizada. Este sistema permite a docentes y tutores registrar comportamientos, faltas, observaciones o cualquier tipo de situación relevante ocurrida durante la jornada escolar.

---

## 🚀 Funcionalidades principales

### 👨‍🏫 Registro de incidencias
- 📌 Registro inmediato de incidencias académicas o conductuales por parte del profesor.
- 👤 Asociación directa al estudiante involucrado.
- 🗃️ Clasificación por **tipo de incidencia**, **gravedad**, **fecha**, **materia** y más.
- 🖊️ Registro automático del **profesor responsable**.

### 🗂️ Historial y seguimiento
- 🧾 Visualización de todas las incidencias registradas para cada estudiante.
- 🔎 Filtros por estado: `Pendiente`, `Revisado`, `Todos`.
- 📄 Detalles ampliados para seguimiento y análisis.

### 📌 Gestión por tutores
- 👨‍👩‍👧‍👦 Los tutores visualizan exclusivamente las incidencias relacionadas con su grado y sección.
- ✅ Pueden marcar incidencias como **revisadas** o dejar comentarios de seguimiento.
- 🧭 Interfaz de pestañas intuitiva (`Todos`, `Pendientes`, `Revisados`).

### 🧑‍🎓 Gestión de estudiantes
- 🗃️ Base de datos con los datos personales y académicos de cada estudiante.
- 📚 Asociación automática con grado y sección.
- 🔍 Acceso a perfil detallado desde la vista de incidencias.

### 🔐 Autenticación segura
- 🔑 Inicio de sesión mediante **correo institucional** y/o **Google Sign-In**.
- 🧩 Roles diferenciados: **profesor**, **tutor** o **administrador**.

### ☁️ Integración con Firebase
- ✅ **Firebase Authentication** para control de acceso.
- 🔥 **Cloud Firestore** para almacenamiento en tiempo real de datos.
- 🗂️ **Firebase Storage** para almacenar evidencias o documentos (opcional).
- 📊 **Firebase Analytics** y **Crashlytics** para monitoreo y mejora continua.

---

## 🏫 Aplicación en contexto real

Este sistema está siendo aplicado en un entorno **escolar real**, con beneficios directos para:

- 👨‍🏫 **Docentes**: Permite documentar comportamientos de forma estructurada.
- 👨‍👧‍👦 **Tutores**: Mejora la comunicación entre profesores y tutores respecto al progreso y conducta de los estudiantes.
- 🧑‍💼 **Administradores**: Acceden a reportes consolidados, lo que facilita la toma de decisiones y el diseño de estrategias pedagógicas.

---

## 🛠️ Tecnologías utilizadas

| Categoría         | Tecnologías                                                                 |
|------------------|------------------------------------------------------------------------------|
| 🧠 Lenguaje       | `Kotlin`, `XML`                                                              |
| ⚙️ SDK            | `Android SDK`, `ViewBinding`                                                 |
| ☁️ Backend        | `Firebase Authentication`, `Firestore`, `Storage`, `Analytics`, `Crashlytics` |
| 🖼️ UI             | `Google Material Design`, `Glide`                                            |
| 🧾 Reportes       | `Jsoup`, `iTextPDF`                                                          |

---

## 📷 Capturas de pantalla

<div align="center">
  
| Inicio de sesión | Registro incidencia | Vista tutor |
|------------------|---------------------|--------------|
| ![img1](ruta/a/captura1.png) | ![img2](ruta/a/captura2.png) | ![img3](ruta/a/captura3.png) |

</div>

---

## ✅ Estado del proyecto

🟢 **Producción funcional y en mejora continua.**  
Actualmente en uso en una institución educativa de nivel secundario.

---

## 👥 Desarrolladores y operadores

| Nombre                  | Rol                  |
|------------------------|----------------------|
| 👨‍💻 **Melisa**   | Desarrollador   |
| 👩‍💻 **Adrian**   | Desarrollador  |
| 👨‍🏫 **Sebastian**   | Operador  |
| 🧑‍🔧 **Karol**   | Operador  |

---

