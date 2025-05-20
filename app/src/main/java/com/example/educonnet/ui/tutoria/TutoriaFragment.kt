package com.example.educonnet.ui.tutoria

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.example.educonnet.LoginActivity
import com.example.educonnet.databinding.FragmentTutoriaBinding
import com.google.android.material.tabs.TabLayout


class TutoriaFragment : Fragment() {
    private var _binding: FragmentTutoriaBinding? = null
    private val binding get() = _binding!!
    private lateinit var tutoriaViewModel: TutoriaViewModel
    private lateinit var grado: String
    private lateinit var nivel: String


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTutoriaBinding.inflate(inflater, container, false)
        grado= LoginActivity.GlobalData.gradoUsuario.toString()
        nivel = LoginActivity.nivelUsuario
        tutoriaViewModel = ViewModelProvider(this).get(TutoriaViewModel::class.java)
        tutoriaViewModel.cargarDatos(grado, nivel, TutoriaRepository())

        setupViewPagerAndTabs()

        return binding.root
    }


    private fun setupViewPagerAndTabs() {
        val viewPager: ViewPager = binding.viewPager
        val tabLayout: TabLayout = binding.tabLayout

        val adapter = AdapterFragments(childFragmentManager)
        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        tutoriaViewModel.cargarDatos(grado, nivel, TutoriaRepository())
        _binding?.viewPager?.post {
            _binding!!.viewPager?.currentItem = 0
        }
    }
}