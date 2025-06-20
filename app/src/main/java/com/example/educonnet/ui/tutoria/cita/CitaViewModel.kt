package com.example.educonnet.ui.tutoria.cita

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educonnet.LoginActivity
import com.example.educonnet.ui.tutoria.recurso.Cita
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CitaViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _allCitas = MutableLiveData<List<Cita>>(emptyList())
    private val _filteredCitas = MutableLiveData<List<Cita>>(emptyList())
    private val _loadingState = MutableLiveData(false)
    private val _errorState = MutableLiveData<String?>(null)
    private val _updateState = MutableLiveData<UpdateState>(UpdateState.Idle)

    private val idProfesor = LoginActivity.GlobalData.idUsuario
    private val allCitasPaginated = mutableListOf<Cita>()

    val filteredCitas: LiveData<List<Cita>> get() = _filteredCitas
    val loadingState: LiveData<Boolean> get() = _loadingState
    val errorState: LiveData<String?> get() = _errorState
    val updateState: LiveData<UpdateState> get() = _updateState

    /**
     * Estados posibles para las operaciones de actualización
     */
    sealed class UpdateState {
        object Idle : UpdateState()
        object Updating : UpdateState()
        object Success : UpdateState()
        data class Error(val message: String) : UpdateState()
    }

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

    /**
     * Reprograma una cita actualizando fecha y hora en Firestore
     */
    fun reprogramarCita(cita: Cita, nuevaFecha: String, nuevaHora: String) {
        viewModelScope.launch {
            try {
                _updateState.value = UpdateState.Updating

                // Validar que la cita tenga ID
                val citaId = cita.idCita
                if (citaId.isNullOrBlank()) {
                    _updateState.value = UpdateState.Error("ID de cita no válido")
                    return@launch
                }

                // Preparar datos para actualizar
                val updateData = hashMapOf<String, Any>(
                    "fechaCita" to nuevaFecha,
                    "hora" to nuevaHora,
                    "fechaModificacion" to getCurrentTimestamp()
                )

                // Actualizar en Firestore
                db.collection("Cita")
                    .document(citaId)
                    .update(updateData)
                    .await()

                // Actualizar la lista local
                updateLocalCita(citaId, nuevaFecha, nuevaHora)

                _updateState.value = UpdateState.Success

            } catch (e: Exception) {
                _updateState.value = UpdateState.Error("Error al reprogramar: ${e.message}")
            }
        }
    }

    /**
     * Actualiza una cita completa en Firestore
     */
    fun actualizarCita(cita: Cita) {
        viewModelScope.launch {
            try {
                _updateState.value = UpdateState.Updating

                val citaId = cita.idCita
                if (citaId.isNullOrBlank()) {
                    _updateState.value = UpdateState.Error("ID de cita no válido")
                    return@launch
                }

                // Crear mapa con todos los datos de la cita (excluyendo el ID)
                val citaMap = hashMapOf<String, Any?>(
                    "apoderado" to cita.apoderado,
                    "parentesco" to cita.parentesco,
                    "fechaCita" to cita.fechaCita,
                    "hora" to cita.hora,
                    "detalle" to cita.detalle,
                    "createFecha" to cita.createFecha,
                    "idProfesor" to cita.idProfesor,
                    "fechaModificacion" to getCurrentTimestamp()
                ).filterValues { it != null } // Remover valores nulos

                // Actualizar en Firestore
                db.collection("Cita")
                    .document(citaId)
                    .update(citaMap)
                    .await()

                // Actualizar la lista local
                updateLocalCitaComplete(cita.copy(fechaModificacion = getCurrentTimestamp()))

                _updateState.value = UpdateState.Success

            } catch (e: Exception) {
                _updateState.value = UpdateState.Error("Error al actualizar cita: ${e.message}")
            }
        }
    }

    /**
     * Elimina una cita de Firestore
     */
    fun eliminarCita(citaId: String) {
        viewModelScope.launch {
            try {
                _updateState.value = UpdateState.Updating

                // Eliminar de Firestore
                db.collection("Cita")
                    .document(citaId)
                    .delete()
                    .await()

                // Remover de la lista local
                removeLocalCita(citaId)

                _updateState.value = UpdateState.Success

            } catch (e: Exception) {
                _updateState.value = UpdateState.Error("Error al eliminar cita: ${e.message}")
            }
        }
    }

    /**
     * Actualiza la cita localmente después de una reprogramación exitosa
     */
    private fun updateLocalCita(citaId: String, nuevaFecha: String, nuevaHora: String) {
        val currentList = _allCitas.value?.toMutableList() ?: return

        val index = currentList.indexOfFirst { it.idCita == citaId }
        if (index != -1) {
            val updatedCita = currentList[index].copy(
                fechaCita = nuevaFecha,
                hora = nuevaHora,
                fechaModificacion = getCurrentTimestamp()
            )
            currentList[index] = updatedCita

            // Actualizar ambas listas
            _allCitas.value = currentList

            // Si hay un filtro activo, también actualizar la lista filtrada
            val filteredList = _filteredCitas.value?.toMutableList()
            filteredList?.let { list ->
                val filteredIndex = list.indexOfFirst { it.idCita == citaId }
                if (filteredIndex != -1) {
                    list[filteredIndex] = updatedCita
                    _filteredCitas.value = list
                }
            } ?: run {
                _filteredCitas.value = currentList
            }

            // Actualizar la lista paginada también
            val paginatedIndex = allCitasPaginated.indexOfFirst { it.idCita == citaId }
            if (paginatedIndex != -1) {
                allCitasPaginated[paginatedIndex] = updatedCita
            }
        }
    }

    /**
     * Actualiza completamente una cita en la lista local
     */
    private fun updateLocalCitaComplete(citaActualizada: Cita) {
        val currentList = _allCitas.value?.toMutableList() ?: return

        val index = currentList.indexOfFirst { it.idCita == citaActualizada.idCita }
        if (index != -1) {
            currentList[index] = citaActualizada

            _allCitas.value = currentList

            // Actualizar lista filtrada si existe
            val filteredList = _filteredCitas.value?.toMutableList()
            filteredList?.let { list ->
                val filteredIndex = list.indexOfFirst { it.idCita == citaActualizada.idCita }
                if (filteredIndex != -1) {
                    list[filteredIndex] = citaActualizada
                    _filteredCitas.value = list
                }
            } ?: run {
                _filteredCitas.value = currentList
            }

            // Actualizar lista paginada
            val paginatedIndex = allCitasPaginated.indexOfFirst { it.idCita == citaActualizada.idCita }
            if (paginatedIndex != -1) {
                allCitasPaginated[paginatedIndex] = citaActualizada
            }
        }
    }

    /**
     * Remueve una cita de las listas locales
     */
    private fun removeLocalCita(citaId: String) {
        val currentList = _allCitas.value?.toMutableList() ?: return

        currentList.removeAll { it.idCita == citaId }
        _allCitas.value = currentList

        val filteredList = _filteredCitas.value?.toMutableList()
        filteredList?.let { list ->
            list.removeAll { it.idCita == citaId }
            _filteredCitas.value = list
        } ?: run {
            _filteredCitas.value = currentList
        }

        allCitasPaginated.removeAll { it.idCita == citaId }
    }

    /**
     * Busca citas por texto
     */
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
                    it.parentesco.contains(query, ignoreCase = true) ||
                    it.detalle?.contains(query, ignoreCase = true) == true
        }

        _filteredCitas.value = resultado
    }

    /**
     * Resetea el filtro mostrando todas las citas
     */
    fun resetCitas() {
        _filteredCitas.value = _allCitas.value ?: emptyList()
    }

    /**
     * Resetea el estado de actualización
     */
    fun resetUpdateState() {
        _updateState.value = UpdateState.Idle
    }

    /**
     * Recarga las citas desde Firestore
     */
    fun reloadCitas() {
        allCitasPaginated.clear()
        _allCitas.value = emptyList()
        loadInitialCitas()
    }

    /**
     * Obtiene el timestamp actual en formato String
     */
    private fun getCurrentTimestamp(): String {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }

    /**
     * Filtra citas por estado (vencidas, vigentes, hoy)
     */
    fun filterByStatus(status: CitaStatus) {
        val allCitas = _allCitas.value ?: return
        val currentDate = java.util.Date()
        val today = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(currentDate)

        val filtered = when (status) {
            CitaStatus.ALL -> allCitas
            CitaStatus.VIGENTES -> allCitas.filter {
                isAppointmentUpcoming(it.fechaCita, it.hora, currentDate)
            }
            CitaStatus.VENCIDAS -> allCitas.filter {
                !isAppointmentUpcoming(it.fechaCita, it.hora, currentDate)
            }
            CitaStatus.HOY -> allCitas.filter { it.fechaCita == today }
        }

        _filteredCitas.value = filtered
    }

    /**
     * Verifica si una cita está vigente
     */
    private fun isAppointmentUpcoming(fechaCita: String, hora: String, currentDate: java.util.Date): Boolean {
        return try {
            val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            val timeFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale("es", "ES"))

            val appointmentDate = dateFormat.parse(fechaCita) ?: return false
            val cleanedTime = hora.replace("a. m.", "AM").replace("p. m.", "PM")
            val appointmentTime = timeFormat.parse(cleanedTime) ?: return false

            val combinedCalendar = java.util.Calendar.getInstance().apply {
                time = appointmentDate
                val timeCal = java.util.Calendar.getInstance().apply { time = appointmentTime }
                set(java.util.Calendar.HOUR_OF_DAY, timeCal.get(java.util.Calendar.HOUR_OF_DAY))
                set(java.util.Calendar.MINUTE, timeCal.get(java.util.Calendar.MINUTE))
            }

            combinedCalendar.time.after(currentDate)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Enum para los estados de filtrado
     */
    enum class CitaStatus {
        ALL, VIGENTES, VENCIDAS, HOY
    }
}