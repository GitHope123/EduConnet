package com.example.educonnet.ui.incidencia.estado

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class FragmentAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int = 6

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> Todo()
            1 -> Pendiente()
            2 -> Revisado()
            3 -> Citado()
            4 -> Notificado()
            5 -> Completado()
            else -> throw IllegalArgumentException("Invalid position $position")
        }
    }
} 