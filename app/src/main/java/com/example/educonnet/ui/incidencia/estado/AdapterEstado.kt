package com.example.educonnet.ui.incidencia.estado

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.educonnet.R
import com.google.android.material.chip.Chip

class AdapterEstado(private val onChipSelected: (Int) -> Unit) : RecyclerView.Adapter<AdapterEstado.ChipViewHolder>() {
    private var selectedPosition = 0
    private val estados = listOf("Todos", "Pendiente", "Revisado", "Citado", "Notificado", "Completado")
    private val iconos = listOf(
        R.drawable.ic_todos,
        R.drawable.ic_pendiente,
        R.drawable.ic_revisado,
        R.drawable.ic_citado,
        R.drawable.ic_notificado,
        R.drawable.ic_completado
    )

    inner class ChipViewHolder(private val chip: Chip) : RecyclerView.ViewHolder(chip) {
        fun bind(position: Int) {
            chip.apply {
                text = estados[position]
                setChipIconResource(iconos[position])
                isCheckable = true
                
                // Remove previous listener to prevent multiple callbacks
                setOnCheckedChangeListener(null)
                
                // Set the checked state
                isChecked = position == selectedPosition
                
                // Update chip colors based on selection state
                updateChipColors(position == selectedPosition)
                
                // Add the listener after setting the state
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked && position != selectedPosition) {
                        val previousPosition = selectedPosition
                        selectedPosition = position
                        notifyItemChanged(previousPosition)
                        updateChipColors(true)
                        onChipSelected(position)
                    }
                }
            }
        }

        private fun updateChipColors(isSelected: Boolean) {
            val context = chip.context
            if (isSelected) {
                chip.setChipBackgroundColorResource(R.color.md_theme_primaryContainer)
                chip.setTextColor(ContextCompat.getColor(context, R.color.white))
                chip.chipIconTint = ContextCompat.getColorStateList(context, R.color.white)
            } else {
                chip.setChipBackgroundColorResource(R.color.white)
                chip.setTextColor(ContextCompat.getColor(context, R.color.md_theme_dark_surfaceContainerHigh))
                chip.chipIconTint = ContextCompat.getColorStateList(context, R.color.md_theme_dark_surfaceContainerHigh)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChipViewHolder {
        val chip = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chip_estado, parent, false) as Chip
        return ChipViewHolder(chip)
    }

    override fun onBindViewHolder(holder: ChipViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount() = estados.size

    fun updateSelectedPosition(position: Int) {
        if (position != selectedPosition) {
            val previousPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(previousPosition)
            notifyItemChanged(position)
        }
    }
}
