package com.example.educonnet.ui.incidencia

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.educonnet.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore

class AgregarEstudiantes : AppCompatActivity() {

    private lateinit var searchViewEstudiante: SearchView
    private lateinit var recyclerViewEstudiantes: RecyclerView
    private lateinit var spinnerGrado: Spinner
    private lateinit var spinnerSeccion: Spinner
    private lateinit var btnContinuar: MaterialButton
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val estudiantesDisponibles = mutableListOf<EstudianteAgregar>()
    private val estudiantesFiltrados = mutableListOf<EstudianteAgregar>()
    private val selectedStudents = mutableListOf<EstudianteAgregar>() // Cambiado a lista de objetos completos

    private lateinit var estudianteAdapter: EstudianteAgregarAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_estudiantes)
        initViews()
        setupToolbar()
        setupSpinners()
        setupRecyclerView()
        setupSearchView()
        setupContinuarButton()
    }

    private fun initViews() {
        searchViewEstudiante = findViewById(R.id.searchViewEstudiante)
        recyclerViewEstudiantes = findViewById(R.id.recyclerViewEstudiantes)
        spinnerGrado = findViewById(R.id.spinnerGrado)
        spinnerSeccion = findViewById(R.id.spinnerSeccion)
        btnContinuar = findViewById(R.id.btnContinuar)
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupSpinners() {
        val grados = arrayOf("Seleccione", "1", "2", "3", "4", "5")
        val adapterGrados = ArrayAdapter(this, R.layout.item_spinner, grados)
        adapterGrados.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerGrado.adapter = adapterGrados

        spinnerGrado.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val grado = spinnerGrado.selectedItem.toString()
                setupSeccionSpinner(grado)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupSeccionSpinner(gradoSeleccionado: String) {
        val secciones = when (gradoSeleccionado) {
            "Seleccione" -> arrayOf("Seleccione")
            "1" -> arrayOf("Seleccione", "A", "B", "C", "D", "E")
            else -> arrayOf("Seleccione", "A", "B", "C", "D")
        }

        val adapterSeccion = ArrayAdapter(this, R.layout.item_spinner, secciones)
        adapterSeccion.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerSeccion.adapter = adapterSeccion
        spinnerSeccion.isEnabled = gradoSeleccionado != "Seleccione"

        spinnerSeccion.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val grado = spinnerGrado.selectedItem.toString()
                val seccion = spinnerSeccion.selectedItem.toString()

                if (grado != "Seleccione" && seccion != "Seleccione") {
                    fetchEstudiantes(grado, seccion)
                } else {
                    clearEstudiantes()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun fetchEstudiantes(grado: String, seccion: String) {
        firestore.collection("Estudiante")
            .whereEqualTo("grado", grado.toInt())
            .whereEqualTo("seccion", seccion)
            .get()
            .addOnSuccessListener { result ->
                estudiantesDisponibles.clear()
                estudiantesFiltrados.clear()

                for (doc in result.documents) {
                    val estudiante = EstudianteAgregar(
                        id = doc.id,
                        nombres = doc.getString("nombres") ?: "",
                        apellidos = doc.getString("apellidos") ?: "",
                        grado = doc.getLong("grado")?.toInt() ?: 0,
                        seccion = doc.getString("seccion") ?: "",
                        celularApoderado = doc.getLong("celularApoderado")?.toInt() ?: 0
                    )
                    estudiantesDisponibles.add(estudiante)
                }

                estudiantesFiltrados.addAll(estudiantesDisponibles)
                ordenarEstudiantes()
                estudianteAdapter.updateEstudiantes(estudiantesFiltrados)
                estudianteAdapter.notifyDataSetChanged()
                updateContinuarButtonState()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar estudiantes: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearEstudiantes() {
        estudiantesDisponibles.clear()
        estudiantesFiltrados.clear()
        estudianteAdapter.updateEstudiantes(estudiantesFiltrados)
        estudianteAdapter.notifyDataSetChanged()
        updateContinuarButtonState()
    }

    private fun ordenarEstudiantes() {
        estudiantesFiltrados.sortWith(compareBy({ it.apellidos }, { it.nombres }))
    }

    private fun setupRecyclerView() {
        estudianteAdapter = EstudianteAgregarAdapter(
            estudiantes = estudiantesFiltrados,
            seleccionados = selectedStudents.map { it.id }.toSet(),
            onItemChecked = { estudiante, isChecked ->
                if (isChecked) {
                    if (!selectedStudents.any { it.id == estudiante.id }) {
                        selectedStudents.add(estudiante)
                    }
                } else {
                    selectedStudents.removeAll { it.id == estudiante.id }
                }
                updateContinuarButtonState()
            }
        )

        recyclerViewEstudiantes.apply {
            layoutManager = LinearLayoutManager(this@AgregarEstudiantes)
            adapter = estudianteAdapter
        }
    }

    private fun setupSearchView() {
        searchViewEstudiante.apply {
            setOnClickListener {
                isIconified = false
                requestFocus()
            }

            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?) = true

                override fun onQueryTextChange(newText: String?): Boolean {
                    filtrarEstudiantes(newText.orEmpty())
                    return true
                }
            })
        }
    }

    private fun filtrarEstudiantes(query: String) {
        val lowerQuery = query.lowercase()
        estudiantesFiltrados.clear()

        if (query.isEmpty()) {
            estudiantesFiltrados.addAll(estudiantesDisponibles)
        } else {
            estudiantesDisponibles.filterTo(estudiantesFiltrados) {
                "${it.nombres} ${it.apellidos}".lowercase().contains(lowerQuery)
            }
        }

        ordenarEstudiantes()
        estudianteAdapter.updateEstudiantes(estudiantesFiltrados)
        estudianteAdapter.notifyDataSetChanged()
    }

    private fun updateContinuarButtonState() {
        btnContinuar.isEnabled = selectedStudents.isNotEmpty()
    }

    private fun setupContinuarButton() {
        btnContinuar.setOnClickListener {
            if (selectedStudents.isNotEmpty()) {
                val cantidad = selectedStudents.size
                val mensaje = if (cantidad == 1)
                    "1 estudiante seleccionado"
                else
                    "$cantidad estudiantes seleccionados"

                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()

                val listaNombres = selectedStudents.joinToString("\n") {
                    "- ${it.nombres} ${it.apellidos}"
                }

                MaterialAlertDialogBuilder(this)
                    .setTitle("Confirmar selección")
                    .setMessage("Has seleccionado:\n\n$listaNombres\n\n¿Deseas continuar?")
                    .setPositiveButton("Sí") { _, _ ->
                        val intent = Intent(this, AgregarIncidencia::class.java).apply {
                            putParcelableArrayListExtra("EXTRA_STUDENTS", ArrayList(selectedStudents))
                        }
                        startActivity(intent)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            } else {
                Toast.makeText(this, "Seleccione al menos un estudiante", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun clearSearchView() {
        searchViewEstudiante.setQuery("", false)
        searchViewEstudiante.clearFocus()
    }

    override fun onPause() {
        super.onPause()
        clearSearchView()
    }

    override fun onResume() {
        super.onResume()
        spinnerGrado.setSelection(0)
        spinnerSeccion.setSelection(0)
        updateContinuarButtonState()
    }
}