package com.example.educonnet.ui.profesor

import java.io.Serializable

data class Profesor(
    var idProfesor: String? = null,      // ID del profesor (documento en Firestore)
    var nombres: String = "",            // Nombre(s) del profesor
    var apellidos: String = "",          // Apellido(s) del profesor
    var celular: Long? = null,           // Número de teléfono del profesor (nullable)
    var cargo: String = "",              // Materia que enseña el profesor
    var correo: String = "",             // Correo electrónico del profesor
    var tutor: Boolean = false,          // Indica si el profesor es tutor
    var grado: Long = 0,                 // Grado del profesor (valor por defecto 0)
    var seccion: String = "",              // Nivel educativo (ej: primaria, secundaria)
    var password: String = "",           // Contraseña del profesor
    var dni: Long? = null                // DNI del profesor (nullable)
) : Serializable {

    // Constructor sin argumentos requerido por Firebase
    constructor() : this(null, "", "", null, "", "", false, 0, "", "", null)


}