package com.example.educonnet.ui.incidencia

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.educonnet.databinding.ItemEstudianteSeleccionadoBinding

class EstudiantesSeleccionadosAdapter(
    private val items: MutableList<EstudianteAgregar>,
    private val onItemRemoved: (EstudianteAgregar) -> Unit
) : RecyclerView.Adapter<EstudiantesSeleccionadosAdapter.ViewHolder>() {

    // Método para inicializar el swipe to delete
    fun attachSwipeToDelete(recyclerView: RecyclerView) {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val removedItem = items[position]
                    items.removeAt(position)
                    notifyItemRemoved(position)
                    onItemRemoved(removedItem)
                }
            }
        }).attachToRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEstudianteSeleccionadoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    // Método para actualizar la lista completa
    fun updateItems(newItems: List<EstudianteAgregar>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemEstudianteSeleccionadoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: EstudianteAgregar) {
            with(binding) {
                tvNombreEstudiante.text = "${item.apellidos} ${item.nombres}"
                tvGradoSeccion.text = "${item.grado}° ${item.seccion}"
            }
        }
    }
}