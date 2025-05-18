package com.example.educonnet.ui.incidencia


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Spinner
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
        updateGrado()
        setupRecyclerView()
        setupSearchView()
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Habilita el botón de retroceso
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Maneja el clic en la flecha de retroceso
        toolbar.setNavigationOnClickListener {
            finish() // Cierra la actividad actual y vuelve a la anterior
        }
    }



    private fun init() {
        recyclerViewEstudiantes = findViewById(R.id.recyclerViewEstudiantes)
        searchViewEstudiante = findViewById(R.id.searchViewEstudiante)
        spinnerGrado = findViewById(R.id.spinnerGrado)
        spinnerSeccion = findViewById(R.id.spinnerSeccion)

    }

    private fun updateGrado() {
        val grados = arrayOf("Selecione", "1", "2", "3", "4", "5","6")
        val adapterGrados = ArrayAdapter(this, R.layout.spinner_item_selected, grados)
        adapterGrados.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGrado.adapter = adapterGrados

        spinnerGrado.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                val gradoSeleccionado = spinnerGrado.selectedItem.toString()
                updateSecciones(gradoSeleccionado)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    private fun updateSecciones(gradoSeleccionado: String) {
        val secciones = when (gradoSeleccionado) {
            "Seleccione" -> arrayOf("Seleccione")
            "1" -> arrayOf("Secundaria")
            "2" -> arrayOf("Secundaria")
            "3" -> arrayOf("Secundaria")
            "4" -> arrayOf("Secundaria")
            "5" -> arrayOf("Primaria","Secundaria")
            "6" -> arrayOf("Primaria")
            else -> arrayOf("Seleccione")
        }
        val adapterSecciones = ArrayAdapter(this, R.layout.spinner_item_selected, secciones)
        adapterSecciones.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSeccion.adapter = adapterSecciones
        spinnerSeccion.isEnabled = gradoSeleccionado != "Selecione"
        spinnerSeccion.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                val gradoSeleccionado = spinnerGrado.selectedItem.toString()
                val seccionSeleccionada = spinnerSeccion.selectedItem.toString()
                if (gradoSeleccionado != "Seleccione" && seccionSeleccionada != "Seleccione") {
                    fetchEstudiantes(gradoSeleccionado, seccionSeleccionada)
                } else {
                    filterEstudianteList.clear()
                    estudianteAdapter.notifyDataSetChanged()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

    }

    private fun setupRecyclerView() {
        estudianteAdapter =
            EstudianteAgregarAdapter(filterEstudianteList) { estudianteSeleccionado ->
                val intent = Intent(this, AgregarIncidencia::class.java)
                intent.putExtra("EXTRA_STUDENT_ID", estudianteSeleccionado.id)
                intent.putExtra("EXTRA_STUDENT_NAME", estudianteSeleccionado.nombres)
                intent.putExtra("EXTRA_STUDENT_LAST_NAME", estudianteSeleccionado.apellidos)
                intent.putExtra("EXTRA_STUDENT_GRADE", estudianteSeleccionado.grado)
                intent.putExtra("EXTRA_STUDENT_SECTION", estudianteSeleccionado.nivel)
                intent.putExtra("EXTRA_STUDENT_CELULAR", estudianteSeleccionado.celularApoderado)
                startActivity(intent)
            }
        recyclerViewEstudiantes.layoutManager = LinearLayoutManager(this)
        recyclerViewEstudiantes.adapter = estudianteAdapter
    }

    private fun fetchEstudiantes(grado: String, seccion: String) {
        firestore.collection("Estudiante")
            .whereEqualTo("grado", grado.toInt())
            .whereEqualTo("nivel", seccion)
            .get()
            .addOnSuccessListener { result ->
                estudianteList.clear()
                for (document in result) {
                    val id = document.id  // Obtener el ID del documento
                    val nombres = document.getString("nombres") ?: ""
                    val apellidos = document.getString("apellidos") ?: ""
                    val grado = document.getLong("grado")?.toInt() ?: 0
                    val seccion = document.getString("nivel") ?: ""
                    val celularApoderado = document.getLong("celularApoderado")?.toInt() ?: 0
                    estudianteList.add(EstudianteAgregar(id, nombres, apellidos, grado, seccion, celularApoderado))
                }
                filterEstudiante(searchViewEstudiante.query.toString())

            }
    }


    private fun setupSearchView() {
        searchViewEstudiante.setOnClickListener {
            searchViewEstudiante.isIconified = false
            searchViewEstudiante.requestFocus()
        }

        searchViewEstudiante.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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
        val gradoSeleccionado = spinnerGrado.selectedItem.toString()
        val seccionSeleccionada = spinnerSeccion.selectedItem.toString()
        val queryWords = query.lowercase().split("\\s+".toRegex())

        filterEstudianteList.clear()
        estudianteList.filterTo(filterEstudianteList) { estudiante ->
            val coincideGrado =
                gradoSeleccionado == "Todas" || estudiante.grado.toString() == gradoSeleccionado
            val coincideSeccion =
                seccionSeleccionada == "Todas" || estudiante.nivel == seccionSeleccionada
            val nombreCompleto = "${estudiante.nombres} ${estudiante.apellidos}".lowercase()
            val coincideNombre = queryWords.all { nombreCompleto.contains(it) }
            coincideNombre && coincideGrado && coincideSeccion
        }
        filterEstudianteList.sortWith { e1, e2 ->
            val nombreCompleto1 = "${e1.apellidos} ${e1.nombres}".lowercase()
            val nombreCompleto2 = "${e2.apellidos} ${e2.nombres}".lowercase()
            nombreCompleto1.compareTo(nombreCompleto2)
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
        // Reset spinners to "Seleccione"
        spinnerGrado.setSelection(0)
        spinnerSeccion.setSelection(0)

        filterEstudianteList.clear()
        estudianteAdapter.notifyDataSetChanged()
    }
}