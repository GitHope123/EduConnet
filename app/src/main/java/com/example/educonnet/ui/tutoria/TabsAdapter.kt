package com.example.educonnet.ui.tutoria

import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.educonnet.R
import com.google.android.material.chip.Chip

class TabsAdapter(
    private val tabs: List<String>,
    private val icons: List<Int>,
    private val onTabSelected: (Int) -> Unit
) : RecyclerView.Adapter<TabsAdapter.TabViewHolder>() {

    private var selectedPosition = 0

    fun updateSelectedPosition(position: Int) {
        val previous = selectedPosition
        selectedPosition = position
        notifyItemChanged(previous)
        notifyItemChanged(position)
    }

    inner class TabViewHolder(private val chip: Chip) : RecyclerView.ViewHolder(chip) {
        fun bind(text: String, icon: Int, position: Int) {
            chip.text = text
            chip.chipIcon = ContextCompat.getDrawable(chip.context, icon)

            val parent = chip.parent
            if (parent is ViewGroup) {
                TransitionManager.beginDelayedTransition(parent)
            }

            val context = chip.context
            val selected = position == selectedPosition

            chip.chipBackgroundColor = ContextCompat.getColorStateList(
                context,
                if (selected) R.color.md_theme_primaryContainer else R.color.white
            )
            chip.setTextColor(
                ContextCompat.getColor(
                    context,
                    if (selected) R.color.white else R.color.md_theme_onSurfaceVariant
                )
            )
            chip.chipIconTint = ContextCompat.getColorStateList(
                context,
                if (selected) R.color.white else R.color.md_theme_onSurfaceVariant
            )
            chip.alpha = if (selected) 1f else 0.7f

            chip.setOnClickListener {
                if (selectedPosition != position) {
                    onTabSelected(position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabViewHolder {
        val chip = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tab, parent, false) as Chip
        return TabViewHolder(chip)
    }

    override fun onBindViewHolder(holder: TabViewHolder, position: Int) {
        holder.bind(tabs[position], icons[position], position)
    }

    override fun getItemCount(): Int = tabs.size
}
