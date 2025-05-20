package com.example.educonnet.ui.tutoria

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.educonnet.R
import com.example.educonnet.databinding.ItemTutoriaBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import java.io.File

class TutoriaAdapter(private var listaTutorias: MutableList<TutoriaClass>) :
    RecyclerView.Adapter<TutoriaAdapter.TutoriaViewHolder>() {

    inner class TutoriaViewHolder(private val binding: ItemTutoriaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val imagenViewAlerta: ImageView = binding.ImagenViewAlertaTutoria
        private val textViewGravedad: MaterialTextView = binding.TextViewGravedadTutoria
        private val textViewFecha: MaterialTextView = binding.textViewFechaTutoria
        private val textViewHora: MaterialTextView = binding.textViewHoraTutoria
        private val textViewEstudiante: MaterialTextView = binding.textViewEstudianteTutoria
        private val textViewProfesor: MaterialTextView = binding.textViewProfesorTutoria
        private val textViewCurso: Chip = binding.textViewCursoTutoria
        private val textViewEstado: Chip = binding.textViewEstadoTutoria
        private val buttonCall: MaterialButton = binding.imageButtonCallApoderado

        fun bind(tutoria: TutoriaClass) {
            // Configuración de datos
            textViewFecha.text = tutoria.fecha
            textViewHora.text = tutoria.hora
            textViewEstudiante.text = "${tutoria.apellidoEstudiante} ${tutoria.nombreEstudiante}"
            textViewProfesor.text = "${tutoria.nombreProfesor} ${tutoria.apellidoProfesor}"
            textViewCurso.text = "${tutoria.grado} ${tutoria.nivel}"
            textViewEstado.text = tutoria.estado
            textViewGravedad.text = tutoria.atencion

            // Configuración de colores según estado
            setEstadoStyle(tutoria.estado)
            setAlertColors(getAlertColor(tutoria.atencion))

            // Configuración del botón de llamada
            buttonCall.setOnClickListener {
                showContactOptionsDialog(tutoria)
            }

            // Click en el item
            binding.root.setOnClickListener {
                val intent = Intent(it.context, DescripcionRevisar::class.java).apply {
                    putExtra("TUTORIA_EXTRA", tutoria)
                }
                it.context.startActivity(intent)
            }
        }

        private fun setEstadoStyle(estado: String) {
            val context = binding.root.context
            when (estado) {
                "Pendiente" -> {
                    textViewEstado.setChipBackgroundColorResource(R.color.md_theme_light_errorContainer)
                    textViewEstado.setTextColor(ContextCompat.getColor(context, R.color.md_theme_light_error))
                    textViewEstado.chipStrokeColor = ContextCompat.getColorStateList(context, R.color.md_theme_light_error)
                }
                "Revisado" -> {
                    textViewEstado.setChipBackgroundColorResource(R.color.md_theme_light_tertiaryContainer)
                    textViewEstado.setTextColor(ContextCompat.getColor(context, R.color.md_theme_light_onTertiaryContainer))
                    textViewEstado.chipStrokeColor = ContextCompat.getColorStateList(context, R.color.md_theme_light_tertiary)
                }
                else -> {
                    textViewEstado.setChipBackgroundColorResource(R.color.md_theme_light_secondaryContainer)
                    textViewEstado.setTextColor(ContextCompat.getColor(context, R.color.md_theme_light_onSecondaryContainer))
                    textViewEstado.chipStrokeColor = ContextCompat.getColorStateList(context, R.color.md_theme_light_secondary)
                }
            }
        }

        private fun getAlertColor(gravedad: String): Int {
            val context = binding.root.context
            return when (gravedad) {
                "Moderado" -> ContextCompat.getColor(context, R.color.md_theme_light_primary)
                "Urgente" -> ContextCompat.getColor(context, R.color.md_theme_light_error)
                else -> ContextCompat.getColor(context, R.color.md_theme_light_tertiary)
            }
        }

        private fun setAlertColors(color: Int) {
            imagenViewAlerta.setColorFilter(color)
            textViewGravedad.setTextColor(color)
        }

        private fun showContactOptionsDialog(tutoria: TutoriaClass) {
            val phoneNumber = tutoria.celularApoderado.toString()
            if (phoneNumber.isNotBlank()) {
                val context = binding.root.context
                val options = listOf(
                    "Llamar a Apoderado de ${tutoria.nombreEstudiante}",
                    "Enviar mensaje por WhatsApp",
                    "Enviar PDF por WhatsApp"
                )
                val icons = listOf(R.drawable.ic_phone, R.drawable.ic_whatsapp, R.drawable.ic_pdf)

                val adapter = object : ArrayAdapter<String>(context, R.layout.dialog_option_item, options) {
                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                        val view = convertView ?: LayoutInflater.from(context)
                            .inflate(R.layout.dialog_option_item, parent, false)
                        val iconView = view.findViewById<ImageView>(R.id.icon)
                        val textView = view.findViewById<TextView>(R.id.option_text)

                        iconView.setImageResource(icons[position])
                        textView.text = options[position]

                        return view
                    }
                }

                MaterialAlertDialogBuilder(context)
                    .setTitle("Selecciona una opción")
                    .setAdapter(adapter) { dialog, which ->
                        when (which) {
                            0 -> makePhoneCall(phoneNumber)
                            1 -> openWhatsApp(phoneNumber)
                            2 -> sendPdfViaWhatsApp(tutoria)
                        }
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            } else {
                Toast.makeText(binding.root.context,
                    "Número de teléfono no válido", Toast.LENGTH_SHORT).show()
            }
        }

        private fun makePhoneCall(phoneNumber: String) {
            try {
                val callIntent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$phoneNumber")
                }
                binding.root.context.startActivity(callIntent)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(binding.root.context,
                    "Error al intentar realizar la llamada", Toast.LENGTH_SHORT).show()
            }
        }

        private fun openWhatsApp(phoneNumber: String) {
            try {
                val whatsappIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://wa.me/$phoneNumber")
                }
                binding.root.context.startActivity(whatsappIntent)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(binding.root.context,
                    "Error al intentar abrir WhatsApp", Toast.LENGTH_SHORT).show()
            }
        }

        private fun sendPdfViaWhatsApp(tutoria: TutoriaClass) {
            val context = binding.root.context // Mueve esta línea al inicio

            try {
                val pdfGenerator = PdfGenerator(context, tutoria)
                val pdfFile = File(context.cacheDir, "Reporte_Tutoria_${tutoria.nombreEstudiante}.pdf")
                pdfGenerator.generatePDF(pdfFile.absolutePath)

                if (pdfFile.exists()) {
                    val fileUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        pdfFile
                    )

                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, fileUri)
                        putExtra(Intent.EXTRA_TEXT, "Consulta la información del estudiante ${tutoria.nombreEstudiante}")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    shareIntent.setPackage("com.whatsapp")
                    context.startActivity(Intent.createChooser(shareIntent, "Enviar PDF por WhatsApp"))
                } else {
                    Toast.makeText(context, "Error al generar el PDF", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "WhatsApp no está instalado", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Error al generar o enviar el PDF: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TutoriaViewHolder {
        val binding = ItemTutoriaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TutoriaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TutoriaViewHolder, position: Int) {
        holder.bind(listaTutorias[position])
    }

    fun updateData(newListTutoria: List<TutoriaClass>) {
        val uniqueTutorias = newListTutoria.distinctBy { it.id }
        listaTutorias.clear()
        listaTutorias.addAll(uniqueTutorias)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = listaTutorias.size
}