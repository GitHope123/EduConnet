package com.example.educonnet.ui.incidencia.estado

import java.io.Serializable

data class IncidenciaClass(
    val id: String = "",
    val fecha: String = "",
    val hora: String = "",
    val nombreEstudiante: String = "",
    val apellidoEstudiante: String = "",
    val grado: Int = 0,
    val seccion: String = "",
    val celularApoderado: Int = 0,
    val tipo: String = "",
    val atencion: String = "",
    val estado: String = "",
    val detalle: String = "",
    val imageUri: String = ""
) : Serializable
