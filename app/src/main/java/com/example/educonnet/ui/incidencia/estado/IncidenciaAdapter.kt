package com.example.educonnet.ui.incidencia.estado

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.RecyclerView
import com.example.educonnet.R
import com.example.educonnet.databinding.ItemIncidenciaBinding
import com.example.educonnet.ui.incidencia.DescripcionIncidencia
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class IncidenciaAdapter(
    private var incidencias: List<IncidenciaClass>,
    private val context: Context
) : RecyclerView.Adapter<IncidenciaAdapter.IncidenciaViewHolder>() {

    inner class IncidenciaViewHolder(private val binding: ItemIncidenciaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("MissingInflatedId")
        fun bind(incidencia: IncidenciaClass) {
            with(binding) {
                // Bind basic information
                tvEstudiante.text = context.getString(
                    R.string.full_name_format,
                    incidencia.apellidoEstudiante,
                    incidencia.nombreEstudiante
                )
                tvAtencion.text = incidencia.atencion
                tvHora.text = incidencia.hora
                tvFecha.text = incidencia.fecha
                tvEstado.text = incidencia.estado
                tvGrado.text = incidencia.grado.toString()
                tvNivel.text = incidencia.seccion
                tvTipo.text = incidencia.tipo

                clickFrameStepper.setOnClickListener {
                    try {
                        // Inflate el layout personalizado del diálogo
                        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_stepper, null)
                        if (dialogView == null) {
                            Toast.makeText(context, "No se pudo cargar la vista del diálogo", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        // Obtener referencias a los iconos y textos del stepper
                        val stepViews = listOf(
                            dialogView.findViewById<ImageView?>(R.id.stepEnviadoIcon) to dialogView.findViewById<TextView?>(R.id.stepEnviadoText),
                            dialogView.findViewById<ImageView?>(R.id.stepRevisadoIcon) to dialogView.findViewById<TextView?>(R.id.stepRevisadoText),
                            dialogView.findViewById<ImageView?>(R.id.stepNotificadoIcon) to dialogView.findViewById<TextView?>(R.id.stepNotificadoText),
                            dialogView.findViewById<ImageView?>(R.id.stepCitadoIcon) to dialogView.findViewById<TextView?>(R.id.stepCitadoText),
                            dialogView.findViewById<ImageView?>(R.id.stepCompletadoIcon) to dialogView.findViewById<TextView?>(R.id.stepCompletadoText)
                        )

                        // Mapas para progreso y orden
                        val pasosOrden = listOf("Enviado", "Revisado", "Notificado", "Citado", "Completado")

                        // Función para actualizar los pasos del stepper
                        fun actualizarStepper(estadoActual: String) {
                            try {
                                val indexActual = pasosOrden.indexOf(estadoActual).coerceAtLeast(0)

                                pasosOrden.forEachIndexed { index, paso ->
                                    val (iconView, textView) = stepViews[index]

                                    val isCompleted = index <= indexActual

                                    iconView?.setImageResource(
                                        if (isCompleted) R.drawable.ic_radio_button_checked
                                        else R.drawable.ic_radio_button_unchecked
                                    )

                                    val colorRes = if (isCompleted) R.color.md_theme_primary else R.color.md_theme_inverseSurface
                                    textView?.setTextColor(ContextCompat.getColor(context, colorRes))
                                }
                            } catch (e: Exception) {
                                Log.e("Stepper", "Error al actualizar stepper", e)
                            }
                        }

                        // Validar estado de la incidencia
                        if (incidencia.estado.isNullOrEmpty()) {
                            Toast.makeText(context, "Estado no disponible", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        // Aplicar cambios visuales según el estado actual
                        actualizarStepper(incidencia.estado)

                        // Mostrar el diálogo
                        MaterialAlertDialogBuilder(context)
                            .setTitle("Estado de la Incidencia")
                            .setView(dialogView)
                            .setPositiveButton("Cerrar", null)
                            .show()

                    } catch (e: Exception) {
                        Log.e("StepperDialog", "Error al mostrar el diálogo", e)
                        Toast.makeText(context, "Error al mostrar el estado", Toast.LENGTH_SHORT).show()
                    }
                }


                // Set status color
                val statusColor = when (incidencia.estado) {
                    "Revisado" -> R.color.Green
                    "Notificado" ->R.color.color_blue
                    "Citado"-> R.color.md_theme_secondary
                    "Completado"->R.color.md_theme_primary
                    else -> R.color.color_red
                }
                tvEstado.setTextColor(context.getColor(statusColor))
                // Set severity color and icon
                val (severityColor, severityIcon) = when (incidencia.atencion) {
                    "Moderado" -> Pair(R.color.Primary_green, R.drawable.ic_type_indicidencia)
                    "Urgente" -> Pair(R.color.color_orange, R.drawable.ic_type_indicidencia)
                    "Muy urgente" -> Pair(R.color.color_red, R.drawable.ic_type_indicidencia)
                    else -> Pair(R.color.color_blue, R.drawable.icon_incidencia_positiva) // incidencia positiva
                }
                if (incidencia.atencion.isBlank()) {
                    tvAtencion.text = "Destacado" // incidencia positiva
                } else {
                    tvAtencion.text = incidencia.atencion
                }
                tvAtencion.setTextColor(context.getColor(severityColor))
                tvImagenGravedad.setColorFilter(context.getColor(severityColor))
                tvImagenGravedad.setImageResource(severityIcon)

                // Establecer progreso según estado
                val progresoMap = mapOf(
                    "Revisado" to 25f,
                    "Notificado" to 50f,
                    "Citado" to 75f,
                    "Completado" to 100f
                )

                val progreso = progresoMap[incidencia.estado] ?: 0f

                binding.circularProgressBar.setProgressWithAnimation(progreso, 1000) // duración opcional
                binding.tvProgressText.text = "${progreso.toInt()}%"



                // Set click listener
                root.setOnClickListener {
                    launchDetailActivity(incidencia)
                }
            }
        }

        private fun launchDetailActivity(incidencia: IncidenciaClass) {
            Intent(context, DescripcionIncidencia::class.java).apply {
                putExtras(bundleOf(
                    "INCIDENCIA_ID" to incidencia.id,
                    "INCIDENCIA_FECHA" to incidencia.fecha,
                    "INCIDENCIA_HORA" to incidencia.hora,
                    "INCIDENCIA_NOMBRE" to incidencia.nombreEstudiante,
                    "INCIDENCIA_APELLIDO" to incidencia.apellidoEstudiante,
                    "INCIDENCIA_GRADO" to incidencia.grado,
                    "INCIDENCIA_SECCION" to incidencia.seccion,
                    "INCIDENCIA_TIPO" to incidencia.tipo,
                    "INCIDENCIA_ATENCION" to incidencia.atencion,
                    "INCIDENCIA_ESTADO" to incidencia.estado,
                    "INCIDENCIA_DETALLE" to incidencia.detalle
                ).apply {
                    incidencia.imageUri?.let { uri ->
                        putString("INCIDENCIA_FOTO_URL", uri)
                    }
                })
                context.startActivity(this)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncidenciaViewHolder {
        val binding = ItemIncidenciaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return IncidenciaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IncidenciaViewHolder, position: Int) {
        holder.bind(incidencias[position])
    }
    // si esta en completado formar el informe en pdf tanto para el profesor como al tutor

    override fun getItemCount(): Int = incidencias.size

    fun updateData(newIncidencias: List<IncidenciaClass>) {
        incidencias = newIncidencias
        notifyDataSetChanged()
    }

    companion object {
        // Add this if you need to create the adapter from fragments
        fun newInstance(incidencias: List<IncidenciaClass>, context: Context) =
            IncidenciaAdapter(incidencias, context)
    }
}