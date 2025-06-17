package com.example.educonnet.ui.incidencia

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckedTextView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.educonnet.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.firebase.firestore.FirebaseFirestore

class AgregarEstudiantes : AppCompatActivity() {

    private lateinit var searchViewEstudiante: SearchView
    private lateinit var recyclerViewEstudiantes: RecyclerView
    private lateinit var spinnerGrado: MaterialAutoCompleteTextView
    private lateinit var spinnerSeccion: MaterialAutoCompleteTextView
    private lateinit var btnContinuar: MaterialButton
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val estudiantesDisponibles = mutableListOf<EstudianteAgregar>()
    private val estudiantesFiltrados = mutableListOf<EstudianteAgregar>()
    private val selectedStudents = mutableListOf<EstudianteAgregar>()

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
        spinnerGrado.setAdapter(adapterGrados)

        spinnerGrado.setOnItemClickListener { _, _, position, _ ->
            val grado = spinnerGrado.text.toString()
            setupSeccionSpinner(grado)
        }
    }

    private fun setupSeccionSpinner(gradoSeleccionado: String) {
        val secciones = when (gradoSeleccionado) {
            "Seleccione" -> arrayOf("Seleccione")
            "1" -> arrayOf("Seleccione", "A", "B", "C", "D", "E")
            else -> arrayOf("Seleccione", "A", "B", "C", "D")
        }

        val adapterSeccion = ArrayAdapter(this, R.layout.item_spinner, secciones)
        spinnerSeccion.setAdapter(adapterSeccion)
        spinnerSeccion.isEnabled = gradoSeleccionado != "Seleccione"

        spinnerSeccion.setOnItemClickListener { _, _, _, _ ->
            val grado = spinnerGrado.text.toString()
            val seccion = spinnerSeccion.text.toString()

            if (grado != "Seleccione" && seccion != "Seleccione") {
                fetchEstudiantes(grado, seccion)
            } else {
                clearEstudiantes()
            }
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
                showConfirmationDialog()
            } else {
                Toast.makeText(this, "Seleccione al menos un estudiante", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showConfirmationDialog() {
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.dialog_estudiantes_seleccionados_material, null)
        val container = view.findViewById<LinearLayout>(R.id.contenedorEstudiantes)

        selectedStudents.forEachIndexed { index, estudiante ->
            val nombreCompleto = "${estudiante.apellidos}, ${estudiante.nombres}"
            val nombreAcortado = if (nombreCompleto.length > 22) {
                nombreCompleto.take(22) + "..."
            } else {
                nombreCompleto
            }

            // Texto del estudiante
            val textView = TextView(this).apply {
                text = "${index + 1}. $nombreAcortado (${estudiante.grado}° ${estudiante.seccion})"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                setTextColor(ContextCompat.getColor(context, R.color.md_theme_onSurfaceVariant))
                setPadding(0, 8, 0, 8)
            }
            container.addView(textView)

            // Línea divisora (excepto después del último)
            if (index < selectedStudents.lastIndex) {
                val divider = View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1
                    ).apply {
                        topMargin = 4
                        bottomMargin = 4
                    }
                    setBackgroundColor(ContextCompat.getColor(context, R.color.md_theme_onSurfaceVariant)) // Usa un color gris claro
                }
                container.addView(divider)
            }
        }

        // Crear el diálogo con vista personalizada
        MaterialAlertDialogBuilder(this)
            .setTitle("Estudiantes seleccionados")
            .setView(view)
            .setPositiveButton("Continuar") { _, _ ->
                val intent = Intent(this, AgregarIncidencia::class.java).apply {
                    putParcelableArrayListExtra("EXTRA_STUDENTS", ArrayList(selectedStudents))
                }
                startActivity(intent)
            }
            .setNegativeButton("Cancelar", null)
            .setNeutralButton("Quitar estudiantes") { _, _ ->
                showRemoveStudentsDialog()
            }
            .show()
    }

    private fun showRemoveStudentsDialog() {
        val studentItems = selectedStudents.map {
            val nombreCompleto = "${it.nombres} ${it.apellidos}"
            val nombreAcortado = if (nombreCompleto.length > 30) {
                nombreCompleto.take(30) + "..."
            } else {
                nombreCompleto
            }
            "$nombreAcortado (${it.grado}° ${it.seccion})"
        }.toMutableList() // ← importante para evitar el error

        val checkedItems = BooleanArray(selectedStudents.size) { true }

        val adapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_list_item_multiple_choice,
            studentItems
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as CheckedTextView
                view.textSize = 12f
                return view
            }
        }

        val listView = ListView(this).apply {
            choiceMode = ListView.CHOICE_MODE_MULTIPLE
            this.adapter = adapter
            for (i in checkedItems.indices) {
                setItemChecked(i, checkedItems[i])
            }
            setOnItemClickListener { _, _, position, _ ->
                checkedItems[position] = !checkedItems[position]
            }
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Quitar estudiantes")
            .setView(listView)
            .setPositiveButton("Quitar seleccionados") { _, _ ->
                removeSelectedStudents(checkedItems)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun removeSelectedStudents(checkedItems: BooleanArray) {
        val studentsToRemove = mutableListOf<EstudianteAgregar>()

        for (i in checkedItems.indices) {
            if (checkedItems[i]) {
                studentsToRemove.add(selectedStudents[i])
            }
        }

        selectedStudents.removeAll(studentsToRemove)

        // Actualizar el estado de los checkboxes en el RecyclerView
        estudianteAdapter.updateSelectedStudents(selectedStudents.map { it.id }.toSet())
        estudianteAdapter.notifyDataSetChanged()
        updateContinuarButtonState()

        Toast.makeText(this, "${studentsToRemove.size} estudiantes removidos", Toast.LENGTH_SHORT).show()
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