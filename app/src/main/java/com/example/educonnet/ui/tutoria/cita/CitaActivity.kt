package com.example.educonnet.ui.tutoria.cita

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.educonnet.R
import com.example.educonnet.databinding.ActivityCitaBinding
import com.example.educonnet.ui.tutoria.recurso.Cita
import com.google.android.material.snackbar.Snackbar


class CitaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCitaBinding
    private lateinit var citaAdapter: CitaAdapter
    private lateinit var viewModel: CitaViewModel
    private var currentFilterStatus = CitaViewModel.CitaStatus.ALL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCitaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[CitaViewModel::class.java]

        setupToolbar()
        setupRecyclerView()
        setupSearchView()
        observeViewModel()

        viewModel.loadInitialCitas()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Mis Citas"
        }
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        citaAdapter = CitaAdapter().apply {
            setOnItemClickListener { cita ->
                // Opcional: Manejar click en item
                showItemClickedSnackbar(cita)
            }

            setOnRescheduleListener { cita, nuevaFecha, nuevaHora ->
                viewModel.reprogramarCita(cita, nuevaFecha, nuevaHora)
            }
        }

        binding.citaRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@CitaActivity)
            adapter = citaAdapter
            // Opcional: Agregar animaciones
            itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()
        }
    }

    private fun setupSearchView() {
        binding.searchView.apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    viewModel.searchCitas(newText.orEmpty())
                    return true
                }
            })

            setOnCloseListener {
                viewModel.resetCitas()
                false
            }

            // Personalizar hint
            queryHint = "Buscar por apoderado, parentesco..."
        }
    }



    private fun observeViewModel() {
        // Observar lista de citas filtradas
        viewModel.filteredCitas.observe(this) { citas ->
            citaAdapter.updateData(citas)
        }

        // Observar estado de carga
        viewModel.loadingState.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observar errores
        viewModel.errorState.observe(this) { error ->
            error?.let {
                showErrorSnackbar(it)
            }
        }

        // Observar estado de actualización (reprogramación)
        viewModel.updateState.observe(this) { updateState ->
            when (updateState) {
                is CitaViewModel.UpdateState.Idle -> {
                    // No hacer nada
                }
                is CitaViewModel.UpdateState.Updating -> {
                    showProgressSnackbar("Actualizando cita...")
                }
                is CitaViewModel.UpdateState.Success -> {
                    showSuccessSnackbar("Cita actualizada exitosamente")
                    viewModel.resetUpdateState()
                }
                is CitaViewModel.UpdateState.Error -> {
                    showErrorSnackbar("Error: ${updateState.message}")
                    viewModel.resetUpdateState()
                }
            }
        }
    }

    private fun refreshCitas() {
        viewModel.reloadCitas()
        // Mantener el filtro actual después de recargar
        if (currentFilterStatus != CitaViewModel.CitaStatus.ALL) {
            viewModel.filterByStatus(currentFilterStatus)
        }
    }


    private fun showItemClickedSnackbar(cita: Cita) {
        Snackbar.make(
            binding.root,
            "Cita con ${cita.apoderado} - ${cita.fechaCita}",
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun showErrorSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction("Reintentar") {
                refreshCitas()
            }
            .show()
    }

    private fun showSuccessSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(getColor(R.color.md_theme_primaryContainer))
            .setTextColor(getColor(R.color.md_theme_onPrimaryContainer))
            .show()
    }

    private fun showProgressSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Limpiar estados del ViewModel si es necesario
        viewModel.resetUpdateState()
    }
}