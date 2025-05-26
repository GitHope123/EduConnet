package com.example.educonnet.ui.tutoria.estado

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.educonnet.LoginActivity
import com.example.educonnet.R
import com.example.educonnet.databinding.FragmentTodosTutoriaBinding
import com.example.educonnet.ui.tutoria.TutoriaAdapter
import com.example.educonnet.ui.tutoria.TutoriaClass
import com.example.educonnet.ui.tutoria.TutoriaRepository
import com.example.educonnet.ui.tutoria.TutoriaViewModel
import com.example.educonnet.ui.tutoria.recurso.TutoriaUpdateCallback
import com.google.firebase.auth.FirebaseAuth

class TodosTutoria : Fragment(), TutoriaUpdateCallback {

    private var _binding: FragmentTodosTutoriaBinding? = null
    private val binding get() = _binding!!

    private lateinit var tutoriaAdapter: TutoriaAdapter
    private val listaTutorias: MutableList<TutoriaClass> = mutableListOf()
    private val tutoriaRepository = TutoriaRepository()
    private var currentFilter: String = "Todos"
    private lateinit var idUsuario: String
    private lateinit var tutoriaViewModel: TutoriaViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTodosTutoriaBinding.inflate(inflater, container, false)
        tutoriaViewModel = ViewModelProvider(requireParentFragment()).get(TutoriaViewModel::class.java)

        init()
        resetAutoComplete()

        val email = FirebaseAuth.getInstance().currentUser?.email
        email?.let {
            loadTutoriasForTutor(it, currentFilter) // Filtro por defecto
        }

        return binding.root
    }

    override fun onTutoriaUpdated(tutoriaId: String, nuevoEstado: String) {
        val position = listaTutorias.indexOfFirst { it.id == tutoriaId }
        if (position != -1) {
            listaTutorias[position].estado = nuevoEstado
            tutoriaAdapter.notifyItemChanged(position)
        }
    }

    private fun init() {
        binding.recyclerViewTutorias.layoutManager = LinearLayoutManager(context)
        tutoriaAdapter = TutoriaAdapter(listaTutorias, this)
        binding.recyclerViewTutorias.adapter = tutoriaAdapter
    }

    private fun resetAutoComplete() {
        val fechaTutoria = resources.getStringArray(R.array.item_fecha_tutoria)
        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.list_tutoria, fechaTutoria)
        arrayAdapter.setDropDownViewResource(R.layout.list_tutoria)
        binding.autoComplete.setAdapter(arrayAdapter)
        binding.autoComplete.text.clear()
        binding.autoComplete.setText(currentFilter, false)
        binding.autoComplete.setOnItemClickListener { parent, _, position, _ ->
            val selectedItem = parent.getItemAtPosition(position) as String
            applyFilter(selectedItem)
        }
    }

    private fun applyFilter(selection: String) {
        currentFilter = selection
        idUsuario = LoginActivity.GlobalData.idUsuario
        loadTutoriasForTutor(idUsuario, selection)
    }

    private fun loadTutoriasForTutor(id: String, filtroFecha: String) {
        tutoriaViewModel.filtrarIncidenciasPorEstado("", filtroFecha)

        // Elimina observadores anteriores para evitar múltiples triggers
        tutoriaViewModel.incidenciasFiltradasLiveData.removeObservers(viewLifecycleOwner)

        tutoriaViewModel.incidenciasFiltradasLiveData.observe(viewLifecycleOwner, Observer { incidencias ->
            listaTutorias.clear()
            listaTutorias.addAll(incidencias)
            tutoriaAdapter.updateData(listaTutorias)
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        currentFilter = "Todos"
        resetAutoComplete()
        idUsuario = LoginActivity.GlobalData.idUsuario
        loadTutoriasForTutor(idUsuario, currentFilter)
    }
}
