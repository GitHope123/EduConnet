package com.example.educonnet.ui.tutor

import androidx.recyclerview.widget.DiffUtil
import com.example.educonnet.ui.profesor.Profesor

class TutorDiffCallback : DiffUtil.ItemCallback<Profesor>() {
    override fun areItemsTheSame(oldItem: Profesor, newItem: Profesor): Boolean {
        // Compare based on unique ID
        return oldItem.idProfesor == newItem.idProfesor
    }

    override fun areContentsTheSame(oldItem: Profesor, newItem: Profesor): Boolean {
        // Compare all relevant fields that affect visual representation
        return oldItem.nombres == newItem.nombres &&
                oldItem.apellidos == newItem.apellidos &&
                oldItem.celular == newItem.celular &&
                oldItem.correo == newItem.correo &&
                oldItem.grado == newItem.grado &&
                oldItem.nivel == newItem.nivel &&
                oldItem.tutor == newItem.tutor
    }

    override fun getChangePayload(oldItem: Profesor, newItem: Profesor): Any? {
        // Return specific changes if needed for partial updates
        return if (oldItem.grado != newItem.grado || oldItem.nivel != newItem.nivel) {
            mapOf("grado_nivel" to true)
        } else {
            super.getChangePayload(oldItem, newItem)
        }
    }
}