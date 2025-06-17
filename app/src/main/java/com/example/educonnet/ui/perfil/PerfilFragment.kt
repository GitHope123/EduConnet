package com.example.educonnet.ui.perfil

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.educonnet.R
import com.example.educonnet.databinding.FragmentPerfilBinding
import com.example.educonnet.ui.incidencia.AgregarEstudiantes
import com.example.educonnet.ui.incidencia.IncidenciaFragment
import com.example.educonnet.ui.tutoria.cita.CitaActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class PerfilFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: PerfilViewModel

    // Registro para manejar el resultado de la actividad de edición
    private val editProfileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.loadUserData()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[PerfilViewModel::class.java]

        setupEditButton()
        observeUserData()
        return binding.root
    }

    private fun observeUserData() {
        viewModel.userData.observe(viewLifecycleOwner) { userData ->
            binding.profileName.text = "${userData.nombre} ${userData.apellido}"
            binding.profilePhone.text = userData.celular
            binding.profileEmail.text = userData.correo

            setupQuickAccessButtons(userData.isTutor)
        }
    }

    private fun setupEditButton() {
        binding.btnEditar.setOnClickListener {
            viewModel.userData.value?.let { userData ->
                val intent = Intent(requireContext(), EditPerfil::class.java).apply {
                    putExtra("nombre", userData.nombre)
                    putExtra("apellido", userData.apellido)
                    putExtra("celular", userData.celular)
                    putExtra("correo", userData.correo)
                    putExtra("password", userData.password)
                    putExtra("id", userData.id)
                    putExtra("isTutor", userData.isTutor)
                }
                editProfileLauncher.launch(intent)
            }
        }
    }

    private fun setupQuickAccessButtons(isTutor: Boolean) {
        // Botón para generar incidencia
        binding.btnSeleccionarEstudiante.setOnClickListener {
            val intent = Intent(requireContext(), AgregarEstudiantes::class.java)
            startActivity(intent)
        }

        // Botón para ver citas: visible solo si es tutor
        if (isTutor) {
            binding.btnVerCitas.visibility = View.VISIBLE
            binding.btnVerCitas.setOnClickListener {
                val intent = Intent(requireContext(), CitaActivity::class.java)
                startActivity(intent)
            }
        } else {
            binding.btnVerCitas.visibility = View.GONE
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
