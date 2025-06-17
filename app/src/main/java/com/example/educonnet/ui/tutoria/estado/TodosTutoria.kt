package com.example.educonnet.ui.tutoria.estado

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import com.example.educonnet.databinding.FragmentTodosTutoriaBinding

class TodosTutoria : BaseTutoriaFragment() {
    private var _binding: FragmentTodosTutoriaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTodosTutoriaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getRecyclerView() = binding.recyclerViewTutorias
    override fun getSpinner(): Spinner = binding.spinnerFecha
    override fun getEstadoFiltro() = ""

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}