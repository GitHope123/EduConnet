package com.example.educonnet.ui.incidencia.estado

import com.example.educonnet.R

class Notificado : BaseIncidenciaFragment() {
    override val estado = "Notificado"
    override val recyclerViewId = R.id.recyclerViewIncidenciaNotificado
    override val layoutId = R.layout.fragment_notificado
    override val progressBarId = R.id.progressBarPaginacionNotificado
}