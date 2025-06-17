package com.example.educonnet.ui.incidencia.estado

import com.example.educonnet.R

class Todo : BaseIncidenciaFragment() {
    override val estado = "Todos" // Estado "Todos" para mostrar todas las incidencias
    override val recyclerViewId = R.id.recyclerViewIncidenciaTodo
    override val layoutId = R.layout.fragment_todo
    override val progressBarId = R.id.progressBarPaginacionTodo
}
