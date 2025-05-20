package com.example.educonnet.ui.tutoria

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TutoriaViewModel : ViewModel()  {
    private val _incidenciasLiveData = MutableLiveData<List<TutoriaClass>>()
    private val _incidenciasFiltradasLiveData = MutableLiveData<List<TutoriaClass>>()
    val incidenciasLiveData: LiveData<List<TutoriaClass>> get() = _incidenciasLiveData
    val incidenciasFiltradasLiveData: LiveData<List<TutoriaClass>> get() = _incidenciasFiltradasLiveData

    fun cargarDatos(grado: String, nivel: String, repositorio: TutoriaRepository) {
        repositorio.getIncidenciasPorGradoSeccion(grado.toInt(),nivel) { incidencias ->
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

    fun filtrarIncidenciasPorEstado(estado: String, filtroFecha: String) {
        _incidenciasLiveData.value?.let { incidencias ->
            val incidenciasFiltradas = if (estado.isEmpty()) {
                incidencias
            } else {
                incidencias.filter { it.estado.equals(estado, ignoreCase = true) }
            }
            val filteredList = when (filtroFecha) {
                "Hoy" -> filterByToday(incidenciasFiltradas)
                "Ultimos 7 dias" -> filterByLast7Days(incidenciasFiltradas)
                "Este mes" -> filterByCurrentMonth(incidenciasFiltradas)
                "Este año" -> filterByCurrentYear(incidenciasFiltradas)
                else -> incidenciasFiltradas
            }

            _incidenciasFiltradasLiveData.value = filteredList.sortedByDescending {
                try {
                    val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    dateTimeFormat.parse("${it.fecha} ${it.hora}") ?: Date(0)
                } catch (e: Exception) {
                    Date(0)
                }
            }
        }
    }
    private fun filterByToday(incidencias: List<TutoriaClass>): List<TutoriaClass> {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        return incidencias.filter {
            val date = parseDate(it.fecha, it.hora)
            date != null && date.after(today) && date.before(Date())
        }
    }
    private fun filterByLast7Days(incidencias: List<TutoriaClass>): List<TutoriaClass> {
        val last7Days = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -7)
        }.time

        return incidencias.filter {
            val date = parseDate(it.fecha, it.hora)
            date != null && date.after(last7Days) && !date.after(Date())
        }
    }
    private fun filterByCurrentMonth(incidencias: List<TutoriaClass>): List<TutoriaClass> {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        return incidencias.filter {
            val date = parseDate(it.fecha, it.hora)
            date != null && calendar.apply { time = date }.let {
                it.get(Calendar.MONTH) == currentMonth && it.get(Calendar.YEAR) == currentYear
            }
        }
    }
    private fun filterByCurrentYear(incidencias: List<TutoriaClass>): List<TutoriaClass> {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        return incidencias.filter {
            val date = parseDate(it.fecha, it.hora)
            date != null && Calendar.getInstance().apply { time = date }.get(Calendar.YEAR) == currentYear
        }
    }
    private fun parseDate(fecha: String, hora: String): Date? {
        return try {
            val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            dateTimeFormat.parse("$fecha $hora")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}