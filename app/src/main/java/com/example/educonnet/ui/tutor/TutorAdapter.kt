package com.example.educonnet.ui.tutor

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.educonnet.R
import com.example.educonnet.ui.profesor.Profesor
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore

class TutorAdapter(
    private val onEditClickListener: (Profesor) -> Unit,
    private val onRemoveClickListener: (Profesor) -> Unit,
    private val isButtonVisible: Boolean,
    private val isTextViewGradosNivelVisible: Boolean,
    private val isImageButtonQuitarTutor: Boolean,
    private val buttonSeleccionar: Boolean
) : ListAdapter<Profesor, TutorAdapter.ProfesorViewHolder>(TutorDiffCallback()) {

    private var selectedProfesorId: String? = null
    private var fullList: List<Profesor> = emptyList()
    private var filteredList: List<Profesor> = emptyList()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    inner class ProfesorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewNombre: TextView = itemView.findViewById(R.id.textViewTutorNombreCompleto)
        private val textViewCelular: TextView = itemView.findViewById(R.id.textViewTutorCelular)
        private val textViewCorreo: TextView = itemView.findViewById(R.id.textViewTutorCorreo)
        private val imageButtonSeleccionar: ImageButton = itemView.findViewById(R.id.imageButtonSeleccionar)
        private val textViewGradosNivelTutor: TextView = itemView.findViewById(R.id.textViewGradosNivelTutor)
        private val imageButtonQuitarTutor: ImageButton = itemView.findViewById(R.id.imageButtonQuitarTutor)
        private val imageButtonLlamadaTutor: ImageButton = itemView.findViewById(R.id.imageViewTutorLlamada)

        fun bind(profesor: Profesor) {
            textViewNombre.text = "${profesor.nombres} ${profesor.apellidos}"
            textViewCelular.text = profesor.celular.toString()
            textViewCorreo.text = profesor.correo

            val letra = profesor.seccion.firstOrNull() ?: ' '
            textViewGradosNivelTutor.text = "${profesor.grado} $letra"

            // Configuración de la visibilidad
            imageButtonSeleccionar.visibility = if (isButtonVisible) View.VISIBLE else View.GONE
            textViewGradosNivelTutor.visibility = if (isTextViewGradosNivelVisible) View.VISIBLE else View.GONE
            imageButtonQuitarTutor.visibility = if (isImageButtonQuitarTutor) View.VISIBLE else View.GONE
            imageButtonSeleccionar.visibility = if (buttonSeleccionar) View.VISIBLE else View.GONE

            val idProfesor = profesor.idProfesor ?: return
            val isSelected = idProfesor == selectedProfesorId
            imageButtonSeleccionar.setColorFilter(if (isSelected) Color.YELLOW else Color.GRAY)

            itemView.setOnClickListener {
                toggleSelection(idProfesor)
                onEditClickListener(profesor)
            }

            imageButtonQuitarTutor.setOnClickListener {
                onRemoveClickListener(profesor)
            }

            imageButtonSeleccionar.setOnClickListener {
                toggleSelection(idProfesor)
                onEditClickListener(profesor)
            }

            imageButtonLlamadaTutor.setOnClickListener {
                val phoneNumber = profesor.celular.toString()
                if (phoneNumber.isNotBlank()) {
                    val options = listOf("Llamar a ${profesor.nombres}", "Enviar mensaje por WhatsApp")
                    val icons = listOf(R.drawable.ic_phone, R.drawable.ic_whatsapp)

                    val adapter = object : ArrayAdapter<String>(itemView.context, R.layout.dialog_option_item, options) {
                        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                            val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.dialog_option_item, parent, false)
                            val iconView = view.findViewById<ImageView>(R.id.icon)
                            val textView = view.findViewById<TextView>(R.id.option_text)

                            iconView.setImageResource(icons[position])
                            textView.text = options[position]

                            return view
                        }
                    }

                    MaterialAlertDialogBuilder(itemView.context)
                        .setTitle("Selecciona una opción")
                        .setAdapter(adapter) { dialog, which ->
                            when (which) {
                                0 -> {
                                    try {
                                        val callIntent = Intent(Intent.ACTION_DIAL).apply {
                                            data = Uri.parse("tel:$phoneNumber")
                                        }
                                        itemView.context.startActivity(callIntent)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        Toast.makeText(itemView.context, "Error al intentar realizar la llamada", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                1 -> {
                                    try {
                                        val whatsappIntent = Intent(Intent.ACTION_VIEW).apply {
                                            data = Uri.parse("https://wa.me/$phoneNumber")
                                        }
                                        itemView.context.startActivity(whatsappIntent)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        Toast.makeText(itemView.context, "Error al intentar abrir WhatsApp", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                } else {
                    Toast.makeText(itemView.context, "Número de teléfono no válido", Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun toggleSelection(idProfesor: String) {
            selectedProfesorId = if (selectedProfesorId == idProfesor) null else idProfesor
            notifyItemChanged(adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfesorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tutor, parent, false)
        return ProfesorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfesorViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun updateList(newList: List<Profesor>) {
        fullList = newList
        filteredList = newList
        submitList(filteredList)
    }

    fun resetList() {
        filteredList = fullList
        submitList(filteredList)
    }

    fun filterList(query: String) {
        filteredList = fullList.filter { profesor ->
            val fullName = "${profesor.nombres} ${profesor.apellidos}".lowercase()
            fullName.contains(query.lowercase())
        }
        submitList(filteredList)
    }
}