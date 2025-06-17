package com.example.educonnet.ui.tutoria.cita

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.educonnet.LoginActivity
import com.example.educonnet.ui.tutoria.recurso.Cita
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject

class CitaViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _allCitas = MutableLiveData<List<Cita>>(emptyList())
    private val _filteredCitas = MutableLiveData<List<Cita>>(emptyList())
    private val _loadingState = MutableLiveData(false)
    private val _errorState = MutableLiveData<String?>(null)

    private val idProfesor = LoginActivity.GlobalData.idUsuario
    private val allCitasPaginated = mutableListOf<Cita>()

    val filteredCitas: LiveData<List<Cita>> get() = _filteredCitas
    val loadingState: LiveData<Boolean> get() = _loadingState
    val errorState: LiveData<String?> get() = _errorState

    fun loadInitialCitas() {
        _allCitas.value?.takeIf { it.isNotEmpty() }?.let {
            _filteredCitas.value = it
            return
        }

        _loadingState.value = true
        _errorState.value = null

        db.collection("Cita")
            .whereEqualTo("idProfesor", idProfesor)
            .orderBy("fechaCita", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val citas = querySnapshot.documents.mapNotNull { doc ->
                    doc.toObject<Cita>()?.copy(idCita = doc.id)
                }

                allCitasPaginated.clear()
                allCitasPaginated.addAll(citas)

                _allCitas.value = allCitasPaginated.toList()
                _filteredCitas.value = allCitasPaginated.toList()
                _loadingState.value = false
            }
            .addOnFailureListener { e ->
                _errorState.value = "Error cargando citas: ${e.message}"
                _loadingState.value = false
            }
    }

    fun searchCitas(query: String) {
        val lista = _allCitas.value ?: return

        if (query.isBlank()) {
            _filteredCitas.value = lista
            return
        }

        val resultado = lista.filter {
            it.apoderado.contains(query, ignoreCase = true) ||
                    it.fechaCita.contains(query, ignoreCase = true) ||
                    it.hora.contains(query, ignoreCase = true) ||
                    it.parentesco.contains(query, ignoreCase = true)
        }

        _filteredCitas.value = resultado
    }

    fun resetCitas() {
        _filteredCitas.value = _allCitas.value ?: emptyList()
    }
}
