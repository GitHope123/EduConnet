package com.example.educonnet.ui.incidencia
import java.io.Serializable

data class EstudianteAgregar(
    val id: String="",
    val nombres: String = "",
    val apellidos: String = "",
    val grado: Int = 0,
    val seccion: String = "",
    val celularApoderado: Int=0,
):Serializable