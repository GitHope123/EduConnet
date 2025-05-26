package com.example.educonnet.ui.tutoria.recurso

data class Informe(
    var idInforme: String? = null,
    var createFecha: String? = null, // generados automaticamente
    var createHora: String? = null, //generados automaticamente
    var detalles: String? = null,
    var apoderado: String? = null, // toma es dato de la coleccion Cita
    var relacionFamiliar: String? = null // toma es dato de Cita
)
