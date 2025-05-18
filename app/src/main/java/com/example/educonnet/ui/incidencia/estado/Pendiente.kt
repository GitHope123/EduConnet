package com.example.educonnet.ui.incidencia.estado

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.educonnet.R


class Pendiente : Fragment() {
    private lateinit var recyclerViewIncidencia: RecyclerView
    private lateinit var incidenciaAdapter: IncidenciaAdapter
    private lateinit var searchView: SearchView
    private var incidenciasPendiente: MutableList<IncidenciaClass> = mutableListOf()
    private var incidenciasFilter: MutableList<IncidenciaClass> = mutableListOf()
    private lateinit var idUsuario:String
    private lateinit var incidenciaViewModel: IncidenciaViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pendiente, container, false)
        incidenciaViewModel = ViewModelProvider(requireParentFragment()).get(IncidenciaViewModel::class.java)
        init(view)
        loadAllIncidencias()
        setupSearchView()
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadAllIncidencias()
    }
    private fun init(view: View) {
        recyclerViewIncidencia = view.findViewById(R.id.recyclerViewIncidenciaPendiente)
        recyclerViewIncidencia.layoutManager = LinearLayoutManager(context)
        searchView = view.findViewById(R.id.searchView)
        incidenciaAdapter = IncidenciaAdapter(incidenciasPendiente, requireContext())
        recyclerViewIncidencia.adapter = incidenciaAdapter
    }
    private fun loadAllIncidencias() {
        incidenciaViewModel.filtrarIncidenciasPorEstado("Pendiente")
        incidenciaViewModel.incidenciasFiltradasLiveData.observe(viewLifecycleOwner) { incidencias ->
            incidenciasPendiente.clear()
            incidenciasPendiente.addAll(incidencias) // Agregar los datos cargados
            incidenciaAdapter.updateData(incidenciasPendiente) // Actualizar la vista
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
                incidenciasPendiente.filter { incidencia ->
                    val nombreCompleto =
                        "${incidencia.nombreEstudiante} ${incidencia.apellidoEstudiante}".lowercase()
                    queryWords.all { nombreCompleto.contains(it) }
                }
            )
        } else {
            incidenciasFilter.addAll(incidenciasPendiente)  // Mostrar todos los elementos si la búsqueda está vacía
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