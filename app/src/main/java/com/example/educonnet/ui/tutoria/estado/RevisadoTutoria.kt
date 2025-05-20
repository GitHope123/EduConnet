package com.example.educonnet.ui.tutoria.estado
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.educonnet.LoginActivity
import com.example.educonnet.R
import com.example.educonnet.databinding.FragmentRevisadoTutoriaBinding
import com.example.educonnet.ui.tutoria.TutoriaAdapter
import com.example.educonnet.ui.tutoria.TutoriaClass
import com.example.educonnet.ui.tutoria.TutoriaRepository
import com.example.educonnet.ui.tutoria.TutoriaViewModel
import com.google.firebase.auth.FirebaseAuth


class RevisadoTutoria : Fragment() {
    private var _binding: FragmentRevisadoTutoriaBinding? = null
    private val binding get() = _binding!!
    private lateinit var tutoriaAdapter: TutoriaAdapter
    private val listaTutorias  : MutableList<TutoriaClass> = mutableListOf()
    private val listaFilter  : MutableList<TutoriaClass> = mutableListOf()
    private val tutoriaRepository = TutoriaRepository()
    private var currentFilter: String = "Todos"
    private lateinit var idUsuario:String
    private lateinit var tutoriaViewModel: TutoriaViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRevisadoTutoriaBinding.inflate(inflater, container, false)
        tutoriaViewModel = ViewModelProvider(requireParentFragment()).get(TutoriaViewModel::class.java)

        init()
        resetAutoComplete()
        val email = FirebaseAuth.getInstance().currentUser?.email
        email?.let {
            loadTutoriasForTutor(it, currentFilter) // Se pasa "Todos" como filtro por defecto
        }
        return binding.root
    }


    private fun init() {
        binding.recyclerViewTutorias.layoutManager = LinearLayoutManager(context)
        tutoriaAdapter = TutoriaAdapter(listaTutorias)
        binding.recyclerViewTutorias.adapter = tutoriaAdapter
    }

    private fun resetAutoComplete() {
        val fechaTutoria = resources.getStringArray(R.array.item_fecha_tutoria)
        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.list_tutoria, fechaTutoria)
        arrayAdapter.setDropDownViewResource(R.layout.list_tutoria)
        binding.autoComplete.setAdapter(arrayAdapter)
        binding.autoComplete.text.clear()
        binding.autoComplete.setText(currentFilter, false)
        binding.autoComplete.setOnItemClickListener { parent, _, position, _ ->
            val selectedItem = parent.getItemAtPosition(position) as String
            applyFilter(selectedItem)
        }
    }

    private fun applyFilter(selection: String) {
        currentFilter= selection
        idUsuario = LoginActivity.GlobalData.idUsuario
        loadTutoriasForTutor(idUsuario, selection)
    }
    private fun loadTutoriasForTutor(id: String, filtroFecha: String) {
        listaTutorias.clear() // Asegurarse de limpiar la lista antes
        tutoriaViewModel.filtrarIncidenciasPorEstado("Revisado",filtroFecha)
        tutoriaViewModel.incidenciasFiltradasLiveData.observe(viewLifecycleOwner) { incidencias ->
            listaTutorias.addAll(incidencias) // Agregar los datos cargados
            tutoriaAdapter.updateData(listaTutorias) // Actualizar la vista
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Limpia la referencia a la vista enlazada
    }

    override fun onResume() {
        super.onResume()
        currentFilter = "Todos"
        resetAutoComplete()
        idUsuario = LoginActivity.GlobalData.idUsuario
        loadTutoriasForTutor(idUsuario, currentFilter)
    }
}