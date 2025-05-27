package com.example.educonnet.ui.tutor

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.educonnet.R
import com.example.educonnet.ui.profesor.Profesor
import com.google.firebase.firestore.FirebaseFirestore

class AddTutor : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var buttonAceptar: Button
    private lateinit var buttonCancelar: Button
    private lateinit var spinnerGrado: Spinner
    private lateinit var spinnerSeccion: Spinner
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
        setupGradoSpinner()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerViewSeleccionarTutor)
        searchView = findViewById(R.id.searchViewTutorAdd)
        buttonAceptar = findViewById(R.id.buttonAceptarTutor)
        buttonCancelar = findViewById(R.id.buttonCancelarTutor)
        spinnerGrado = findViewById(R.id.spinnerGrado)
        spinnerSeccion = findViewById(R.id.spinnerNivel)
    }

    private fun setupGradoSpinner() {
        val grados = arrayOf("1", "2", "3", "4", "5")
        val adapterGrados = ArrayAdapter(this, R.layout.spinner_item_selected, grados)
        adapterGrados.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
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
            "1" -> arrayOf("A", "B", "C", "D", "E")
            else -> arrayOf("A", "B", "C", "D")
        }

        val adapterSecciones = ArrayAdapter(this, R.layout.spinner_item_selected, secciones)
        adapterSecciones.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSeccion.adapter = adapterSecciones
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

    private fun fetchProfesores() {
        db.collection("Profesor")
            .whereEqualTo("tutor", false)
            .get()
            .addOnSuccessListener { result ->
                val listaProfesores = result.documents.mapNotNull { document ->
                    try {
                        Profesor(
                            idProfesor = document.id,
                            nombres = document.getString("nombres").orEmpty(),
                            apellidos = document.getString("apellidos").orEmpty(),
                            celular = document.getLong("celular") ?: 0L,
                            correo = document.getString("correo").orEmpty(),
                            grado = document.getLong("grado") ?: 0L,
                            seccion = document.getString("seccion").orEmpty()
                        )
                    } catch (e: Exception) {
                        Log.e("FetchError", "Error fetching profesor: ${e.message}")
                        null
                    }
                }
                tutorAdapter.updateList(listaProfesores)
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Error getting documents: ", exception)
                Toast.makeText(this, "Error al cargar profesores", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateSelectedTutor() {
        selectedProfesorId?.let { profesorId ->
            val selectedGrado = spinnerGrado.selectedItem.toString().toLongOrNull() ?: 0L
            val selectedSeccion = spinnerSeccion.selectedItem.toString()

            // Verificar si la sección ya está asignada
            db.collection("Profesor")
                .whereEqualTo("grado", selectedGrado)
                .whereEqualTo("seccion", selectedSeccion)
                .whereEqualTo("tutor", true)
                .get()
                .addOnSuccessListener { result ->
                    if (result.isEmpty) {
                        // Actualizar profesor como tutor
                        db.collection("Profesor").document(profesorId)
                            .update(
                                mapOf(
                                    "tutor" to true,
                                    "grado" to selectedGrado,
                                    "seccion" to selectedSeccion
                                )
                            )
                            .addOnSuccessListener {
                                Toast.makeText(this, "Tutor asignado exitosamente", Toast.LENGTH_SHORT).show()
                                setResult(RESULT_OK)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error al asignar tutor: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "Esta sección ya tiene un tutor asignado", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al verificar disponibilidad: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } ?: run {
            Toast.makeText(this, "Seleccione un profesor primero", Toast.LENGTH_SHORT).show()
        }
    }
}