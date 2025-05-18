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
    var nivel: String = "",              // Nivel educativo (ej: primaria, secundaria)
    var password: String = "",           // Contraseña del profesor
    var dni: Long? = null                // DNI del profesor (nullable)
) : Serializable {

    // Constructor sin argumentos requerido por Firebase
    constructor() : this(null, "", "", null, "", "", false, 0, "", "", null)

    companion object {
        // Función para crear un Profesor desde un Map (opcional)
        fun fromMap(map: Map<String, Any?>): Profesor {
            return Profesor(
                idProfesor = map["idProfesor"] as? String,
                nombres = map["nombres"] as? String ?: "",
                apellidos = map["apellidos"] as? String ?: "",
                celular = (map["celular"] as? Long) ?: (map["celular"] as? String)?.toLongOrNull(),
                cargo = map["cargo"] as? String ?: "",
                correo = map["correo"] as? String ?: "",
                tutor = map["tutor"] as? Boolean ?: false,
                grado = (map["grado"] as? Long) ?: (map["grado"] as? String)?.toLongOrNull() ?: 0,
                nivel = map["nivel"] as? String ?: "",
                password = map["password"] as? String ?: "",
                dni = (map["dni"] as? Long) ?: (map["dni"] as? String)?.toLongOrNull()
            )
        }
    }

    // Función para convertir a Map (opcional)
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "idProfesor" to idProfesor,
            "nombres" to nombres,
            "apellidos" to apellidos,
            "celular" to celular,
            "cargo" to cargo,
            "correo" to correo,
            "tutor" to tutor,
            "grado" to grado,
            "nivel" to nivel,
            "password" to password,
            "dni" to dni
        )
    }
}