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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.educonnet.R
import com.google.firebase.firestore.FirebaseFirestore

class AgregarEstudiantes : AppCompatActivity() {
    private lateinit var searchViewEstudiante: SearchView
    private lateinit var recyclerViewEstudiantes: RecyclerView
    private lateinit var spinnerGrado: Spinner
    private lateinit var spinnerSeccion: Spinner
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val estudianteList = mutableListOf<EstudianteAgregar>()
    private val filterEstudianteList = mutableListOf<EstudianteAgregar>()
    private lateinit var estudianteAdapter: EstudianteAgregarAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_estudiantes)
        init()
        setupGradoSpinner()
        setupRecyclerView()
        setupSearchView()

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun init() {
        recyclerViewEstudiantes = findViewById(R.id.recyclerViewEstudiantes)
        searchViewEstudiante = findViewById(R.id.searchViewEstudiante)
        spinnerGrado = findViewById(R.id.spinnerGrado)
        spinnerSeccion = findViewById(R.id.spinnerSeccion)
    }

    private fun setupGradoSpinner() {
        val grados = arrayOf("Seleccione", "1", "2", "3", "4", "5")
        val adapterGrados = ArrayAdapter(this, R.layout.item_spinner, grados)
        adapterGrados.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerGrado.adapter = adapterGrados

        spinnerGrado.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val gradoSeleccionado = spinnerGrado.selectedItem.toString()
                setupSeccionSpinner(gradoSeleccionado)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupSeccionSpinner(gradoSeleccionado: String) {
        val secciones = when (gradoSeleccionado) {
            "Seleccione" -> arrayOf("Seleccione")
            "1" -> arrayOf("A", "B", "C", "D", "E")
            else -> arrayOf("A", "B", "C", "D")
        }

        val adapterSecciones = ArrayAdapter(this, R.layout.item_spinner, secciones)
        adapterSecciones.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerSeccion.adapter = adapterSecciones
        spinnerSeccion.isEnabled = gradoSeleccionado != "Seleccione"

        spinnerSeccion.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val grado = spinnerGrado.selectedItem.toString()
                val seccion = spinnerSeccion.selectedItem.toString()
                if (grado != "Seleccione" && seccion != "Seleccione") {
                    fetchEstudiantes(grado, seccion)
                } else {
                    filterEstudianteList.clear()
                    estudianteAdapter.notifyDataSetChanged()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupRecyclerView() {
        estudianteAdapter = EstudianteAgregarAdapter(filterEstudianteList) { estudiante ->
            Intent(this, AgregarIncidencia::class.java).apply {
                putExtra("EXTRA_STUDENT_ID", estudiante.id)
                putExtra("EXTRA_STUDENT_NAME", estudiante.nombres)
                putExtra("EXTRA_STUDENT_LAST_NAME", estudiante.apellidos)
                putExtra("EXTRA_STUDENT_GRADE", estudiante.grado)
                putExtra("EXTRA_STUDENT_SECTION", estudiante.seccion)
                putExtra("EXTRA_STUDENT_CELULAR", estudiante.celularApoderado)
                startActivity(this)
            }
        }
        recyclerViewEstudiantes.apply {
            layoutManager = LinearLayoutManager(this@AgregarEstudiantes)
            adapter = estudianteAdapter
        }
    }

    private fun fetchEstudiantes(grado: String, seccion: String) {
        firestore.collection("Estudiante")
            .whereEqualTo("grado", grado.toInt())
            .whereEqualTo("seccion", seccion)
            .get()
            .addOnSuccessListener { result ->
                estudianteList.clear()
                result.documents.forEach { document ->
                    EstudianteAgregar(
                        id = document.id,
                        nombres = document.getString("nombres") ?: "",
                        apellidos = document.getString("apellidos") ?: "",
                        grado = document.getLong("grado")?.toInt() ?: 0,
                        seccion = document.getString("seccion") ?: "",
                        celularApoderado = document.getLong("celularApoderado")?.toInt() ?: 0
                    ).let { estudianteList.add(it) }
                }
                filterEstudiante(searchViewEstudiante.query.toString())
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar estudiantes: ${e.message}", Toast.LENGTH_SHORT).show()
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
                    newText?.let { filterEstudiante(it) }
                    return true
                }
            })
        }
    }

    private fun filterEstudiante(query: String) {
        val gradoSeleccionado = spinnerGrado.selectedItem?.toString() ?: ""
        val seccionSeleccionada = spinnerSeccion.selectedItem?.toString() ?: ""
        val queryLower = query.lowercase()

        filterEstudianteList.clear()
        estudianteList.filterTo(filterEstudianteList) { estudiante ->
            val coincideGrado = gradoSeleccionado.isEmpty() || estudiante.grado.toString() == gradoSeleccionado
            val coincideSeccion = seccionSeleccionada.isEmpty() || estudiante.seccion == seccionSeleccionada
            val nombreCompleto = "${estudiante.nombres} ${estudiante.apellidos}".lowercase()
            val coincideNombre = queryLower.isEmpty() || nombreCompleto.contains(queryLower)

            coincideNombre && coincideGrado && coincideSeccion
        }.sortedWith { e1, e2 ->
            "${e1.apellidos} ${e1.nombres}".compareTo("${e2.apellidos} ${e2.nombres}")
        }
        estudianteAdapter.notifyDataSetChanged()
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
        filterEstudianteList.clear()
        estudianteAdapter.notifyDataSetChanged()
    }
}