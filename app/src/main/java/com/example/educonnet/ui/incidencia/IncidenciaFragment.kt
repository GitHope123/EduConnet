package com.example.educonnet.ui.incidencia

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.educonnet.LoginActivity
import com.example.educonnet.databinding.FragmentIncidenciaBinding
import com.example.educonnet.ui.incidencia.estado.AdapterEstado
import com.example.educonnet.ui.incidencia.estado.IncidenciaRepository
import com.example.educonnet.ui.incidencia.estado.IncidenciaViewModel

class IncidenciaFragment : Fragment() {
    private lateinit var incidenciaViewModel: IncidenciaViewModel
    private lateinit var idUsuario: String

    // Usamos View Binding para gestionar las vistas
    private var _binding: FragmentIncidenciaBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Asegurarse de que el ID de usuario esté disponible
        idUsuario = LoginActivity.GlobalData.idUsuario

        // Inicializar el ViewModel
        incidenciaViewModel = ViewModelProvider(this)[IncidenciaViewModel::class.java]
        incidenciaViewModel.cargarIncidencias(idUsuario, IncidenciaRepository())
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflar el diseño usando View Binding
        _binding = FragmentIncidenciaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configurar el ViewPager y el TabLayout
        binding.viewPager.adapter = AdapterEstado(childFragmentManager)
        binding.tabLayout.setupWithViewPager(binding.viewPager)

        // Establecer la pestaña "Todos" como la predeterminada
        binding.viewPager.post {
            binding.viewPager.currentItem = 0
        }

        // Inicializar el botón para agregar incidencias
        init()
    }

    private fun init() {
        // Configurar el botón de agregar incidencia
        binding.btnAgregarIncidencia.setOnClickListener {
            val intent = Intent(requireContext(), AgregarEstudiantes::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()

        // Recargar incidencias
        recargarIncidencias()

        // Asegurarse de que la pestaña "Todos" esté seleccionada al volver
        binding.viewPager.post {
            binding.viewPager.currentItem = 0
        }
    }

    private fun recargarIncidencias() {
        incidenciaViewModel.cargarIncidencias(idUsuario, IncidenciaRepository())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}