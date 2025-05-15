package com.example.educonnet.ui.estudiante

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SlideshowViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is estudiante Fragment"
    }
    val text: LiveData<String> = _text
}