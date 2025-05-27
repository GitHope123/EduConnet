package com.example.educonnet.ui.estudiante

import java.io.Serializable

data class Estudiante(
    val idEstudiante: String = "",
    val apellidos: String = "",
    val nombres: String = "",
    val grado: Int = 0,
    val seccion: String = "", // A B C D..
    val cantidadIncidencias:Int=0,
    val celularApoderado: Int =0

):Serializable
