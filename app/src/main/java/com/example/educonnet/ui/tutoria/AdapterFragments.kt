package com.example.educonnet.ui.tutoria

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.example.educonnet.ui.tutoria.estado.CitadoTutoria
import com.example.educonnet.ui.tutoria.estado.CompletadoTutoria
import com.example.educonnet.ui.tutoria.estado.NotificadoTutoria
import com.example.educonnet.ui.tutoria.estado.PendienteTutoria
import com.example.educonnet.ui.tutoria.estado.RevisadoTutoria
import com.example.educonnet.ui.tutoria.estado.TodosTutoria
import com.google.android.material.tabs.TabLayout
import android.view.LayoutInflater
import com.example.educonnet.R
import com.google.android.material.chip.Chip

class AdapterFragments(fm: FragmentManager) : FragmentStatePagerAdapter(
    fm,
    BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
) {

    private val fragmentList: List<Fragment> by lazy {
        listOf(
            TodosTutoria(),
            PendienteTutoria(),
            RevisadoTutoria(),
            NotificadoTutoria(),
            CitadoTutoria(),
            CompletadoTutoria()
        )
    }

    val fragmentTitleList = listOf(
        "Todos",
        "Pendientes",
        "Revisados",
        "Notificados",
        "Citados",
        "Completados"
    )

    override fun getItem(position: Int): Fragment {
        return fragmentList[position]
    }

    override fun getPageTitle(position: Int): CharSequence {
        return fragmentTitleList[position]
    }

    override fun getCount(): Int {
        return fragmentList.size
    }

    fun setupTabLayout(tabLayout: TabLayout) {
        tabLayout.tabMode = TabLayout.MODE_SCROLLABLE
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL
        
        for (i in 0 until fragmentTitleList.size) {
            val tab = tabLayout.newTab()
            val customView = LayoutInflater.from(tabLayout.context)
                .inflate(R.layout.custom_tab, null) as Chip
            
            customView.text = fragmentTitleList[i]
            customView.isCheckable = true
            customView.isChecked = i == 0 // Set first tab as selected by default
            
            tab.customView = customView
            tabLayout.addTab(tab)
        }

        // Handle tab selection
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                (tab?.customView as? Chip)?.isChecked = true
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                (tab?.customView as? Chip)?.isChecked = false
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Handle tab reselection if needed
            }
        })
    }
}
