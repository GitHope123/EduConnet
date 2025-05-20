package com.example.educonnet

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.RemoteViews
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalTime
import java.util.UUID

class BarraNavegacionActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityBarraNavegacionBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var navController: NavController
    private var userType: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBarraNavegacionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        createNotificationChannel()

        userType = intent.getStringExtra("USER_TYPE") ?: ""

        setSupportActionBar(binding.appBarBarraNavegacion.toolbar)

        navController = findNavController(R.id.nav_host_fragment_content_barra_navegacion)
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_perfil,
                R.id.nav_profesor,
                R.id.nav_estudiante,
                R.id.nav_tutor,
                R.id.nav_incidencia,
                R.id.nav_tutoria,
                R.id.nav_reporte
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navView.setNavigationItemSelectedListener(this)

        updateNavigationHeader()
        configureMenuBasedOnUserType(userType)

        if (userType == "Administrador") {
            notificacionesPush()
        }
    }

    private fun updateNavigationHeader() {
        val navView = binding.navView
        val headerView = navView.getHeaderView(0)
        val usernameTextView = headerView.findViewById<TextView>(R.id.nav_header_username)
        val emailTextView = headerView.findViewById<TextView>(R.id.nav_header_email)

        val nombres = LoginActivity.GlobalData.nombresUsuario
        val apellidos = LoginActivity.GlobalData.apellidosUsuario
        val correo = LoginActivity.GlobalData.correoUsuario

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

        val saludo = saludoSegunHora()
        usernameTextView.text = "$saludo, $nombreCompleto!"
        emailTextView.text = if (correo.isNotEmpty()) correo else "usuario@educonnet.com"
    }

    private fun saludoSegunHora(): String {
        val horaActual = LocalTime.now()
        return when {
            horaActual.isBefore(LocalTime.NOON) -> "Buenos días"
            horaActual.isBefore(LocalTime.of(18, 0)) -> "Buenas tardes"
            else -> "Buenas noches"
        }
    }

    private fun configureMenuBasedOnUserType(userType: String) {
        val navMenu = binding.navView.menu
        when (userType) {
            "Administrador" -> showAdminMenuItems(navMenu)
            "Tutor" -> showTutorMenuItems(navMenu)
            "Profesor" -> showProfesorMenuItems(navMenu)
            else -> showDefaultMenuItems(navMenu)
        }
    }

    private fun notificacionesPush() {
        firestore.collection("Estudiante")
            .whereGreaterThanOrEqualTo("cantidadIncidencias", 2)
            .limit(3)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val apellidos = document.getString("apellidos") ?: ""
                    val nombres = document.getString("nombres") ?: ""
                    val nombreCompleto = "$apellidos $nombres"
                    enviarNotificacionPush(nombreCompleto)
                }
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error al obtener estudiantes con incidencias", e)
            }
    }

    @SuppressLint("RemoteViewLayout")
    private fun enviarNotificacionPush(nombres: String) {
        val message = "El estudiante $nombres ha alcanzado 2 incidencias."
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val customView = RemoteViews(packageName, R.layout.notification_layout).apply {
            setTextViewText(R.id.notification_title, "Incidencia de Estudiante")
            setTextViewText(R.id.notification_text, message)
        }

        val notificationBuilder = NotificationCompat.Builder(this, "incidencias_channel")
            .setSmallIcon(R.drawable.icon_message)
            .setCustomContentView(customView)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

        val notificationManager = NotificationManagerCompat.from(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(UUID.randomUUID().hashCode(), notificationBuilder.build())
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "incidencias_channel"
            val channelName = "Notificaciones de Incidencias"
            val channelDescription = "Notificaciones para incidencias de estudiantes"
            val channel = NotificationChannel(
                channelId, channelName, NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = channelDescription
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showAdminMenuItems(navMenu: Menu) {
        navMenu.findItem(R.id.nav_perfil).isVisible = false
        navMenu.findItem(R.id.nav_profesor).isVisible = true
        navMenu.findItem(R.id.nav_estudiante).isVisible = true
        navMenu.findItem(R.id.nav_tutor).isVisible = true
        navMenu.findItem(R.id.nav_incidencia).isVisible = true
        navMenu.findItem(R.id.nav_tutoria).isVisible = false
        navMenu.findItem(R.id.nav_reporte).isVisible = true
    }

    private fun showTutorMenuItems(navMenu: Menu) {
        navMenu.findItem(R.id.nav_perfil).isVisible = true
        navMenu.findItem(R.id.nav_profesor).isVisible = false
        navMenu.findItem(R.id.nav_estudiante).isVisible = false
        navMenu.findItem(R.id.nav_tutor).isVisible = true
        navMenu.findItem(R.id.nav_incidencia).isVisible = true
        navMenu.findItem(R.id.nav_tutoria).isVisible = true
        navMenu.findItem(R.id.nav_reporte).isVisible = false
    }

    private fun showProfesorMenuItems(navMenu: Menu) {
        navMenu.findItem(R.id.nav_perfil).isVisible = true
        navMenu.findItem(R.id.nav_profesor).isVisible = true
        navMenu.findItem(R.id.nav_estudiante).isVisible = false
        navMenu.findItem(R.id.nav_tutor).isVisible = true
        navMenu.findItem(R.id.nav_incidencia).isVisible = true
        navMenu.findItem(R.id.nav_tutoria).isVisible = false
        navMenu.findItem(R.id.nav_reporte).isVisible = true
    }

    private fun showDefaultMenuItems(navMenu: Menu) {
        navMenu.findItem(R.id.nav_perfil).isVisible = true
        navMenu.findItem(R.id.nav_profesor).isVisible = false
        navMenu.findItem(R.id.nav_estudiante).isVisible = false
        navMenu.findItem(R.id.nav_tutor).isVisible = false
        navMenu.findItem(R.id.nav_incidencia).isVisible = false
        navMenu.findItem(R.id.nav_tutoria).isVisible = false
        navMenu.findItem(R.id.nav_reporte).isVisible = false
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
            R.id.nav_perfil -> navController.navigate(R.id.nav_perfil)
            R.id.nav_profesor -> handleNavigation(R.id.nav_profesor,
                userType == "Administrador" || userType == "Profesor")
            R.id.nav_estudiante -> handleNavigation(R.id.nav_estudiante,
                userType == "Administrador" || userType == "Tutor")
            R.id.nav_tutor -> handleNavigation(R.id.nav_tutor,
                userType == "Administrador" || userType == "Tutor")
            R.id.nav_incidencia -> handleNavigation(R.id.nav_incidencia,
                userType == "Administrador" || userType == "Profesor" || userType == "Tutor")
            R.id.nav_tutoria -> handleNavigation(R.id.nav_tutoria,
                userType == "Tutor" || userType == "Profesor")
            R.id.nav_reporte -> handleNavigation(R.id.nav_reporte,
                userType == "Administrador")
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun handleNavigation(destination: Int, isAllowed: Boolean) {
        if (isAllowed) {
            navController.navigate(destination)
        } else {
            Toast.makeText(this, "Acceso no autorizado", Toast.LENGTH_SHORT).show()
        }
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