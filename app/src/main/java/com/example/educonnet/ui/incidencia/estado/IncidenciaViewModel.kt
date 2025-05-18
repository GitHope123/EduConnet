package com.example.educonnet.ui.incidencia.estado
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class IncidenciaViewModel : ViewModel() {

    private val _incidenciasLiveData = MutableLiveData<List<IncidenciaClass>>()
    private val _incidenciasFiltradasLiveData = MutableLiveData<List<IncidenciaClass>>()
    val incidenciasLiveData: LiveData<List<IncidenciaClass>> get() = _incidenciasLiveData
    val incidenciasFiltradasLiveData: LiveData<List<IncidenciaClass>> get() = _incidenciasFiltradasLiveData

    // Cargar incidencias desde el repositorio y actualizar LiveData
    fun cargarIncidencias(idProfesor: String, repositorio: IncidenciaRepository) {
        repositorio.getIncidenciaByEstado(idProfesor) { incidencias ->
            _incidenciasLiveData.value = incidencias
            _incidenciasFiltradasLiveData.value = incidencias.sortedByDescending {
                try {
                    val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    dateTimeFormat.parse("${it.fecha} ${it.hora}") ?: Date(0)
                } catch (e: Exception) {
                    Date(0)
                }
            }
        }
    }

    fun filtrarIncidenciasPorEstado(estado: String) {
        _incidenciasLiveData.value?.let { incidencias ->
            val incidenciasFiltradas = if (estado.isEmpty()) {
                // Si no hay estado, mostrar todas
                incidencias
            } else {
                // Filtrar por estado
                incidencias.filter { it.estado == estado }
            }

            // Ordenar las incidencias filtradas por fecha y hora
            _incidenciasFiltradasLiveData.value = incidenciasFiltradas.sortedByDescending {
                try {
                    val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    dateTimeFormat.parse("${it.fecha} ${it.hora}") ?: Date(0)
                } catch (e: Exception) {
                    Date(0)
                }
            }
        }
    }
}