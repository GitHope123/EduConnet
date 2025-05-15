package com.example.educonnet.ui.profesor

import java.io.Serializable

data class Profesor(
    var idProfesor: String? = null, // ID del profesor (documento en Firestore)
    var nombres: String = "",       // Nombre(s) del profesor
    var apellidos: String = "",     // Apellido(s) del profesor
    var celular: Long? = null,      // Número de teléfono del profesor (puede ser null)
    var cargo: String = "",         // Materia que enseña el profesor
    var correo: String = "",        // Correo electrónico del profesor
    var tutor: Boolean = false,     // Indica si el profesor es tutor
    var grado: Long,        // Grado del profesor (puede ser null)
    var nivel: String = "",         // Nivel educativo del profesor (por ejemplo, primaria, secundaria)
    var password: String = "",      // Contraseña del profesor
    var dni: Long? = null           // DNI del profesor (puede ser null)
) : Serializable