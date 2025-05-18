package com.example.educonnet.ui.tutor

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.educonnet.LoginActivity
import com.example.educonnet.R
import com.example.educonnet.ui.profesor.Profesor
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

class TutorFragment : Fragment() {

    private val ADD_TUTOR_REQUEST_CODE = 1
    private lateinit var tutorAdapter: TutorAdapter
    private val db = FirebaseFirestore.getInstance()
    private lateinit var recyclerViewTutores: RecyclerView
    private lateinit var searchViewTutor: SearchView
    private lateinit var addButtonTutor: FloatingActionButton
    private lateinit var userType: String
    private var isAddButtonVisible: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tutor, container, false)
        userType = LoginActivity.GlobalData.datoTipoUsuario
        isAddButtonVisible = userType == "Administrador"
        recyclerViewTutores = view.findViewById(R.id.recyclerViewTutores)
        searchViewTutor = view.findViewById(R.id.searchViewTutor)
        addButtonTutor = view.findViewById(R.id.addButtonTutor)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearchView()
        setupButtons()
    }

    private fun setupRecyclerView() {
        recyclerViewTutores.layoutManager = LinearLayoutManager(requireContext())
        tutorAdapter = TutorAdapter(
            onEditClickListener = { profesor -> handleEditClick(profesor) },
            onRemoveClickListener = { profesor -> showRemoveConfirmation(profesor) },
            isButtonVisible = userType == "Administrador",
            isTextViewGradosNivelVisible = true,
            isImageButtonQuitarTutor = isAddButtonVisible,
            buttonSeleccionar = false
        )
        recyclerViewTutores.adapter = tutorAdapter
        fetchProfesores()
    }

    private fun setupSearchView() {
        searchViewTutor.setOnClickListener {
            searchViewTutor.isIconified = false
            searchViewTutor.requestFocus()
        }

        searchViewTutor.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = handleSearch(query.orEmpty()).let { true }
            override fun onQueryTextChange(newText: String?) = handleSearch(newText.orEmpty()).let { true }
        })
    }

    private fun setupButtons() {
        addButtonTutor.visibility = if (isAddButtonVisible) View.VISIBLE else View.GONE
        addButtonTutor.setOnClickListener {
            startActivityForResult(Intent(requireContext(), AddTutor::class.java), ADD_TUTOR_REQUEST_CODE)
        }
    }

    private fun handleEditClick(profesor: Profesor) {
        // Implement edit functionality here
    }

    private fun showRemoveConfirmation(profesor: Profesor) {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar")
            .setMessage("¿Estás seguro de que deseas quitar al tutor?")
            .setPositiveButton("Sí") { _, _ -> removeTutor(profesor) }
            .setNegativeButton("No", null)
            .show()
    }

    private fun removeTutor(profesor: Profesor) {
        profesor.idProfesor?.let { id ->
            db.collection("Profesor").document(id)
                .update("grado", 0, "nivel", "", "tutor", false)
                .addOnSuccessListener {
                    tutorAdapter.updateList(tutorAdapter.currentList.filter { it.idProfesor != id })
                    showToast("Tutor removido correctamente")
                }
                .addOnFailureListener { e -> showToast("Error al remover el tutor: ${e.message}") }
        }
    }

    private fun handleSearch(query: String) {
        if (query.isBlank()) tutorAdapter.resetList() else tutorAdapter.filterList(query)
    }

    private fun fetchProfesores() {
        db.collection("Profesor")
            .whereEqualTo("tutor", true)
            .get()
            .addOnSuccessListener { result ->
                val listaProfesores = result.documents.mapNotNull { document ->
                    document.toObject<Profesor>()?.apply {
                        idProfesor = document.id
                        // Ensure all fields are properly set
                        nombres = document.getString("nombres").orEmpty()
                        apellidos = document.getString("apellidos").orEmpty()
                        celular = document.getLong("celular") ?: 0L
                        cargo = document.getString("cargo").orEmpty()
                        correo = document.getString("correo").orEmpty()
                        tutor = document.getBoolean("tutor") ?: false
                        grado = when (val gradoValue = document.get("grado")) {
                            is Long -> gradoValue
                            is String -> gradoValue.toLongOrNull() ?: 0L
                            else -> 0L
                        }
                        nivel = document.getString("nivel").orEmpty()
                        dni = document.getLong("dni") ?: 0L
                    }
                }
                tutorAdapter.updateList(listaProfesores)
            }
            .addOnFailureListener { e -> showToast("Error al cargar tutores: ${e.message}") }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_TUTOR_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            fetchProfesores()
        }
    }

    override fun onResume() {
        super.onResume()
        fetchProfesores()
    }
}