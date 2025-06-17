package com.example.educonnet.ui.incidencia.estado

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.educonnet.R


abstract class BaseIncidenciaFragment : Fragment() {

    // Views
    protected lateinit var recyclerViewIncidencia: RecyclerView
    protected lateinit var incidenciaAdapter: IncidenciaAdapter
    protected lateinit var searchView: SearchView
    protected lateinit var progressBar: ProgressBar

    // ViewModel
    protected lateinit var incidenciaViewModel: IncidenciaViewModel

    // Data
    private var listaCompleta: List<IncidenciaClass> = emptyList()
    private var listaFiltrada: List<IncidenciaClass> = emptyList()
    private var isSearching = false

    // Pagination control
    private var cantidadVisible = INITIAL_VISIBLE_ITEMS
    private var isLoading = false
    private var isLastPage = false

    // Abstract properties
    abstract val estado: String
    abstract val recyclerViewId: Int
    abstract val layoutId: Int
    abstract val progressBarId: Int

    companion object {
        private const val INITIAL_VISIBLE_ITEMS = 15
        private const val LOAD_MORE_ITEMS = 15
        private const val LOAD_DELAY_MS = 300L
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(layoutId, container, false)
        initViewModel()
        initViews(view)
        setupObservers()
        setupSearchView()
        return view
    }

    private fun initViewModel() {
        incidenciaViewModel = ViewModelProvider(requireParentFragment()).get(IncidenciaViewModel::class.java)
    }

    private fun initViews(view: View) {
        with(view) {
            recyclerViewIncidencia = findViewById(recyclerViewId)
            searchView = findViewById<androidx.appcompat.widget.SearchView>(R.id.searchView)
            progressBar = findViewById(progressBarId)
        }
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        recyclerViewIncidencia.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = IncidenciaAdapter(mutableListOf(), requireContext()).also {
                incidenciaAdapter = it
            }
            addOnScrollListener(createScrollListener())
        }
    }

    private fun createScrollListener() = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (dy <= 0 || isSearching || isLastPage) return

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
            val totalItemCount = layoutManager.itemCount

            if (!isLoading && lastVisibleItem + 1 >= totalItemCount) {
                loadMoreItems()
            }
        }
    }

    private fun setupObservers() {
        incidenciaViewModel.incidenciasFiltradasLiveData.observe(viewLifecycleOwner) { data ->
            if (data.isNotEmpty()) {
                listaCompleta = if (estado == "Todos") {
                    data
                } else {
                    data.filter { it.estado == estado }
                }
                resetPagination()
                actualizarIncidenciasPaginadas()
            } else {
                incidenciaAdapter.updateData(mutableListOf())
            }
            progressBar.isVisible = false
        }
    }

    private fun resetPagination() {
        cantidadVisible = INITIAL_VISIBLE_ITEMS
        isSearching = false
        isLastPage = false
    }

    private fun loadMoreItems() {
        if (cantidadVisible >= listaCompleta.size) {
            isLastPage = true
            return
        }

        isLoading = true
        progressBar.isVisible = true

        recyclerViewIncidencia.postDelayed({
            cantidadVisible = (cantidadVisible + LOAD_MORE_ITEMS).coerceAtMost(listaCompleta.size)
            actualizarIncidenciasPaginadas()
            isLoading = false
            progressBar.isVisible = false
            isLastPage = cantidadVisible >= listaCompleta.size
        }, LOAD_DELAY_MS)
    }

    private fun actualizarIncidenciasPaginadas() {
        val datosAMostrar = if (isSearching) {
            listaFiltrada
        } else {
            listaCompleta.take(cantidadVisible)
        }
        incidenciaAdapter.updateData(datosAMostrar.toMutableList())
    }

    private fun setupSearchView() {
        // Configurar el icono de búsqueda para expandir el SearchView
        searchView.setOnSearchClickListener {
            isSearching = true
        }

        // Configurar el SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterEstudiante(newText ?: "")
                return true
            }
        })

        // Manejar el cierre del SearchView
        searchView.setOnCloseListener {
            if (searchView.query.isNotEmpty()) {
                searchView.setQuery("", false)
                resetPagination()
                actualizarIncidenciasPaginadas()
            }
            true
        }
    }

    private fun filterEstudiante(query: String) {
        isSearching = query.isNotEmpty()

        if (!isSearching) {
            resetPagination()
            actualizarIncidenciasPaginadas()
            return
        }

        val queryWords = query.lowercase().split("\\s+".toRegex())
        listaFiltrada = listaCompleta.filter {
            "${it.nombreEstudiante} ${it.apellidoEstudiante}".lowercase()
                .let { nombreCompleto -> queryWords.all { word -> nombreCompleto.contains(word) } }
        }

        incidenciaAdapter.updateData(listaFiltrada.toMutableList())
    }

    protected fun loadAllIncidencias() {
        if (listaCompleta.isEmpty()) {
            progressBar.isVisible = true
        }
        incidenciaViewModel.filtrarIncidenciasPorEstado(estado)
    }

    override fun onResume() {
        super.onResume()
        loadAllIncidencias()
        clearSearchView()
    }

    private fun clearSearchView() {
        if (searchView.query.isNotEmpty()) {
            searchView.setQuery("", false)
            resetPagination()
            actualizarIncidenciasPaginadas()
        }
        searchView.clearFocus()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recyclerViewIncidencia.clearOnScrollListeners()
    }
}