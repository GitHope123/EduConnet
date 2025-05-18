package com.example.educonnet.ui.incidencia.estado

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.RecyclerView
import com.example.educonnet.R
import com.example.educonnet.databinding.ItemIncidenciaBinding
import com.example.educonnet.ui.incidencia.DescripcionIncidencia

class IncidenciaAdapter(
    private var incidencias: List<IncidenciaClass>,
    private val context: Context
) : RecyclerView.Adapter<IncidenciaAdapter.IncidenciaViewHolder>() {

    inner class IncidenciaViewHolder(private val binding: ItemIncidenciaBinding) :
        RecyclerView.ViewHolder(binding.root) {

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
                tvNivel.text = incidencia.nivel
                tvTipo.text = incidencia.tipo

                // Set status color
                val statusColor = when (incidencia.estado) {
                    "Revisado" -> R.color.Green
                    else -> R.color.color_red
                }
                tvEstado.setTextColor(context.getColor(statusColor))

                // Set severity color and icon
                val (severityColor, severityIcon) = when (incidencia.atencion) {
                    "Moderado" -> Pair(R.color.Primary_green, R.drawable.ic_type_indicidencia)
                    "Urgente" -> Pair(R.color.color_orange, R.drawable.ic_type_indicidencia)
                    else -> Pair(R.color.color_red, R.drawable.ic_type_indicidencia)
                }
                tvAtencion.setTextColor(context.getColor(severityColor))
                tvImagenGravedad.setColorFilter(context.getColor(severityColor))
                tvImagenGravedad.setImageResource(severityIcon)

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
                    "INCIDENCIA_SECCION" to incidencia.nivel,
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