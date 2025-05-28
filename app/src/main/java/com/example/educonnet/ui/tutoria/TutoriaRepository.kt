package com.example.educonnet.ui.tutoria
import com.google.firebase.firestore.FirebaseFirestore

class TutoriaRepository {

    private val firestore = FirebaseFirestore.getInstance()


    fun getIncidenciasPorGradoSeccion(
        grado: Int,
        nivel: String,
        callback: (List<TutoriaClass>) -> Unit
    ) {
        var query = firestore.collection("Incidencia")
            .whereEqualTo("grado", grado)
            .whereEqualTo("seccion", nivel)
        query.get()
            .addOnSuccessListener { querySnapshot ->
                val incidencias = querySnapshot.documents.mapNotNull { document ->
                    TutoriaClass(
                        id = document.id,
                        apellidoEstudiante = document.getString("apellidoEstudiante") ?: "",
                        apellidoProfesor = document.getString("apellidoProfesor") ?: "",
                        detalle = document.getString("detalle") ?: "",
                        estado = document.getString("estado") ?: "",
                        fecha = document.getString("fecha") ?: "",
                        grado = document.getLong("grado")?.toInt() ?: 0,
                        atencion = document.getString("atencion") ?: "",
                        hora = document.getString("hora") ?: "",
                        nombreEstudiante = document.getString("nombreEstudiante") ?: "",
                        nombreProfesor = document.getString("nombreProfesor") ?: "",
                        seccion = document.getString("seccion") ?: "",
                        celularApoderado =document.getLong("celularApoderado")?.toInt() ?:0,
                        tipo = document.getString("tipo") ?: "",
                        urlImagen = document.getString("urlImagen") ?: "",
                        cargo= document.getString("cargo") ?: "",
                    )
                }
                callback(incidencias)
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
                callback(emptyList())
            }
    }
}