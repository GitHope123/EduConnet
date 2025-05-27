package com.example.educonnet.ui.estudiante

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telephony.PhoneNumberUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.educonnet.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class EstudianteAdapter(
    private val estudiantes: List<Estudiante>,
    private val onEditClickListener: (Estudiante) -> Unit,
    private val isEditButtonVisible: Boolean
) : RecyclerView.Adapter<EstudianteAdapter.EstudianteViewHolder>() {

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EstudianteViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_estudiante, parent, false)
        return EstudianteViewHolder(view)
    }

    override fun onBindViewHolder(holder: EstudianteViewHolder, position: Int) {
        holder.bind(estudiantes[position])
    }

    override fun getItemCount(): Int = estudiantes.size

    inner class EstudianteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nombreTextView: TextView = itemView.findViewById(R.id.textViewNombreCompleto)
        private val gradoTextView: TextView = itemView.findViewById(R.id.studentGradeTextViewItem)
        private val celularTextView: TextView = itemView.findViewById(R.id.textViewCelularApoderado)
        private val btnCall: ImageButton = itemView.findViewById(R.id.imageButtonCelularApoderado)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.imageButtonEditStudent)

        fun bind(estudiante: Estudiante) {
            bindBasicInfo(estudiante)
            setupEditButton(estudiante)
            setupCallButton(estudiante)
            setupItemClickListener(estudiante)
        }

        @SuppressLint("StringFormatMatches")
        private fun bindBasicInfo(estudiante: Estudiante) {
            nombreTextView.text = context.getString(
                R.string.nombre_completo_format,
                estudiante.apellidos,
                estudiante.nombres
            )

            gradoTextView.text = context.getString(
                R.string.grado_nivel_format,
                estudiante.grado,
                estudiante.seccion
            )

            celularTextView.text = estudiante.celularApoderado?.let { phone ->
                PhoneNumberUtils.formatNumber(phone.toString(), "PE")
            } ?: context.getString(R.string.phone_not_available)
        }

        private fun setupEditButton(estudiante: Estudiante) {
            btnEdit.visibility = if (isEditButtonVisible) View.VISIBLE else View.GONE
            btnEdit.setOnClickListener { onEditClickListener(estudiante) }
        }

        private fun setupCallButton(estudiante: Estudiante) {
            val hasPhoneNumber = estudiante.celularApoderado != null && estudiante.celularApoderado.toString().isNotEmpty()
            btnCall.isEnabled = hasPhoneNumber
            btnCall.alpha = if (hasPhoneNumber) 1f else 0.5f

            btnCall.setOnClickListener {
                if (hasPhoneNumber) {
                    mostrarOpcionesContacto(estudiante)
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.phone_not_available),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        private fun setupItemClickListener(estudiante: Estudiante) {
            itemView.setOnClickListener { onEditClickListener(estudiante) }
        }

        private fun mostrarOpcionesContacto(estudiante: Estudiante) {
            val phoneNumber = estudiante.celularApoderado?.toString() ?: return

            MaterialAlertDialogBuilder(context)
                .setTitle(context.getString(R.string.contact_parent_title))
                .setItems(context.resources.getStringArray(R.array.contact_options)) { _, which ->
                    when (which) {
                        0 -> realizarLlamada(phoneNumber)
                        1 -> abrirWhatsApp(phoneNumber)
                        2 -> enviarSMS(phoneNumber)
                    }
                }
                .setNegativeButton(context.getString(R.string.cancel), null)
                .show()
        }

        private fun realizarLlamada(phoneNumber: String) {
            try {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:${phoneNumber.trim()}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                ContextCompat.startActivity(context, intent, null)
            } catch (e: Exception) {
                showError(context.getString(R.string.call_failed))
            }
        }

        private fun abrirWhatsApp(phoneNumber: String) {
            try {
                val url = "https://wa.me/51${phoneNumber.trim()}" // 51 es código de país para Perú
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(url)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                ContextCompat.startActivity(context, intent, null)
            } catch (e: Exception) {
                showError(context.getString(R.string.whatsapp_not_installed))
            }
        }

        private fun enviarSMS(phoneNumber: String) {
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("sms:${phoneNumber.trim()}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                ContextCompat.startActivity(context, intent, null)
            } catch (e: Exception) {
                showError(context.getString(R.string.sms_failed))
            }
        }

        private fun showError(message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}