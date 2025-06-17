package com.example.educonnet.ui.tutoria.estado

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import androidx.recyclerview.widget.RecyclerView
import com.example.educonnet.databinding.FragmentCitadoTutoriaBinding

class CitadoTutoria : BaseTutoriaFragment() {

    private var _binding: FragmentCitadoTutoriaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCitadoTutoriaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getRecyclerView(): RecyclerView = binding.recyclerViewTutorias

    override fun getSpinner(): Spinner = binding.spinnerFecha

    override fun getEstadoFiltro(): String = "Citado"

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
