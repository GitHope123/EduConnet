package com.example.educonnet.ui.tutor

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.educonnet.LoginActivity
import com.example.educonnet.R
import com.example.educonnet.ui.profesor.Profesor
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore

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
        inflater: LayoutInflater, container: ViewGroup?,
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

        recyclerViewTutores.layoutManager = LinearLayoutManager(requireContext())

        tutorAdapter = TutorAdapter(
            onEditClickListener = { profesor ->
                // Implementar lógica para editar un profesor si es necesario
            },
            onRemoveClickListener = { profesor ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Confirmar")
                    .setMessage("¿Estás seguro de que deseas quitar al tutor?")
                    .setPositiveButton("Sí") { dialog, which ->
                        profesor.idProfesor?.let { id ->
                            db.collection("Profesor").document(id)
                                .update(
                                    "grado", 0,
                                    "nivel", "",
                                    "tutor", false
                                )
                                .addOnSuccessListener {
                                    val updatedList = tutorAdapter.currentList.filter { it.idProfesor != id }
                                    tutorAdapter.updateList(updatedList)
                                    Toast.makeText(requireContext(), "Tutor removido correctamente", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { exception ->
                                    Toast.makeText(requireContext(), "Error al remover el tutor: ${exception.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                    .setNegativeButton("No") { dialog, which -> dialog.dismiss() }
                    .show()
            },
            isButtonVisible = userType == "Administrador",
            isTextViewGradosNivelVisible = true,
            isImageButtonQuitarTutor = isAddButtonVisible,
            buttonSeleccionar = false
        )

        recyclerViewTutores.adapter = tutorAdapter
        fetchProfesores()

        searchViewTutor.setOnClickListener {
            searchViewTutor.isIconified = false
            searchViewTutor.requestFocus()
        }

        searchViewTutor.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                handleSearch(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                handleSearch(newText.orEmpty())
                return true
            }
        })

        setupButtons()

        addButtonTutor.setOnClickListener {
            val intent = Intent(requireContext(), AddTutor::class.java)
            startActivityForResult(intent, ADD_TUTOR_REQUEST_CODE)
        }
    }

    private fun setupButtons() {
        addButtonTutor.visibility = if (isAddButtonVisible) View.VISIBLE else View.GONE
    }

    private fun handleSearch(query: String) {
        if (query.isBlank()) {
            tutorAdapter.resetList()
        } else {
            tutorAdapter.filterList(query)
        }
    }

    private fun fetchProfesores() {
        db.collection("Profesor")
            .whereEqualTo("tutor", true)
            .get()
            .addOnSuccessListener { result ->
                val listaProfesores = result.documents.mapNotNull { document ->
                    document.toObject(Profesor::class.java)?.apply {
                        idProfesor = document.id
                        nombres = document.getString("nombres") ?: ""
                        apellidos = document.getString("apellidos") ?: ""
                        celular = document.getLong("celular") ?: 0L
                        cargo = document.getString("cargo") ?: ""
                        correo = document.getString("correo") ?: ""
                        tutor = document.getBoolean("tutor") ?: false
                        grado = when (val gradoValue = document.get("grado")) {
                            is Long -> gradoValue
                            is String -> gradoValue.toLongOrNull() ?: 0L
                            else -> 0L
                        }
                        seccion = document.getString("seccion") ?: ""
                        dni = document.getLong("dni") ?: 0L
                    }
                }

                // Ordenar por grado (ascendente) y luego por sección (alfabéticamente)
                val listaOrdenada = listaProfesores.sortedWith(
                    compareBy<Profesor> { it.grado }.thenBy { it.seccion }
                )

                tutorAdapter.updateList(listaOrdenada)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error fetching tutors: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
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