package com.example.educonnet.ui.incidencia.estado


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.educonnet.R


class Revisado : Fragment() {
    private lateinit var recyclerViewIncidencia: RecyclerView
    private lateinit var incidenciaAdapter: IncidenciaAdapter
    private lateinit var searchView: SearchView
    private lateinit var progressBar: ProgressBar
    private var incidenciasRevisado: MutableList<IncidenciaClass> = mutableListOf()
    private var incidenciasFilter: MutableList<IncidenciaClass> = mutableListOf()
    private lateinit var incidenciaViewModel: IncidenciaViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view= inflater.inflate(R.layout.fragment_revisado, container, false)
        incidenciaViewModel = ViewModelProvider(requireParentFragment()).get(IncidenciaViewModel::class.java)
        init(view)
        loadAllIncidencias()
        setupSearchView()
        return view
    }

    private fun init(view: View) {
        recyclerViewIncidencia = view.findViewById(R.id.recyclerViewIncidencia)
        recyclerViewIncidencia.layoutManager = LinearLayoutManager(context)
        searchView = view.findViewById(R.id.searchView)
        incidenciaAdapter = IncidenciaAdapter(incidenciasRevisado, requireContext())
        recyclerViewIncidencia.adapter = incidenciaAdapter
    }
    private fun loadAllIncidencias() {
        incidenciaViewModel.filtrarIncidenciasPorEstado("Revisado")
        incidenciaViewModel.incidenciasFiltradasLiveData.observe(viewLifecycleOwner) { incidencias ->
            incidenciasRevisado.clear()
            incidenciasRevisado.addAll(incidencias) // Agregar los datos cargados
            incidenciaAdapter.updateData(incidenciasRevisado) // Actualizar la vista

        }

    }
    private fun setupSearchView() {
        searchView.setOnClickListener {
            searchView.isIconified = false
            searchView.requestFocus()
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { filterEstudiante(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { filterEstudiante(it) }
                return true
            }
        })
    }

    private fun filterEstudiante(query: String) {
        val queryWords = query.lowercase().split("\\s+".toRegex())

        incidenciasFilter.clear()

        if (query.isNotEmpty()) {
            incidenciasFilter.addAll(
                incidenciasRevisado.filter { incidencia ->
                    val nombreCompleto =
                        "${incidencia.nombreEstudiante} ${incidencia.apellidoEstudiante}".lowercase()
                    queryWords.all { nombreCompleto.contains(it) }
                }
            )
        } else {
            incidenciasFilter.addAll(incidenciasRevisado)  // Mostrar todos los elementos si la búsqueda está vacía
        }

        incidenciaAdapter.updateData(incidenciasFilter)
    }
    override fun onResume() {
        super.onResume()
        loadAllIncidencias()  // Recargar los datos cuando el fragmento se reanuda
        clearSearchView()    // Limpiar el texto de la búsqueda
    }

    private fun clearSearchView() {
        searchView.setQuery("", false)  // Limpiar el texto de búsqueda sin disparar la búsqueda
        searchView.clearFocus()         // Quitar el foco del SearchView
    }

}