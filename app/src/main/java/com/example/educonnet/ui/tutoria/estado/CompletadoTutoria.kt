package com.example.educonnet.ui.tutoria.estado

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import androidx.recyclerview.widget.RecyclerView
import com.example.educonnet.databinding.FragmentCompletadoTutoriaBinding


class CompletadoTutoria : BaseTutoriaFragment() {

    private var _binding: FragmentCompletadoTutoriaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCompletadoTutoriaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getRecyclerView(): RecyclerView = binding.recyclerViewTutorias

    override fun getSpinner(): Spinner = binding.spinnerFecha

    override fun getEstadoFiltro(): String = "Completado"

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
