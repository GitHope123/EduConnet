package com.example.educonnet.ui.incidencia.estado

import com.example.educonnet.R

class Completado : BaseIncidenciaFragment() {
    override val estado = "Completado"
    override val recyclerViewId = R.id.recyclerViewIncidenciaCompletado
    override val layoutId = R.layout.fragment_completado
    override val progressBarId = R.id.progressBarPaginacionCompletado // Añade esta línea
}