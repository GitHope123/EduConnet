package com.example.educonnet.ui.incidencia

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.educonnet.databinding.ItemEstudianteSeleccionadoBinding


class EstudiantesSeleccionadosAdapter(private val items: List<EstudianteAgregar>) :
    RecyclerView.Adapter<EstudiantesSeleccionadosAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemEstudianteSeleccionadoBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    class ViewHolder(private val binding: ItemEstudianteSeleccionadoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: EstudianteAgregar) {
            binding.tvNombreEstudiante.text = "${item.apellidos} ${item.nombres}"
            binding.tvGradoSeccion.text = "${item.grado} ${item.seccion}"
        }
    }
}
