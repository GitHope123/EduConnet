package com.example.educonnet.ui.incidencia
import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.educonnet.R


class EstudianteAgregarAdapter(private val estudiantes: MutableList<EstudianteAgregar>,
                               private val onItemClick: (EstudianteAgregar) -> Unit ) :
    RecyclerView.Adapter<EstudianteAgregarAdapter.EstudianteAgregarViewHolder>() {
    private var selectedPosition: Int = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EstudianteAgregarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_estudiante_registrar_incidencia, parent, false)
        return EstudianteAgregarViewHolder(view)
    }

    override fun onBindViewHolder(holder: EstudianteAgregarViewHolder, position: Int) {
        val estudiante = estudiantes[position]
        holder.bind(estudiante)
        holder.itemView.setOnClickListener {
            selectedPosition = holder.adapterPosition
            notifyDataSetChanged() // Actualiza todos los ítems para reflejar el cambio de color
            onItemClick(estudiante)
        }
    }

    override fun getItemCount(): Int = estudiantes.size

    inner class EstudianteAgregarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val studentNameTextView: TextView = itemView.findViewById(R.id.studentNameTextView)
        private val studentGradeTextView: TextView = itemView.findViewById(R.id.studentGradeTextView)
        private val studentSectionTextView: TextView = itemView.findViewById(R.id.studentSectionTextView)

        @SuppressLint("PrivateResource")
        fun bind(estudiante: EstudianteAgregar) {

            studentNameTextView.text =estudiante.apellidos+ " " +estudiante.nombres
            studentGradeTextView.text = estudiante.grado.toString()
            studentSectionTextView.text = estudiante.nivel
        }
    }
}