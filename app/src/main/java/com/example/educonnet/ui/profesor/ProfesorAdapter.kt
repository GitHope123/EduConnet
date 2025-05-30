package com.example.educonnet.ui.profesor

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.educonnet.R
import com.google.android.material.imageview.ShapeableImageView

class ProfesorAdapter(
    private val context: Context,
    private val profesores: List<Profesor>,
    private val onEditClickListener: (Profesor) -> Unit,
    private val onDeleteClickListener: (Profesor) -> Unit,
    private val isEditButtonVisible: Boolean = true
) : RecyclerView.Adapter<ProfesorAdapter.ProfesorViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfesorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_profesor, parent, false)
        return ProfesorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfesorViewHolder, position: Int) {
        holder.bind(profesores[position])
    }

    override fun getItemCount(): Int = profesores.size

    inner class ProfesorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewNombreCompleto: TextView = itemView.findViewById(R.id.textViewNombreCompletos)
        private val textViewTelefono: TextView = itemView.findViewById(R.id.textViewTelefono)
        private val textViewCorreo: TextView = itemView.findViewById(R.id.textViewCorreo)
        private val editButton: ShapeableImageView = itemView.findViewById(R.id.imageButtonEdit)
        private val deleteButton: ShapeableImageView = itemView.findViewById(R.id.imageButtonDelete)

        fun bind(profesor: Profesor) {
            val nombreCompleto = "${profesor.apellidos} ${profesor.nombres}"
            textViewNombreCompleto.text = nombreCompleto
            textViewTelefono.text = profesor.celular?.toString() ?: "N/A"
            textViewCorreo.text = profesor.correo ?: "N/A"

            editButton.visibility = if (isEditButtonVisible) View.VISIBLE else View.GONE
            deleteButton.visibility = if (isEditButtonVisible) View.VISIBLE else View.GONE

            if (isEditButtonVisible) {
                editButton.setOnClickListener {
                    onEditClickListener(profesor)
                }
                deleteButton.setOnClickListener {
                    onDeleteClickListener(profesor)
                }
                itemView.setOnClickListener {
                    onEditClickListener(profesor)
                }
                itemView.isClickable = true
                itemView.isFocusable = true
            } else {
                // Deshabilitar clicks si no puede editar
                editButton.setOnClickListener(null)
                deleteButton.setOnClickListener(null)
                itemView.setOnClickListener(null)
                itemView.isClickable = false
                itemView.isFocusable = false
            }
        }

    }
}
