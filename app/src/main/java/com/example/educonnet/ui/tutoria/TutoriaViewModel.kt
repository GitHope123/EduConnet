package com.example.educonnet.ui.tutoria

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.*

class TutoriaViewModel : ViewModel() {

    private val _incidenciasLiveData = MutableLiveData<List<TutoriaClass>>()
    private val _incidenciasFiltradasLiveData = MutableLiveData<List<TutoriaClass>>()

    val incidenciasLiveData: LiveData<List<TutoriaClass>> get() = _incidenciasLiveData
    val incidenciasFiltradasLiveData: LiveData<List<TutoriaClass>> get() = _incidenciasFiltradasLiveData

    private val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "ES"))

    private var currentEstadoFilter: String = ""
    private var currentDateFilter: String = "Todos"

    companion object {
        val DATE_FILTER_OPTIONS = listOf(
            "Todos",
            "Hoy",
            "Últimos 7 días",
            "Este mes",
            "Este año"
        )
    }

    fun cargarDatos(grado: String, seccion: String, repositorio: TutoriaRepository) {
        repositorio.getIncidenciasPorGradoSeccion(grado.toInt(), seccion) { incidencias ->
            _incidenciasLiveData.value = incidencias
            aplicarFiltros()
        }
    }

    fun filtrarIncidenciasPorEstado(estado: String, filtroFecha: String) {
        currentEstadoFilter = estado
        currentDateFilter = filtroFecha
        aplicarFiltros()
    }

    fun getCurrentDateFilter(): String = currentDateFilter

    private fun aplicarFiltros() {
        _incidenciasLiveData.value?.let { incidencias ->
            val filtradasPorEstado = if (currentEstadoFilter.isBlank()) {
                incidencias
            } else {
                incidencias.filter { it.estado.equals(currentEstadoFilter, ignoreCase = true) }
            }

            val filtradasPorFecha = when (currentDateFilter) {
                "Hoy" -> filterByToday(filtradasPorEstado)
                "Últimos 7 días" -> filterByLast7Days(filtradasPorEstado)
                "Este mes" -> filterByCurrentMonth(filtradasPorEstado)
                "Este año" -> filterByCurrentYear(filtradasPorEstado)
                else -> filtradasPorEstado
            }

            _incidenciasFiltradasLiveData.value = ordenarPorFecha(filtradasPorFecha)
        }
    }

    private fun ordenarPorFecha(lista: List<TutoriaClass>): List<TutoriaClass> {
        return lista.sortedByDescending {
            parseDate(it.fecha, it.hora) ?: Date(0)
        }
    }

    private fun filterByToday(incidencias: List<TutoriaClass>): List<TutoriaClass> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.time

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val end = calendar.time

        return incidencias.filter {
            parseDate(it.fecha, it.hora)?.let { date -> date in start..end } == true
        }
    }

    private fun filterByLast7Days(incidencias: List<TutoriaClass>): List<TutoriaClass> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -6)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.time

        calendar.add(Calendar.DAY_OF_YEAR, 6)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val end = calendar.time

        return incidencias.filter {
            parseDate(it.fecha, it.hora)?.let { date -> date in start..end } == true
        }
    }

    private fun filterByCurrentMonth(incidencias: List<TutoriaClass>): List<TutoriaClass> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.time

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val end = calendar.time

        return incidencias.filter {
            parseDate(it.fecha, it.hora)?.let { date -> date in start..end } == true
        }
    }

    private fun filterByCurrentYear(incidencias: List<TutoriaClass>): List<TutoriaClass> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_YEAR, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.time

        calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val end = calendar.time

        return incidencias.filter {
            parseDate(it.fecha, it.hora)?.let { date -> date in start..end } == true
        }
    }

    private fun parseDate(fecha: String, hora: String): Date? {
        return try {
            dateTimeFormat.parse("$fecha $hora")
        } catch (e: Exception) {
            null
        }
    }
}
