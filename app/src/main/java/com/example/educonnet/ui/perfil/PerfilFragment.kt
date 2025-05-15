package com.example.educonnet.ui.perfil

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.educonnet.LoginActivity
import com.example.educonnet.databinding.FragmentPerfilBinding

class PerfilFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!

    // Registro para manejar el resultado de la actividad de edición
    private val editProfileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Actualizar datos cuando regresamos de editar
        updateUserDataFromGlobal()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        setupEditButton()
        loadProfessorData()
        return binding.root
    }

    private fun updateUserDataFromGlobal() {
        val globalData = LoginActivity.GlobalData
        binding.profileName.text = "${globalData.nombresUsuario} ${globalData.apellidosUsuario}"
        binding.profilePhone.text = globalData.celularUsuario.toString()
        binding.profileEmail.text = globalData.correoUsuario
    }

    private fun loadProfessorData() {
        updateUserDataFromGlobal()
    }

    private fun setupEditButton() {
        binding.btnEditar.setOnClickListener {
            val globalData = LoginActivity.GlobalData
            val intent = Intent(requireContext(), EditPerfil::class.java).apply {
                putExtra("nombre", globalData.nombresUsuario)
                putExtra("apellido", globalData.apellidosUsuario)
                putExtra("celular", globalData.celularUsuario.toString())
                putExtra("correo", globalData.correoUsuario)
                putExtra("password", globalData.passwordUsuario)
                putExtra("id", globalData.idUsuario)
                putExtra("isTutor", globalData.tutor)
            }
            editProfileLauncher.launch(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}