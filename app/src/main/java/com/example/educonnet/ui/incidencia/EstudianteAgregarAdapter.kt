package com.example.educonnet.ui.incidencia

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.educonnet.R

class EstudianteAgregarAdapter(
    private var estudiantes: List<EstudianteAgregar>,
    private var seleccionados: Set<String>,
    private val onItemChecked: (EstudianteAgregar, Boolean) -> Unit
) : RecyclerView.Adapter<EstudianteAgregarAdapter.EstudianteAgregarViewHolder>() {

    // Lista interna mutable para mantener el estado de selección
    private val selectedItems = seleccionados.toMutableSet()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EstudianteAgregarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_estudiante_registrar_incidencia, parent, false)
        return EstudianteAgregarViewHolder(view, onItemChecked)
    }

    override fun onBindViewHolder(holder: EstudianteAgregarViewHolder, position: Int) {
        val estudiante = estudiantes[position]
        val isSelected = selectedItems.contains(estudiante.id)
        holder.bind(estudiante, isSelected)
    }

    override fun getItemCount(): Int = estudiantes.size

    // Método para actualizar la lista completa de estudiantes
    fun updateEstudiantes(nuevosEstudiantes: List<EstudianteAgregar>) {
        estudiantes = nuevosEstudiantes
        notifyDataSetChanged()
    }

    // Método para actualizar los estudiantes seleccionados
    fun updateSelectedStudents(nuevosSeleccionados: Set<String>) {
        selectedItems.clear()
        selectedItems.addAll(nuevosSeleccionados)
        notifyDataSetChanged()
    }

    // Obtener lista de estudiantes seleccionados
    fun getSelectedStudents(): List<EstudianteAgregar> {
        return estudiantes.filter { selectedItems.contains(it.id) }
    }

    // Obtener IDs de estudiantes seleccionados
    fun getSelectedIds(): Set<String> = selectedItems.toSet()

    inner class EstudianteAgregarViewHolder(
        itemView: View,
        private val onItemChecked: (EstudianteAgregar, Boolean) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val checkBox: CheckBox = itemView.findViewById(R.id.checkBoxEstudiante)
        private val textViewNombre: TextView = itemView.findViewById(R.id.textViewNombreEstudiante)
        private val textViewGradoSeccion: TextView = itemView.findViewById(R.id.textViewGradoSeccion)

        private var currentEstudiante: EstudianteAgregar? = null

        init {
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                currentEstudiante?.let {
                    if (isChecked) {
                        selectedItems.add(it.id)
                    } else {
                        selectedItems.remove(it.id)
                    }
                    onItemChecked(it, isChecked)
                }
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(estudiante: EstudianteAgregar, isSelected: Boolean) {
            currentEstudiante = estudiante
            textViewNombre.text = "${estudiante.nombres} ${estudiante.apellidos}"
            textViewGradoSeccion.text = "${estudiante.grado}° ${estudiante.seccion}"

            // Evitar notificaciones innecesarias al cambiar el estado
            checkBox.setOnCheckedChangeListener(null)
            checkBox.isChecked = isSelected
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                currentEstudiante?.let {
                    if (isChecked) {
                        selectedItems.add(it.id)
                    } else {
                        selectedItems.remove(it.id)
                    }
                    onItemChecked(it, isChecked)
                }
            }
        }
    }
}