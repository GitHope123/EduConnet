package com.example.educonnet.ui.incidencia

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.educonnet.LoginActivity
import com.example.educonnet.databinding.FragmentIncidenciaBinding
import com.example.educonnet.ui.incidencia.estado.AdapterEstado
import com.example.educonnet.ui.incidencia.estado.IncidenciaRepository
import com.example.educonnet.ui.incidencia.estado.IncidenciaViewModel
import com.example.educonnet.ui.incidencia.estado.*

class IncidenciaFragment : Fragment() {
    private lateinit var incidenciaViewModel: IncidenciaViewModel
    private lateinit var idUsuario: String
    private lateinit var chipAdapter: AdapterEstado
    private lateinit var fragmentAdapter: FragmentAdapter
    private var isUpdatingViewPager = false

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

        setupChips()
        setupViewPager()
        init()
    }

    private fun setupChips() {
        chipAdapter = AdapterEstado { position ->
            if (!isUpdatingViewPager) {
                binding.viewPager.setCurrentItem(position, true)
            }
        }

        binding.recyclerViewChips.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = chipAdapter
        }
    }

    private fun setupViewPager() {
        fragmentAdapter = FragmentAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)
        binding.viewPager.adapter = fragmentAdapter

        binding.viewPager.registerOnPageChangeCallback(object : androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (!isUpdatingViewPager) {
                    isUpdatingViewPager = true
                    chipAdapter.updateSelectedPosition(position)
                    isUpdatingViewPager = false
                }
            }
        })
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
    }

    private fun recargarIncidencias() {
        incidenciaViewModel.cargarIncidencias(idUsuario, IncidenciaRepository())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.viewPager.unregisterOnPageChangeCallback(object : androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {})
        _binding = null
    }
}