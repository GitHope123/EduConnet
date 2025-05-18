package com.example.educonnet.ui.reporte

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.educonnet.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class ReporteFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflar el layout del fragmento
        return inflater.inflate(R.layout.fragment_reporte, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val copyButton = view.findViewById<MaterialButton>(R.id.buttonOpenLink)
        val shareButton = view.findViewById<MaterialButton>(R.id.imageButtonCompartir)
        val editTextLink = view.findViewById<TextInputEditText>(R.id.editTextLink)

        val url = "https://lookerstudio.google.com/reporting/861f85b1-4e07-4e3e-b3da-c863a4f02d45"

        editTextLink.setText(url)
        editTextLink.isFocusable = false
        editTextLink.isClickable = false

        copyButton.setOnClickListener {
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Report URL", url)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(requireContext(), "URL copiada", Toast.LENGTH_SHORT).show()
        }

        shareButton.setOnClickListener {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "Mira este reporte: $url")
                type = "text/plain"
            }
            startActivity(Intent.createChooser(sendIntent, null))
        }
    }
}