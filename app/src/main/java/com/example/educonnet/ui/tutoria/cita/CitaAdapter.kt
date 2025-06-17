package com.example.educonnet.ui.tutoria.cita

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.educonnet.R
import com.example.educonnet.databinding.ItemCitaBinding
import com.example.educonnet.ui.tutoria.recurso.Cita
import com.google.android.material.chip.Chip // Importar Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.ParseException // Importar ParseException
import java.text.SimpleDateFormat
import java.util.*

class CitaAdapter : RecyclerView.Adapter<CitaAdapter.CitaViewHolder>() {

    private var items: List<Cita> = emptyList()
    private var onItemClickListener: ((Cita) -> Unit)? = null

    fun setOnItemClickListener(listener: (Cita) -> Unit) {
        onItemClickListener = listener
    }

    inner class CitaViewHolder(private val binding: ItemCitaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Cita) {
            with(binding) {
                tvCreateDate.text = formatCreateDate(item.createFecha)
                tvApoderado.text = item.apoderado

                // Llama a la nueva función para establecer el estado del chip de fecha/hora
                setChipFechaHoraStatus(item.fechaCita, item.hora)

                chipVerDetalles.setOnClickListener {
                    showEnhancedDetailsDialog(item)
                }

                root.setOnClickListener {
                    onItemClickListener?.invoke(item)
                }
            }
        }

        /**
         * Muestra un diálogo de detalles mejorado con formato profesional
         */
        private fun showEnhancedDetailsDialog(cita: Cita) {
            val context = binding.root.context
            val dialogView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_cita_detalle, null)

            val dialog = MaterialAlertDialogBuilder(context)
                .setTitle("Detalles de la Cita")
                .setView(dialogView)
                .setPositiveButton("Cerrar", null)
                .create()

            // Configurar los datos en la vista personalizada
            dialogView.apply {
                findViewById<TextView>(R.id.tvApoderado).text = cita.apoderado
                findViewById<TextView>(R.id.tvParentesco).text = cita.parentesco
                findViewById<TextView>(R.id.tvFecha).text = formatAppointmentDate(cita.fechaCita)
                findViewById<TextView>(R.id.tvHora).text = formatAppointmentTime(cita.hora)
                findViewById<TextView>(R.id.tvMotivo).text = formatMotivo(cita.detalle)

            }

            dialog.show()

