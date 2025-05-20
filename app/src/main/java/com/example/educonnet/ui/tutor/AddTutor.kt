package com.example.educonnet.ui.tutor

import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.widget.SearchView
import com.example.educonnet.R
import com.example.educonnet.ui.profesor.Profesor

class AddTutor : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var buttonAceptar: Button
    private lateinit var buttonCancelar: Button
    private lateinit var spinnerGrado: Spinner
    private lateinit var spinnerNivel: Spinner
    private lateinit var tutorAdapter: TutorAdapter
    private var selectedProfesorId: String? = null
    private val db = FirebaseFirestore.getInstance()
    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_tutor)

        initViews()
        setupRecyclerView()
        setupSearchView()
        setupButtons()
        fetchProfesores()
        getGradosYSeccionesAsignados()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerViewSeleccionarTutor)
        searchView = findViewById(R.id.searchViewTutorAdd)
        buttonAceptar = findViewById(R.id.buttonAceptarTutor)
        buttonCancelar = findViewById(R.id.buttonCancelarTutor)
        spinnerGrado = findViewById(R.id.spinnerGrado)
        spinnerNivel = findViewById(R.id.spinnerNivel)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        tutorAdapter = TutorAdapter(
            onEditClickListener = { profesor ->
                profesor.idProfesor?.let { id ->
                    selectedProfesorId = if (selectedProfesorId == id) null else id
                    tutorAdapter.notifyDataSetChanged()
                }
            },
            onRemoveClickListener = { profesor -> },
            isButtonVisible = true,
            isTextViewGradosNivelVisible = false,
            isImageButtonQuitarTutor = false,
            buttonSeleccionar = true
        )
        recyclerView.adapter = tutorAdapter
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                handleSearchQuery(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchRunnable?.let { searchHandler.removeCallbacks(it) }
                searchRunnable = Runnable {
                    handleSearchQuery(newText)
                }
                searchHandler.postDelayed(searchRunnable!!, 300)
                return true
            }
        })
    }

    private fun setupButtons() {
        buttonAceptar.setOnClickListener { updateSelectedTutor() }
        buttonCancelar.setOnClickListener { finish() }
    }

    private fun handleSearchQuery(query: String?) {
        val trimmedQuery = query?.trim()?.lowercase().orEmpty()
        if (trimmedQuery.isEmpty()) {
            tutorAdapter.resetList()
        } else {
            tutorAdapter.filterList(trimmedQuery)
        }
    }

    private fun getGradosYSeccionesAsignados() {
        val gradosYSeccionesAsignados = mutableListOf<Pair<String, String>>()
        val gradosDisponibles = arrayListOf("1", "2", "3", "4", "5", "6")

        db.collection("Profesor")
            .whereEqualTo("tutor", true)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val grado = document.getLong("grado")?.toString() ?: ""
                    val nivel = document.getString("nivel") ?: ""
                    if (grado.isNotEmpty()) {
                        gradosYSeccionesAsignados.add(Pair(grado, nivel))
                    }
                }
                displayGradosSeccionesDisponibles(gradosYSeccionesAsignados, gradosDisponibles)
                updateGrado(gradosYSeccionesAsignados)
            }
            .addOnFailureListener { exception ->
                Log.w("FirestoreError", "Error getting documents: ", exception)
            }
    }

    private fun displayGradosSeccionesDisponibles(
        gradosYSeccionesAsignados: List<Pair<String, String>>,
        gradosDisponibles: List<String>
    ) {
        val nivelesDisponiblesPorGrado = mapOf(
            "1" to listOf("Secundaria"),
            "2" to listOf("Secundaria"),
            "3" to listOf("Secundaria"),
            "4" to listOf("Secundaria"),
            "5" to listOf("Primaria", "Secundaria"),
            "6" to listOf("Primaria")
        )

        val gradosConNivelesDisponibles = mutableListOf<String>()
        for (grado in gradosDisponibles) {
            val seccionesAsignadas = gradosYSeccionesAsignados
                .filter { it.first == grado }
                .map { it.second }
            val seccionesDisponibles = nivelesDisponiblesPorGrado[grado] ?: emptyList()

            // Check available sections for each grade
            for (nivel in seccionesDisponibles) {
                if (!seccionesAsignadas.contains(nivel)) {
                    gradosConNivelesDisponibles.add("Grado $grado - Sección $nivel")
                }
            }
        }

        val mensaje = if (gradosConNivelesDisponibles.isNotEmpty()) {
            gradosConNivelesDisponibles.joinToString(separator = "\n")
        } else {
            "Todos los grados y secciones están completamente asignados."
        }

        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
    }

    private fun updateGrado(gradosYNivelesAsignados: List<Pair<String, String>>) {
        val gradosDisponibles = listOf("1", "2", "3", "4", "5", "6")
        val gradosFiltrados = gradosDisponibles.filter { grado ->
            val nivelesAsignados = gradosYNivelesAsignados
                .filter { it.first == grado }
                .map { it.second }
            val nivelesDisponibles = when (grado) {
                "1", "2", "3", "4" -> listOf("Secundaria")
                "5" -> listOf("Primaria", "Secundaria")
                "6" -> listOf("Primaria")
                else -> emptyList()
            }
            nivelesDisponibles.any { !nivelesAsignados.contains(it) }
        }

        val adapterGrados = ArrayAdapter(this, R.layout.spinner_item_selected, gradosFiltrados)
        adapterGrados.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGrado.adapter = adapterGrados
        spinnerGrado.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val gradoSeleccionado = spinnerGrado.selectedItem.toString()
                updateNiveles(gradoSeleccionado, gradosYNivelesAsignados)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun updateNiveles(gradoSeleccionado: String, gradosYNivelesAsignados: List<Pair<String, String>>) {
        val nivelesDisponibles = when (gradoSeleccionado) {
            "1", "2", "3", "4" -> listOf("Secundaria")
            "5" -> listOf("Primaria", "Secundaria")
            "6" -> listOf("Primaria")
            else -> emptyList()
        }

        val nivelesDisponiblesParaGrado = nivelesDisponibles.filter { nivel ->
            gradosYNivelesAsignados.none { it.first == gradoSeleccionado && it.second == nivel }
        }

        val adapterNiveles = ArrayAdapter(this, R.layout.spinner_item_selected, nivelesDisponiblesParaGrado)
        adapterNiveles.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerNivel.adapter = adapterNiveles
    }

    private fun fetchProfesores() {
        db.collection("Profesor")
            .whereEqualTo("tutor", false)
            .get()
            .addOnSuccessListener { result ->
                val listaProfesores = result.documents.mapNotNull { document ->
                    try {
                        val nombres = document.getString("nombres").orEmpty()
                        val apellidos = document.getString("apellidos").orEmpty()
                        val celular = document.getLong("celular") ?: 0L
                        val correo = document.getString("correo").orEmpty()
                        val grado = document.getLong("grado") ?: 0L
                        val seccion = document.getString("nivel").orEmpty()

                        if (nombres.isNotEmpty() && apellidos.isNotEmpty() && celular > 0 && correo.isNotEmpty()) {
                            Profesor(
                                idProfesor = document.id,
                                nombres = nombres,
                                apellidos = apellidos,
                                celular = celular,
                                correo = correo,
                                grado = grado,
                                nivel = seccion
                            )
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        Log.e("FetchError", "Error fetching profesor: ${e.message}")
                        null
                    }
                }

                tutorAdapter.updateList(listaProfesores)
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Error getting documents: ", exception)
            }
    }
    private fun updateSelectedTutor() {
        selectedProfesorId?.let { profesorId ->
            val selectedGrado = spinnerGrado.selectedItem.toString().toLongOrNull() ?: 0L
            val selectedNivel = spinnerNivel.selectedItem.toString()

            db.collection("Profesor")
                .whereEqualTo("grado", selectedGrado)
                .whereEqualTo("nivel", selectedNivel)
                .whereEqualTo("tutor", true)
                .get()
                .addOnSuccessListener { result ->
                    if (result.isEmpty) {
                        val profesorRef = db.collection("Profesor").document(profesorId)
                        val batch = db.batch()
                        batch.update(profesorRef, "tutor", true)
                        batch.update(profesorRef, "grado", selectedGrado)
                        batch.update(profesorRef, "nivel", selectedNivel)

                        batch.commit()
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "Tutor actualizado exitosamente.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                setResult(RESULT_OK)
                                finish()
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(
                                    this,
                                    "Error al actualizar tutor: ${exception.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        Toast.makeText(
                            this,
                            "La combinación de grado y sección ya está en uso.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        this,
                        "Error al verificar disponibilidad: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } ?: Toast.makeText(this, "No hay tutor seleccionado.", Toast.LENGTH_SHORT).show()
    }

}