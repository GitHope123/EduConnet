package com.example.educonnet

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.educonnet.databinding.ActivityBarraNavegacionBinding
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class BarraNavegacionActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityBarraNavegacionBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var navController: NavController
    private var userType: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBarraNavegacionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicialización de Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Obtener tipo de usuario del Intent
        userType = intent.getStringExtra("USER_TYPE") ?: ""

        // Configurar Toolbar
        setSupportActionBar(binding.appBarBarraNavegacion.toolbar)

        // Configurar FAB
        binding.appBarBarraNavegacion.fab.setOnClickListener { view ->
            showUserInfoSnackbar(view)
        }

        // Configurar Navigation Controller
        navController = findNavController(R.id.nav_host_fragment_content_barra_navegacion)
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView

        // Configurar AppBarConfiguration con los fragments de nivel superior
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_perfil,
                R.id.nav_profesor,
                R.id.nav_estudiante
            ), drawerLayout
        )

        // Configurar ActionBar y NavigationView
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navView.setNavigationItemSelectedListener(this)

        // Actualizar header con datos del usuario
        updateNavigationHeader()
    }

    private fun updateNavigationHeader() {
        val navView: NavigationView = binding.navView
        val headerView = navView.getHeaderView(0)
        val usernameTextView = headerView.findViewById<TextView>(R.id.nav_header_username)
        val emailTextView = headerView.findViewById<TextView>(R.id.nav_header_email)

        // Obtener datos del usuario desde GlobalData
        val nombres = LoginActivity.GlobalData.nombresUsuario
        val apellidos = LoginActivity.GlobalData.apellidosUsuario
        val correo = LoginActivity.GlobalData.correoUsuario

        // Formatear el nombre completo
        val nombreCompleto = when {
            nombres.isNotEmpty() && apellidos.isNotEmpty() ->
                "${nombres.replaceFirstChar { it.uppercase() }} ${apellidos.replaceFirstChar { it.uppercase() }}"
            nombres.isNotEmpty() -> nombres.replaceFirstChar { it.uppercase() }
            else -> when(userType) {
                "Administrador" -> "Administrador"
                "Profesor" -> "Profesor"
                "Tutor" -> "Tutor"
                else -> "Usuario"
            }
        }

        // Asignar valores
        usernameTextView.text = nombreCompleto
        emailTextView.text = if (correo.isNotEmpty()) correo else "usuario@educonnet.com"
    }

    private fun showUserInfoSnackbar(view: android.view.View) {
        val userInfo = when(userType) {
            "Administrador" -> "Modo Administrador"
            "Profesor" -> "Modo Profesor"
            "Tutor" -> "Modo Tutor"
            else -> "Usuario General"
        }

        Snackbar.make(view, userInfo, Snackbar.LENGTH_LONG)
            .setAction("Cerrar", null)
            .setAnchorView(R.id.fab)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.barra_navegacion, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                showLogoutConfirmationDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Estás seguro de que deseas salir?")
            .setPositiveButton("Sí") { _, _ ->
                auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finishAffinity()
            }
            .setNegativeButton("No", null)
            .show()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_perfil -> {
                navController.navigate(R.id.nav_perfil)
            }
            R.id.nav_profesor -> {
                if (userType == "Administrador" || userType == "Profesor") {
                    navController.navigate(R.id.nav_profesor)
                } else {
                    Toast.makeText(this, "Acceso no autorizado", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.nav_estudiante -> {
                if (userType == "Administrador" || userType == "Tutor") {
                    navController.navigate(R.id.nav_estudiante)
                } else {
                    Toast.makeText(this, "Acceso no autorizado", Toast.LENGTH_SHORT).show()
                }
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}