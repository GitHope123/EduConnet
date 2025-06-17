package com.example.educonnet.ui.tutoria.estado

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.educonnet.LoginActivity
import com.example.educonnet.R
import com.example.educonnet.ui.tutoria.TutoriaAdapter
import com.example.educonnet.ui.tutoria.TutoriaClass
import com.example.educonnet.ui.tutoria.TutoriaViewModel
import com.example.educonnet.ui.tutoria.recurso.TutoriaUpdateCallback

abstract class BaseTutoriaFragment : Fragment(), TutoriaUpdateCallback {

    protected lateinit var tutoriaAdapter: TutoriaAdapter
    protected val listaTutorias: MutableList<TutoriaClass> = mutableListOf()
    protected lateinit var tutoriaViewModel: TutoriaViewModel
    protected var currentDateFilter: String = "Todos"
    protected lateinit var idUsuario: String

    abstract fun getRecyclerView(): RecyclerView
    abstract fun getSpinner(): Spinner? // 🔄 CAMBIO: ahora usamos Spinner
    abstract fun getEstadoFiltro(): String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        setupRecyclerView()
        setupSpinner()
        loadInitialData()
    }

    protected open fun setupViewModel() {
        tutoriaViewModel = ViewModelProvider(requireParentFragment()).get(TutoriaViewModel::class.java)
        currentDateFilter = tutoriaViewModel.getCurrentDateFilter()
    }

    protected open fun setupRecyclerView() {
        getRecyclerView().layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        tutoriaAdapter = TutoriaAdapter(listaTutorias, this)
        getRecyclerView().adapter = tutoriaAdapter
    }

    protected open fun setupSpinner() {
        val spinner = getSpinner() ?: return
        val adapter = ArrayAdapter(requireContext(),R.layout.list_tutoria, TutoriaViewModel.DATE_FILTER_OPTIONS)
        adapter.setDropDownViewResource(R.layout.list_tutoria)
        spinner.adapter = adapter

        // Restaurar la selección anterior
        val selectedIndex = TutoriaViewModel.DATE_FILTER_OPTIONS.indexOf(currentDateFilter)
        if (selectedIndex >= 0) spinner.setSelection(selectedIndex)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedItem = parent.getItemAtPosition(position) as String
                setDateFilter(selectedItem)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    protected open fun loadInitialData() {
        idUsuario = LoginActivity.GlobalData.idUsuario
        applyDateFilter(currentDateFilter)
    }

    override fun onResume() {
        super.onResume()
        restoreFilterState()
    }

    private fun restoreFilterState() {
        getSpinner()?.apply {
            val selectedIndex = TutoriaViewModel.DATE_FILTER_OPTIONS.indexOf(currentDateFilter)
            if (selectedIndex >= 0) setSelection(selectedIndex)
        }
        applyDateFilter(currentDateFilter)
    }

    override fun onTutoriaUpdated(tutoriaId: String, nuevoEstado: String) {
        val position = listaTutorias.indexOfFirst { it.id == tutoriaId }
        if (position != -1) {
            listaTutorias[position].estado = nuevoEstado
            tutoriaAdapter.notifyItemChanged(position)
        }
    }

    fun setDateFilter(filter: String) {
        if (currentDateFilter != filter) {
            currentDateFilter = filter
            applyDateFilter(filter)
        }
    }

    protected open fun applyDateFilter(filter: String) {
        currentDateFilter = filter
        loadTutorias()
    }

    protected open fun loadTutorias() {
        tutoriaViewModel.filtrarIncidenciasPorEstado(getEstadoFiltro(), currentDateFilter)

        tutoriaViewModel.incidenciasFiltradasLiveData.removeObservers(viewLifecycleOwner)
        tutoriaViewModel.incidenciasFiltradasLiveData.observe(viewLifecycleOwner, Observer { incidencias ->
            listaTutorias.clear()
            listaTutorias.addAll(incidencias)
            tutoriaAdapter.notifyDataSetChanged()
        })
    }
}
