package com.example.educonnet.ui.tutoria.cita

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.educonnet.R
import com.example.educonnet.databinding.ItemCitaBinding
import com.example.educonnet.ui.tutoria.recurso.Cita
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class CitaAdapter : RecyclerView.Adapter<CitaAdapter.CitaViewHolder>() {

    private var items: List<Cita> = emptyList()
    private var onItemClickListener: ((Cita) -> Unit)? = null
    private var onRescheduleListener: ((Cita, String, String) -> Unit)? = null

    fun setOnItemClickListener(listener: (Cita) -> Unit) {
        onItemClickListener = listener
    }

    fun setOnRescheduleListener(listener: (Cita, String, String) -> Unit) {
        onRescheduleListener = listener
    }

    inner class CitaViewHolder(private val binding: ItemCitaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Cita) {
            with(binding) {
                tvCreateDate.text = formatCreateDate(item.createFecha)
                tvApoderado.text = item.apoderado

                // Establece el estado del chip de fecha/hora
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
         * Muestra un diálogo de detalles mejorado con opción de reprogramar
         */
        private fun showEnhancedDetailsDialog(cita: Cita) {
            val context = binding.root.context
            val dialogView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_cita_detalle, null)

            val dialog = MaterialAlertDialogBuilder(context)
                .setTitle("Detalles de la Cita")
                .setView(dialogView)
                .setPositiveButton("Cerrar", null)
                .setNeutralButton("Reprogramar") { _, _ ->
                    showRescheduleDialog(cita)
                }
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

            // Personalizar colores de botones
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(
                ContextCompat.getColor(context, R.color.md_theme_primary)
            )
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL)?.setTextColor(
                ContextCompat.getColor(context, R.color.md_theme_secondary)
            )
        }

        /**
         * Muestra el diálogo para reprogramar la cita
         */
        private fun showRescheduleDialog(cita: Cita) {
            val context = binding.root.context
            val calendar = Calendar.getInstance()

            var selectedDate = ""
            var selectedTime = ""

            // Selector de fecha
            val datePickerDialog = DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    selectedDate = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)

                    // Después de seleccionar la fecha, mostrar selector de hora
                    val timePickerDialog = TimePickerDialog(
                        context,
                        { _, hourOfDay, minute ->
                            val timeCalendar = Calendar.getInstance().apply {
                                set(Calendar.HOUR_OF_DAY, hourOfDay)
                                set(Calendar.MINUTE, minute)
                            }
                            val timeFormat = SimpleDateFormat("hh:mm a", Locale("es", "ES"))
                            selectedTime = timeFormat.format(timeCalendar.time)

                            // Confirmar reprogramación
                            confirmReschedule(cita, selectedDate, selectedTime)
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        false
                    )
                    timePickerDialog.show()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            // No permitir fechas pasadas
            datePickerDialog.datePicker.minDate = System.currentTimeMillis()
            datePickerDialog.show()
        }

        /**
         * Confirma la reprogramación de la cita
         */
        private fun confirmReschedule(cita: Cita, newDate: String, newTime: String) {
            val context = binding.root.context

            MaterialAlertDialogBuilder(context)
                .setTitle("Confirmar Reprogramación")
                .setMessage("¿Desea reprogramar la cita para el $newDate a las $newTime?")
                .setPositiveButton("Confirmar") { _, _ ->
                    onRescheduleListener?.invoke(cita, newDate, newTime)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        private fun formatMotivo(detalle: String?): String {
            return detalle?.takeIf { it.isNotBlank() } ?: "No se especificó motivo"
        }

        private fun formatCreateDate(createDateString: String?): String {
            return try {
                val inputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val outputFormat = SimpleDateFormat("d 'de' MMMM 'de' yyyy, hh:mm a", Locale("es", "ES"))
                val date = inputFormat.parse(createDateString ?: return "Fecha no disponible")
                outputFormat.format(date!!)
            } catch (e: Exception) {
                "Fecha no disponible"
            }
        }

        private fun formatAppointmentDate(dateString: String?): String {
            return try {
                val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val outputFormat = SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", Locale("es", "ES"))
                val date = inputFormat.parse(dateString ?: return "Fecha no disponible")
                outputFormat.format(date!!).replaceFirstChar { it.titlecase() }
            } catch (e: Exception) {
                "Fecha no disponible"
            }
        }

        private fun formatAppointmentTime(timeString: String?): String {
            return try {
                val cleanedTime = timeString?.replace("a. m.", "AM")?.replace("p. m.", "PM")
                    ?: return "Hora no disponible"

                val inputFormat = SimpleDateFormat("hh:mm a", Locale("es", "ES"))
                val outputFormat = SimpleDateFormat("hh:mm a", Locale("es", "ES"))
                val time = inputFormat.parse(cleanedTime) ?: return "Hora no disponible"

                outputFormat.format(time)
            } catch (e: Exception) {
                try {
                    val inputFormat24 = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("hh:mm a", Locale("es", "ES"))
                    val time = inputFormat24.parse(timeString ?: return "Hora no disponible")
                    outputFormat.format(time!!)
                } catch (e2: Exception) {
                    "Hora no disponible"
                }
            }
        }

        private fun setChipFechaHoraStatus(fechaCitaString: String?, horaCitaString: String?) {
            val chip = binding.chipFechaHora
            val context = chip.context

            try {
                // Obtener fecha y hora actual con precisión mejorada
                val currentCalendar = Calendar.getInstance().apply {
                    // Mantener la hora actual completa para comparaciones precisas
                    // Solo resetear segundos y milisegundos para comparaciones justas
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val fechaHoraActual = currentCalendar.time

                // Parsear fecha de la cita
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val fechaCitaParsed = fechaCitaString?.let { dateFormat.parse(it) }
                    ?: run {
                        setChipStyle(chip, "Fecha Inválida", R.color.md_theme_error, R.color.white, R.color.md_theme_error)
                        return
                    }

                // Parsear hora de la cita con mejor manejo de formatos
                val cleanedHoraCita = horaCitaString?.replace("a. m.", "AM")?.replace("p. m.", "PM")
                val horaCitaParsed = try {
                    // Intentar con formato 12 horas primero
                    val timeFormat12 = SimpleDateFormat("hh:mm a", Locale("es", "ES"))
                    cleanedHoraCita?.let { timeFormat12.parse(it) }
                } catch (e: Exception) {
                    try {
                        // Intentar con formato 24 horas
                        val timeFormat24 = SimpleDateFormat("HH:mm", Locale.getDefault())
                        horaCitaString?.let { timeFormat24.parse(it) }
                    } catch (e2: Exception) {
                        null
                    }
                } ?: run {
                    setChipStyle(chip, "Hora Inválida", R.color.md_theme_error, R.color.white, R.color.md_theme_error)
                    return
                }

                // Combinar fecha y hora de la cita con mayor precisión
                val citaCalendar = Calendar.getInstance().apply {
                    time = fechaCitaParsed
                    val horaCal = Calendar.getInstance().apply { time = horaCitaParsed }
                    set(Calendar.HOUR_OF_DAY, horaCal.get(Calendar.HOUR_OF_DAY))
                    set(Calendar.MINUTE, horaCal.get(Calendar.MINUTE))
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val fechaHoraCita = citaCalendar.time

                // Calcular diferencias con mayor precisión
                val diffInMillis = fechaHoraCita.time - fechaHoraActual.time
                val diffInMinutes = diffInMillis / (1000 * 60)
                val diffInHours = diffInMinutes / 60
                val diffInDays = diffInHours / 24

                // Crear calendarios para comparación de solo fechas (sin horas)
                val citaDateOnly = Calendar.getInstance().apply {
                    time = fechaCitaParsed
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                val currentDateOnly = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                val daysDifference = ((citaDateOnly.timeInMillis - currentDateOnly.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()

                when {
                    daysDifference == 0 -> {
                        // Es hoy - verificar si ya pasó la hora
                        val timeString = SimpleDateFormat("hh:mm a", Locale("es", "ES")).format(fechaHoraCita)
                        if (fechaHoraCita.after(fechaHoraActual)) {
                            // Cita pendiente hoy
                            val horasRestantes = kotlin.math.ceil(diffInHours.toDouble()).toInt()
                            val minutosRestantes = (diffInMinutes % 60).toInt()

                            when {
                                diffInMinutes <= 30 -> {
                                    setChipStyle(chip, "¡En $minutosRestantes min! ($timeString)",
                                        R.color.md_theme_tertiaryContainer,
                                        R.color.md_theme_onTertiaryContainer,
                                        R.color.md_theme_tertiary)
                                }
                                diffInHours <= 2 -> {
                                    setChipStyle(chip, "En ${horasRestantes}h ($timeString)",
                                        R.color.md_theme_tertiaryContainer,
                                        R.color.md_theme_onTertiaryContainer,
                                        R.color.md_theme_tertiary)
                                }
                                else -> {
                                    setChipStyle(chip, "Hoy ($timeString)",
                                        R.color.md_theme_tertiaryContainer,
                                        R.color.md_theme_onTertiaryContainer,
                                        R.color.md_theme_tertiary)
                                }
                            }
                        } else {
                            // Cita vencida hoy
                            val horasVencidas = kotlin.math.abs(kotlin.math.floor(diffInHours.toDouble())).toInt()
                            val minutosVencidos = kotlin.math.abs(diffInMinutes % 60).toInt()

                            when {
                                kotlin.math.abs(diffInMinutes) <= 15 -> {
                                    setChipStyle(chip, "Recién pasada ($timeString)",
                                        R.color.md_theme_errorContainer,
                                        R.color.md_theme_onErrorContainer,
                                        R.color.md_theme_error)
                                }
                                kotlin.math.abs(diffInHours) <= 1 -> {
                                    setChipStyle(chip, "Vencida hace ${minutosVencidos}min",
                                        R.color.md_theme_errorContainer,
                                        R.color.md_theme_onErrorContainer,
                                        R.color.md_theme_error)
                                }
                                else -> {
                                    setChipStyle(chip, "Vencida hace ${horasVencidas}h",
                                        R.color.md_theme_error,
                                        R.color.white,
                                        R.color.md_theme_onError)
                                }
                            }
                        }
                    }
                    daysDifference < 0 -> {
                        // Fecha pasada
                        val diasVencidos = kotlin.math.abs(daysDifference)
                        when {
                            diasVencidos == 1 -> {
                                setChipStyle(chip, "Vencida ayer",
                                    R.color.md_theme_error,
                                    R.color.white,
                                    R.color.md_theme_onError)
                            }
                            diasVencidos <= 7 -> {
                                setChipStyle(chip, "Vencida ($diasVencidos días)",
                                    R.color.md_theme_error,
                                    R.color.white,
                                    R.color.md_theme_onError)
                            }
                            else -> {
                                setChipStyle(chip, "Vencida",
                                    R.color.md_theme_error,
                                    R.color.white,
                                    R.color.md_theme_onError)
                            }
                        }
                    }
                    daysDifference == 1 -> {
                        // Mañana
                        val timeString = SimpleDateFormat("hh:mm a", Locale("es", "ES")).format(fechaHoraCita)
                        setChipStyle(chip, "Mañana ($timeString)",
                            R.color.md_theme_primaryContainer,
                            R.color.md_theme_onPrimaryContainer,
                            R.color.md_theme_primary)
                    }
                    daysDifference in 2..7 -> {
                        // Esta semana
                        val dayName = SimpleDateFormat("EEEE", Locale("es", "ES")).format(fechaHoraCita)
                            .replaceFirstChar { it.titlecase() }
                        setChipStyle(chip, "$dayName ($daysDifference días)",
                            R.color.md_theme_primaryContainer,
                            R.color.md_theme_onPrimaryContainer,
                            R.color.md_theme_primary)
                    }
                    daysDifference in 8..30 -> {
                        // Este mes
                        setChipStyle(chip, "En $daysDifference días",
                            R.color.md_theme_primaryContainer,
                            R.color.md_theme_onPrimaryContainer,
                            R.color.md_theme_primary)
                    }
                    else -> {
                        // Más de un mes
                        setChipStyle(chip, "Programada",
                            R.color.md_theme_primaryContainer,
                            R.color.md_theme_onPrimaryContainer,
                            R.color.md_theme_primary)
                    }
                }

                chip.chipIconTint = null

            } catch (e: ParseException) {
                setChipStyle(chip, "Error de Formato",
                    R.color.md_theme_surfaceVariant,
                    R.color.md_theme_onSurfaceVariant,
                    R.color.md_theme_outline)
                e.printStackTrace()
            } catch (e: Exception) {
                setChipStyle(chip, "Error Desconocido",
                    R.color.md_theme_surfaceVariant,
                    R.color.md_theme_onSurfaceVariant,
                    R.color.md_theme_outline)
                e.printStackTrace()
            }
        }

        private fun setChipStyle(chip: Chip, text: String, bgColor: Int, textColor: Int, strokeColor: Int) {
            val context = chip.context
            chip.text = text
            chip.chipBackgroundColor = ContextCompat.getColorStateList(context, bgColor)
            chip.setTextColor(ContextCompat.getColor(context, textColor))
            chip.chipStrokeColor = ContextCompat.getColorStateList(context, strokeColor)
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