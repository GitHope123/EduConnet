package com.example.educonnet.ui.tutoria.recurso

data class Cita(
    var idCita: String? = "",
    var apoderado: String = "",
    var parentesco: String = "",
    var fechaCita: String = "",
    var hora: String = "",
    var detalle: String? = "",
    var createFecha: String = "",
    var idProfesor: String = "",
    var fechaModificacion: String? = "" // Nuevo campo para tracking de modificaciones
) {
    // Constructor sin argumentos para Firestore
    constructor() : this(
        idCita = "",
        apoderado = "",
        parentesco = "",
        fechaCita = "",
        hora = "",
        detalle = "",
        createFecha = "",
        idProfesor = "",
        fechaModificacion = ""
    )
}
