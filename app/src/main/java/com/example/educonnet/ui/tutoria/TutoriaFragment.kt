package com.example.educonnet.ui.tutoria

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.educonnet.LoginActivity
import com.example.educonnet.R
import com.example.educonnet.databinding.FragmentTutoriaBinding
import com.example.educonnet.ui.tutoria.cita.CitaActivity
import com.example.educonnet.ui.tutoria.estado.*

class TutoriaFragment : Fragment() {

    private var _binding: FragmentTutoriaBinding? = null
    private val binding get() = _binding!!

    private lateinit var tutoriaViewModel: TutoriaViewModel
    private lateinit var grado: String
    private lateinit var seccion: String

    private lateinit var viewPager: ViewPager2
    private lateinit var tabsRecyclerView: RecyclerView
    private lateinit var adapterFragments: FragmentStateAdapter
    private lateinit var tabsAdapter: TabsAdapter

    private var currentDateFilter = "Todos"
    private var currentPosition = 0

    private val tabIcons = listOf(
        R.drawable.ic_todos,
        R.drawable.ic_pendiente,
        R.drawable.ic_revisado,
        R.drawable.ic_notificado,
        R.drawable.ic_citado,
        R.drawable.ic_completado
    )

    private val fragmentTitles = listOf(
        "Todos", "Pendientes", "Revisados", "Notificados", "Citados", "Completados"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTutoriaBinding.inflate(inflater, container, false)

        grado = LoginActivity.GlobalData.gradoUsuario.toString()
        seccion = LoginActivity.seccionUsuario

        tutoriaViewModel = ViewModelProvider(this)[TutoriaViewModel::class.java]
        tutoriaViewModel.cargarDatos(grado, seccion, TutoriaRepository())

        binding.fabCitas.setOnClickListener {
            startActivity(Intent(requireContext(), CitaActivity::class.java))
        }

        setupTabs()
        setupViewPager()

        return binding.root
    }

    private fun setupViewPager() {
        viewPager = binding.viewPager
        adapterFragments = object : FragmentStateAdapter(this) {
            override fun getItemCount() = fragmentTitles.size

            override fun createFragment(position: Int): Fragment = when (position) {
                0 -> TodosTutoria()
                1 -> PendienteTutoria()
                2 -> RevisadoTutoria()
                3 -> NotificadoTutoria()
                4 -> CitadoTutoria()
                5 -> CompletadoTutoria()
                else -> throw IllegalArgumentException("Invalid position: $position")
            }
        }

        viewPager.adapter = adapterFragments

        // Animación de deslizamiento suave
        viewPager.setPageTransformer { page, position ->
            page.alpha = 0.25f + (1 - Math.abs(position)) * 0.75f
            page.translationX = -position * page.width * 0.2f
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (currentPosition != position) {
                    currentPosition = position
                    tabsAdapter.updateSelectedPosition(position)
                    tabsRecyclerView.smoothScrollToPosition(position)
                    updateCurrentFragmentFilter()
                }
            }
        })
    }

    private fun setupTabs() {
        tabsAdapter = TabsAdapter(fragmentTitles, tabIcons) { position ->
            if (currentPosition != position) {
                viewPager.setCurrentItem(position, true)
            }
        }

        tabsRecyclerView = binding.recyclerViewChips
        tabsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = tabsAdapter
            setHasFixedSize(true)
            itemAnimator = DefaultItemAnimator()
        }
    }

    private fun updateCurrentFragmentFilter() {
        val tag = "f$currentPosition"
        val fragment = childFragmentManager.findFragmentByTag(tag)
        (fragment as? BaseTutoriaFragment)?.setDateFilter(currentDateFilter)
    }

    override fun onResume() {
        super.onResume()
        tutoriaViewModel.cargarDatos(grado, seccion, TutoriaRepository())

        binding.viewPager.post {
            binding.viewPager.currentItem = currentPosition
            tabsAdapter.updateSelectedPosition(currentPosition)
            updateCurrentFragmentFilter()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
