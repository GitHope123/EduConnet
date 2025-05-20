package com.example.educonnet.ui.tutoria
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.example.educonnet.ui.tutoria.estado.PendienteTutoria
import com.example.educonnet.ui.tutoria.estado.RevisadoTutoria
import com.example.educonnet.ui.tutoria.estado.TodosTutoria


class AdapterFragments(fm: FragmentManager) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val fragmentList: List<Fragment> by lazy {
        listOf(
            TodosTutoria(), // Fragmento que muestra todas las tutorías
            PendienteTutoria(), // Fragmento que muestra las tutorías pendientes
            RevisadoTutoria() // Fragmento que muestra las tutorías revisadas
        )
    }

    private val fragmentTitleList = listOf("Todos", "Pendientes", "Revisadas")

    override fun getItem(position: Int): Fragment {
        return fragmentList[position]
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return fragmentTitleList[position]
    }

    override fun getCount(): Int {
        return fragmentList.size
    }
}