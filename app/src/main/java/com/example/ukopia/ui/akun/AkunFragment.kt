package com.example.ukopia.ui.akun


import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.ukopia.LoginActivity
import com.example.ukopia.R
import com.example.ukopia.RegisterActivity
import com.example.ukopia.SessionManager

class AkunFragment : Fragment() {

    companion object {
        fun newInstance() = AkunFragment()
    }

    private val viewModel: AkunViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_akun, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val btnMasuk = view.findViewById<Button>(R.id.btnMasuk)
        val btnDaftar = view.findViewById<Button>(R.id.btnDaftar)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)

        btnMasuk?.setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
        }
        btnDaftar?.setOnClickListener {
            val intent = Intent(requireContext(), RegisterActivity::class.java)
            startActivity(intent)
        }
        if(SessionManager.SessionManager.isLoggedIn(requireContext())){
            btnLogout.visibility = View.VISIBLE
        }else{
            btnLogout.visibility = View.GONE
        }
    }
}