            // Personalizar botón positivo
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(
                ContextCompat.getColor(context, R.color.md_theme_primary)
            )
        }

        private fun formatMotivo(detalle: String?): String {
            return detalle?.takeIf { it.isNotBlank() } ?: "No se especificó motivo"
        }

        private fun formatCreateDate(createDateString: String?): String {
            return try {
                val inputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val outputFormat = SimpleDateFormat("d 'de' MMMM 'de' yyyy, hh:mm a", Locale("es", "ES"))
                val date = inputFormat.parse(createDateString ?: return binding.root.context.getString(R.string.fecha_no_disponible))
                outputFormat.format(date!!)
            } catch (e: Exception) {
                binding.root.context.getString(R.string.fecha_no_disponible)
            }
        }

        private fun formatAppointmentDate(dateString: String?): String {
            return try {
                val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val outputFormat = SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", Locale("es", "ES"))
                val date = inputFormat.parse(dateString ?: return binding.root.context.getString(R.string.fecha_no_disponible))
                outputFormat.format(date!!).replaceFirstChar { it.titlecase() }
            } catch (e: Exception) {
                binding.root.context.getString(R.string.fecha_no_disponible)
            }
        }

        private fun formatAppointmentTime(timeString: String?): String {
            return try {
                // Formato de entrada esperado: "05:00 p. m." o similar
                val cleanedTime = timeString?.replace("a. m.", "AM")?.replace("p. m.", "PM") ?:
                return binding.root.context.getString(R.string.hora_no_disponible)

                val inputFormat = SimpleDateFormat("hh:mm a", Locale("es", "ES"))
                val outputFormat = SimpleDateFormat("hh:mm a", Locale("es", "ES"))
                val time = inputFormat.parse(cleanedTime) ?:
                return binding.root.context.getString(R.string.hora_no_disponible)

                outputFormat.format(time)
            } catch (e: Exception) {
                // Si falla el primer intento, probar con formato de 24 horas
                try {
                    val inputFormat24 = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("hh:mm a", Locale("es", "ES"))
                    val time = inputFormat24.parse(timeString ?:
                    return binding.root.context.getString(R.string.hora_no_disponible))
                    outputFormat.format(time!!)
                } catch (e2: Exception) {
                    binding.root.context.getString(R.string.hora_no_disponible)
                }
            }
        }

        private fun setChipFechaHoraStatus(fechaCitaString: String?, horaCitaString: String?) {
            val chip = binding.chipFechaHora
            val context = chip.context

            // Formato para la fecha de la cita (dd/MM/yyyy)
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            // Formato para la hora de la cita (hh:mm a) - manejar "a. m." y "p. m."
            val timeFormat = SimpleDateFormat("hh:mm a", Locale("es", "ES"))
            // Formato para comparar solo el día (ignorar la hora)
            val dayMonthYearFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            try {
                // Paso 1: Parsear la fecha y hora de la cita
                val fechaCitaParsed = fechaCitaString?.let { dateFormat.parse(it) }
                // Limpiar la cadena de hora para que el SimpleDateFormat la entienda
                val cleanedHoraCita = horaCitaString?.replace("a. m.", "AM")?.replace("p. m.", "PM")
                val horaCitaParsed = cleanedHoraCita?.let { timeFormat.parse(it) }

                if (fechaCitaParsed == null || horaCitaParsed == null) {
                    chip.text = "Fecha/Hora Inválida"
                    chip.chipBackgroundColor = ContextCompat.getColorStateList(context, R.color.md_theme_error) // Rojo error
                    chip.setTextColor(ContextCompat.getColor(context, R.color.white))
                    return
                }

                // Combinar fecha y hora para obtener un objeto Date completo de la cita
                val combinedCalendar = Calendar.getInstance()
                combinedCalendar.time = fechaCitaParsed // Establece la fecha
                val horaCal = Calendar.getInstance().apply { time = horaCitaParsed }
                combinedCalendar.set(Calendar.HOUR_OF_DAY, horaCal.get(Calendar.HOUR_OF_DAY))
                combinedCalendar.set(Calendar.MINUTE, horaCal.get(Calendar.MINUTE))
                combinedCalendar.set(Calendar.SECOND, 0)
                combinedCalendar.set(Calendar.MILLISECOND, 0)

                val fechaHoraCita = combinedCalendar.time
                val fechaHoraActual = Calendar.getInstance().apply {
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time // Fecha y hora actual, sin segundos/milisegundos para una comparación precisa

                // Paso 2: Comparar fechas
                val today = dayMonthYearFormat.format(fechaHoraActual)
                val appointmentDay = dayMonthYearFormat.format(fechaHoraCita)

                if (today == appointmentDay) {
                    // Es hoy
                    if (fechaHoraCita.after(fechaHoraActual)) {
                        chip.text = "Hoy (${SimpleDateFormat("hh:mm a", Locale("es", "ES")).format(fechaHoraCita)})"
                        chip.chipBackgroundColor = ContextCompat.getColorStateList(context, R.color.md_theme_tertiaryContainer) // Color para HOY (e.g., azul claro)
                        chip.setTextColor(ContextCompat.getColor(context, R.color.md_theme_onTertiaryContainer))
                        chip.chipStrokeColor = ContextCompat.getColorStateList(context, R.color.md_theme_tertiary)
                    } else {
                        // Es hoy, pero la hora ya pasó
                        chip.text = "Vencida Hoy (${SimpleDateFormat("hh:mm a", Locale("es", "ES")).format(fechaHoraCita)})"
                        chip.chipBackgroundColor = ContextCompat.getColorStateList(context, R.color.md_theme_errorContainer) // Rojo más suave para vencida hoy
                        chip.setTextColor(ContextCompat.getColor(context, R.color.md_theme_onErrorContainer))
                        chip.chipStrokeColor = ContextCompat.getColorStateList(context, R.color.md_theme_error)
                    }
                } else if (fechaHoraCita.before(fechaHoraActual)) {
                    // La fecha de la cita ya pasó
                    chip.text = "Vencida"
                    chip.chipBackgroundColor = ContextCompat.getColorStateList(context, R.color.md_theme_error) // Rojo fuerte para vencida
                    chip.setTextColor(ContextCompat.getColor(context, R.color.white))
                    chip.chipStrokeColor = ContextCompat.getColorStateList(context, R.color.md_theme_onError)
                } else {
                    // La fecha de la cita es futura
                    chip.text = "Vigente"
                    chip.chipBackgroundColor = ContextCompat.getColorStateList(context, R.color.md_theme_primaryContainer) // Verde/azul para vigente
                    chip.setTextColor(ContextCompat.getColor(context, R.color.md_theme_onPrimaryContainer))
                    chip.chipStrokeColor = ContextCompat.getColorStateList(context, R.color.md_theme_primary)
                }

                // Resetear el tint del ícono si no se usa
                chip.chipIconTint = null

            } catch (e: ParseException) {
                // Si hay un error al parsear la fecha/hora
                chip.text = "Error de Fecha"
                chip.chipBackgroundColor = ContextCompat.getColorStateList(context, R.color.md_theme_surfaceVariant) // Gris para error
                chip.setTextColor(ContextCompat.getColor(context, R.color.md_theme_onSurfaceVariant))
                chip.chipStrokeColor = ContextCompat.getColorStateList(context, R.color.md_theme_outline)
                e.printStackTrace()
            } catch (e: Exception) {
                // Otros posibles errores
                chip.text = "Error Desconocido"
                chip.chipBackgroundColor = ContextCompat.getColorStateList(context, R.color.md_theme_surfaceVariant) // Gris para error
                chip.setTextColor(ContextCompat.getColor(context, R.color.md_theme_onSurfaceVariant))
                chip.chipStrokeColor = ContextCompat.getColorStateList(context, R.color.md_theme_outline)
                e.printStackTrace()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitaViewHolder {
        val binding = ItemCitaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CitaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CitaViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<Cita>) {
        items = newItems
        notifyDataSetChanged()
    }

    fun filter(query: String?, originalList: List<Cita>) {
        val filteredList = if (query.isNullOrEmpty()) {
            originalList
        } else {
            originalList.filter {
                it.apoderado.contains(query, true) ||
                        it.parentesco.contains(query, true) ||
                        it.detalle?.contains(query, true) ?: false
            }
        }
        updateData(filteredList)
    }
}