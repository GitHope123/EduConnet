package com.example.educonnet.ui.tutoria.cita

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.educonnet.databinding.ActivityCitaBinding
import com.google.android.material.snackbar.Snackbar

class CitaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCitaBinding
    private lateinit var citaAdapter: CitaAdapter
    private lateinit var viewModel: CitaViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCitaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[CitaViewModel::class.java]

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        setupRecyclerView()
        setupSearchView()
        observeViewModel()

        viewModel.loadInitialCitas()
    }

    private fun setupRecyclerView() {
        citaAdapter = CitaAdapter()
        binding.citaRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@CitaActivity)
            adapter = citaAdapter
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.searchCitas(newText.orEmpty())
                return true
            }
        })

        binding.searchView.setOnCloseListener {
            viewModel.resetCitas()
            false
        }
    }

    private fun observeViewModel() {
        viewModel.filteredCitas.observe(this) { data ->
            citaAdapter.updateData(data)
        }

        viewModel.errorState.observe(this) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            }
        }

        viewModel.loadingState.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }
}
