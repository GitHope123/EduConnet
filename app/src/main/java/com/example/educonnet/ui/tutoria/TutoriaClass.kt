package com.example.educonnet.ui.tutoria

import java.io.Serializable

data class TutoriaClass(
    val id: String = "",
    val apellidoEstudiante: String = "",
    val apellidoProfesor: String = "",
    val detalle: String = "",
    var estado: String = "",
    val fecha: String = "",
    val grado: Int = 0,
    val atencion: String = "",
    val hora: String = "",
    val nombreEstudiante: String = "",
    val nombreProfesor: String = "",
    val nivel: String = "",
    val celularApoderado: Int = 0,
    val tipo: String = "",
    val urlImagen: String = "",
    val cargo: String = ""
) : Serializable {

    // Modifica el estado directamente
    fun cambiarEstado(nuevoEstado: String) {
        this.estado = nuevoEstado
    }
}
