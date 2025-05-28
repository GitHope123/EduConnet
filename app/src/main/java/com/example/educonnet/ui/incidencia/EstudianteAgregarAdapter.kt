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
    private var estudiantes: List<EstudianteAgregar>,  // Cambiado a var para poder actualizar lista
    seleccionados: Set<String>,
    private val onItemChecked: (EstudianteAgregar, Boolean) -> Unit
) : RecyclerView.Adapter<EstudianteAgregarAdapter.EstudianteAgregarViewHolder>() {

    // Set interno mutable para IDs seleccionados
    private val selectedItems = seleccionados.toMutableSet()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EstudianteAgregarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_estudiante_registrar_incidencia, parent, false)
        return EstudianteAgregarViewHolder(view)
    }

    override fun onBindViewHolder(holder: EstudianteAgregarViewHolder, position: Int) {
        val estudiante = estudiantes[position]
        val isChecked = selectedItems.contains(estudiante.id)

        // Limpiar listener antes de modificar el estado para evitar callbacks innecesarios
        holder.checkBox.setOnCheckedChangeListener(null)

        // Bind con los datos y estado actual
        holder.bind(estudiante, isChecked)

        // Reasignar listener para detectar cambios en la selección
        holder.checkBox.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                selectedItems.add(estudiante.id)
            } else {
                selectedItems.remove(estudiante.id)
            }
            onItemChecked(estudiante, checked)
        }
    }

    override fun getItemCount(): Int = estudiantes.size

    // Método para actualizar la lista de estudiantes
    fun updateEstudiantes(nuevosEstudiantes: List<EstudianteAgregar>) {
        estudiantes = nuevosEstudiantes
        notifyDataSetChanged()
    }

    // Obtener Set inmutable de IDs seleccionados
    fun getSelectedIds(): Set<String> = selectedItems.toSet()

    // Obtener lista de estudiantes seleccionados
    fun getSeleccionados(): List<EstudianteAgregar> =
        estudiantes.filter { selectedItems.contains(it.id) }

    // Actualizar la selección externa (útil para refrescar selección al filtrar)
    fun updateSeleccionados(nuevosSeleccionados: Set<String>) {
        selectedItems.clear()
        selectedItems.addAll(nuevosSeleccionados)
        notifyDataSetChanged()
    }

    inner class EstudianteAgregarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBoxEstudiante)
        val textViewNombre: TextView = itemView.findViewById(R.id.textViewNombreEstudiante)
        val textViewGradoSeccion: TextView = itemView.findViewById(R.id.textViewGradoSeccion)

        @SuppressLint("SetTextI18n")
        fun bind(estudiante: EstudianteAgregar, isChecked: Boolean) {
            textViewNombre.text = "${estudiante.nombres} ${estudiante.apellidos}"
            textViewGradoSeccion.text = "${estudiante.grado}° ${estudiante.seccion}"
            checkBox.isChecked = isChecked
        }
    }
}
