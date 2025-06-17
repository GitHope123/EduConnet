package com.example.educonnet.ui.profesor

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.educonnet.LoginActivity
import com.example.educonnet.databinding.FragmentProfesorBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class ProfesorFragment : Fragment() {

    private var _binding: FragmentProfesorBinding? = null
    private val binding get() = _binding!!
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val profesorList = mutableListOf<Profesor>()
    private val filteredProfesorList = mutableListOf<Profesor>()
    private lateinit var profesorAdapter: ProfesorAdapter
    private lateinit var userType: String
    private var isAdmin: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfesorBinding.inflate(inflater, container, false)
        userType = LoginActivity.GlobalData.datoTipoUsuario
        isAdmin = userType == "Administrador"
        setupRecyclerView()
        fetchProfesores()
        setupSearchView()
        setupButtons()
        return binding.root
    }

    private fun setupRecyclerView() {
        profesorAdapter = ProfesorAdapter(
            context = requireContext(),
            profesores = filteredProfesorList,
            onEditClickListener = { profesor ->
                editarProfesor(profesor)
            },
            onDeleteClickListener = { profesor ->
                confirmarEliminacionProfesor(profesor.idProfesor.toString())
            },
            isEditButtonVisible = isAdmin
        )

        binding.recyclerViewProfesores.apply {
            adapter = profesorAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun editarProfesor(profesor: Profesor) {
        val intent = Intent(requireContext(), EditProfesor::class.java).apply {
            putExtra("idProfesor", profesor.idProfesor)
            putExtra("nombres", profesor.nombres)
            putExtra("apellidos", profesor.apellidos)
            putExtra("celular", profesor.celular)
            putExtra("cargo", profesor.cargo) // o usa "cargo" si ese es el campo real
            putExtra("correo", profesor.correo)
            putExtra("password", profesor.password) // si tienes acceso
            putExtra("dni", profesor.dni)
        }
        startActivity(intent)
    }
    private fun confirmarEliminacionProfesor(idProfesor: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Confirmar eliminación")
            .setMessage("¿Estás seguro de que deseas eliminar este profesor?")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarProfesor(idProfesor)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    private fun eliminarProfesor(idProfesor: String) {
        FirebaseFirestore.getInstance()
            .collection("Profesor")
            .document(idProfesor)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profesor eliminado", Toast.LENGTH_SHORT).show()
                fetchProfesores() // vuelve a cargar la lista si tienes esta función
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error al eliminar: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterProfesores(query ?: "")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterProfesores(newText ?: "")
                return true
            }
        })
    }

    private fun filterProfesores(query: String) {
        val filteredList = profesorList.filter {
            it.nombres.contains(query, ignoreCase = true) ||
                    it.apellidos.contains(query, ignoreCase = true) ||
                    it.celular.toString().contains(query, ignoreCase = true) ||
                    it.correo.contains(query, ignoreCase = true)
        }
        filteredProfesorList.clear()
        filteredProfesorList.addAll(filteredList)
        profesorAdapter.notifyDataSetChanged()
    }

    private fun fetchProfesores() {
        firestore.collection("Profesor")
            .get()
            .addOnSuccessListener { result ->
                profesorList.clear()
                result.documents.forEach { document ->
                    document.toProfesor()?.let {
                        profesorList.add(it)
                    }
                }
                filteredProfesorList.clear()
                filteredProfesorList.addAll(profesorList)
                profesorAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.e("ProfesorFragment", "Error fetching profesores", exception)
                Toast.makeText(context, "Error al cargar los profesores: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun setupButtons() {
        binding.addButtonProfesor.setOnClickListener {
            startActivity(Intent(context, AddProfesor::class.java))
        }
        binding.addButtonProfesor.visibility = if (isAdmin) View.VISIBLE else View.GONE
    }

    private fun DocumentSnapshot.toProfesor(): Profesor? {
        return try {
            Profesor(
                idProfesor = id,
                nombres = getString("nombres") ?: "",
                apellidos = getString("apellidos") ?: "",
                celular = (get("celular") as? Number)?.toLong() ?: 0L,
                cargo = getString("cargo") ?: "",
                correo = getString("correo") ?: "",
                grado = (get("grado") as? Number)?.toLong() ?: 0L,
                seccion = getString("seccion") ?: "",
                password = getString("password") ?: "",
                dni = (get("dni") as? Number)?.toLong() ?: 0L
            )
        } catch (e: Exception) {
            Log.e("ProfesorFragment", "Error converting document to Profesor", e)
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        fetchProfesores()
    }

    override fun onPause() {
        super.onPause()
        clearSearchView()
    }

    private fun clearSearchView() {
        binding.searchView.setQuery("", false)
        binding.searchView.clearFocus()
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }
}
