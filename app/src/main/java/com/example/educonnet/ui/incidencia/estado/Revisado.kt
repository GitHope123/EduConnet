package com.example.educonnet.ui.incidencia.estado

import com.example.educonnet.R

class Revisado : BaseIncidenciaFragment() {
    override val estado = "Revisado"
    override val recyclerViewId = R.id.recyclerViewIncidenciaRevisado
    override val layoutId = R.layout.fragment_revisado
    override val progressBarId = R.id.progressBarPaginacionRevisado
}
