package com.example.educonnet.ui.estudiante

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.educonnet.LoginActivity
import com.example.educonnet.R
import com.example.educonnet.databinding.FragmentEstudianteBinding
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class EstudianteFragment : Fragment() {

    private var _binding: FragmentEstudianteBinding? = null
    private val binding get() = _binding!!
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var estudianteAdapter: EstudianteAdapter
    private val estudiantesList = mutableListOf<Estudiante>()
    private val estudiantesFiltrados = mutableListOf<Estudiante>()
    private val userType by lazy { LoginActivity.GlobalData.datoTipoUsuario }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEstudianteBinding.inflate(inflater, container, false)
        setupUI()
        return binding.root
    }

    private fun setupUI() {
        setupSpinners()
        setupRecyclerView()
        setupSearchView()
        setupButtons()
    }

    private fun setupSpinners() {
        // Configuración del spinner de grados (1-5)
        val grados = arrayOf("Seleccione", "1", "2", "3", "4", "5")
        val adapterGrados = ArrayAdapter(requireContext(), R.layout.spinner_item_selected, grados)
        adapterGrados.setDropDownViewResource(R.layout.spinner_item_selected)
        binding.spinnerGrado.adapter = adapterGrados

        binding.spinnerGrado.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                val selectedGrado = parent?.getItemAtPosition(pos)?.toString() ?: ""
                updateSecciones(selectedGrado)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun updateSecciones(grado: String) {
        if (grado == "Seleccione") {
            binding.spinnerSeccion.isEnabled = false
            limpiarLista()
            return
        }

        val secciones = when (grado) {
            "Seleccione" -> arrayOf("Seleccione")
            "1" -> arrayOf("Seleccione", "A", "B", "C", "D", "E")
            else -> arrayOf("Seleccione", "A", "B", "C", "D")
        }

        val adapterSecciones = ArrayAdapter(requireContext(), R.layout.spinner_item_selected, secciones)
        adapterSecciones.setDropDownViewResource(R.layout.spinner_item_selected)
        binding.spinnerSeccion.adapter = adapterSecciones
        binding.spinnerSeccion.isEnabled = grado != "Seleccione"

        binding.spinnerSeccion.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                val grado = binding.spinnerGrado.selectedItem?.toString() ?: ""
                val seccion = parent?.getItemAtPosition(pos)?.toString() ?: ""
                if (grado != "Seleccione" && seccion != "Seleccione") {
                    cargarEstudiantes(grado, seccion)
                } else {
                    limpiarLista()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun cargarEstudiantes(grado: String, seccion: String) {
        firestore.collection("Estudiante")
            .whereEqualTo("grado", grado.toInt())
            .whereEqualTo("seccion", seccion)
            .get()
            .addOnSuccessListener { result ->
                estudiantesList.clear()
                result.documents.mapNotNull { it.toEstudiante() }.forEach {
                    estudiantesList.add(it)
                }
                filtrarEstudiantes(binding.searchView.query?.toString() ?: "")
            }
            .addOnFailureListener { e ->
                Log.e("EstudianteFragment", "Error al cargar estudiantes", e)
                // Aquí podrías mostrar un mensaje al usuario
            }
    }

    private fun DocumentSnapshot.toEstudiante(): Estudiante? {
        return try {
            Estudiante(
                idEstudiante = getString("idEstudiante") ?: "",
                apellidos = getString("apellidos") ?: "",
                nombres = getString("nombres") ?: "",
                grado = getLong("grado")?.toInt() ?: 0,
                seccion = getString("seccion") ?: "",
                cantidadIncidencias = getLong("cantidadIncidencias")?.toInt() ?: 0,
                celularApoderado = getLong("celularApoderado")?.toInt() ?: 0
            )
        } catch (e: Exception) {
            Log.e("EstudianteFragment", "Error al convertir documento", e)
            null
        }
    }

    private fun filtrarEstudiantes(query: String) {
        val queryLower = query.lowercase()
        val grado = binding.spinnerGrado.selectedItem?.toString()
        val seccion = binding.spinnerSeccion.selectedItem?.toString()

        estudiantesFiltrados.clear()
        estudiantesFiltrados.addAll(
            estudiantesList.filter { estudiante ->
                val coincideFiltros = (grado == null || estudiante.grado.toString() == grado) &&
                        (seccion == null || estudiante.seccion == seccion)

                val coincideNombre = "${estudiante.nombres} ${estudiante.apellidos}"
                    .lowercase().contains(queryLower)

                coincideFiltros && (query.isEmpty() || coincideNombre)
            }.sortedBy { "${it.apellidos} ${it.nombres}".lowercase() }
        )

        estudianteAdapter.notifyDataSetChanged()
    }

    private fun setupRecyclerView() {
        estudianteAdapter = EstudianteAdapter(
            estudiantes = estudiantesFiltrados,
            onEditClickListener = { estudiante ->
                val intent = Intent(requireContext(), EditEstudiante::class.java).apply {
                    putExtra("idEstudiante", estudiante.idEstudiante)
                    putExtra("nombres", estudiante.nombres)
                    putExtra("apellidos", estudiante.apellidos)
                    putExtra("grado", estudiante.grado)
                    putExtra("seccion", estudiante.seccion)
                    putExtra("celularApoderado", estudiante.celularApoderado)
                }
                startActivityForResult(intent, EDIT_ESTUDIANTE_REQUEST_CODE)
            },
            isEditButtonVisible = userType == "Administrador"
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = estudianteAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true
            override fun onQueryTextChange(newText: String?): Boolean {
                filtrarEstudiantes(newText ?: "")
                return true
            }
        })
    }

    private fun setupButtons() {
        binding.addButtonEstudiante.apply {
            visibility = if (userType == "Administrador") View.VISIBLE else View.GONE
            setOnClickListener {
                startActivity(Intent(context, AddEstudiante::class.java))
            }
        }
    }

    private fun limpiarLista() {
        estudiantesFiltrados.clear()
        estudianteAdapter.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EDIT_ESTUDIANTE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            refrescarDatos()
        }
    }

    override fun onResume() {
        super.onResume()
        refrescarDatos()
    }

    private fun refrescarDatos() {
        val grado = binding.spinnerGrado.selectedItem?.toString() ?: ""
        val seccion = binding.spinnerSeccion.selectedItem?.toString() ?: ""
        if (grado != "Seleccione" && seccion != "Seleccione") {
            cargarEstudiantes(grado, seccion)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val EDIT_ESTUDIANTE_REQUEST_CODE = 1001
    }
}