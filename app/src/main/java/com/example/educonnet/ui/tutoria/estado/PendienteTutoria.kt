package com.example.educonnet.ui.tutoria.estado

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import com.example.educonnet.databinding.FragmentPendienteTutoriaBinding

class PendienteTutoria : BaseTutoriaFragment() {

    private var _binding: FragmentPendienteTutoriaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPendienteTutoriaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getRecyclerView() = binding.recyclerViewTutorias
    override fun getSpinner(): Spinner = binding.spinnerFecha
    override fun getEstadoFiltro() = "Pendiente"

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}