package com.example.educonnet.ui.incidencia.estado

import com.example.educonnet.R

class Citado : BaseIncidenciaFragment() {
    override val estado = "Citado"
    override val recyclerViewId = R.id.recyclerViewIncidenciaCitado
    override val layoutId = R.layout.fragment_citado
    override val progressBarId = R.id.progressBarPaginacionCitado // Añade esta línea
}