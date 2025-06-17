package com.example.educonnet.ui.incidencia.estado

import com.example.educonnet.R

class Pendiente : BaseIncidenciaFragment() {
    override val estado = "Pendiente"
    override val recyclerViewId = R.id.recyclerViewIncidenciaPendiente
    override val layoutId = R.layout.fragment_pendiente
    override val progressBarId = R.id.progressBarPaginacionPendiente
}
