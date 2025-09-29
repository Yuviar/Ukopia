package com.example.ukopia.ui.akun


import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.example.ukopia.LoginActivity
import com.example.ukopia.MainActivity
import com.example.ukopia.R
import com.example.ukopia.RegisterActivity
import com.example.ukopia.SessionManager
import com.example.ukopia.databinding.FragmentAkunBinding

class AkunFragment : Fragment() {
    private var _binding: FragmentAkunBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance() = AkunFragment()
    }

    interface OnAkunFragmentInteractionListener {
        fun OnPeralatanClicked()
    }

    private var listener: OnAkunFragmentInteractionListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnAkunFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + "must implement OnAkunFragmentInteractionListener")
        }
    }

    private val viewModel: AkunViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAkunBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUserInterface()
        setupClickListeners()
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                requireContext(),
                "Tidak ada aplikasi yang dapat membuka URL ini",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setupUserInterface() {
        // Gunakan SessionManager yang sudah diperbaiki (tanpa object ganda)
        if (SessionManager.SessionManager.isLoggedIn(requireContext())) {
            // --- JIKA SUDAH LOGIN ---
            // Sembunyikan tombol Masuk/Daftar
            binding.displayBtn.visibility = View.GONE
            // Tampilkan layout nama/email dan tombol logout
            binding.displayName.visibility = View.VISIBLE
            binding.btnLogout.visibility = View.VISIBLE

            // Ambil data dari SessionManager dan tampilkan
            val userName = SessionManager.SessionManager.getUserName(requireContext())
            val userEmail = SessionManager.SessionManager.getUserEmail(requireContext())

            binding.tvNama.text = userName ?: "Nama Tidak Ditemukan"
            binding.tvEmail.text = userEmail ?: "Email Tidak Ditemukan"

        } else {
            // --- JIKA BELUM LOGIN ---
            // Tampilkan tombol Masuk/Daftar
            binding.displayBtn.visibility = View.VISIBLE
            // Sembunyikan layout nama/email dan tombol logout
            binding.displayName.visibility = View.GONE
            binding.btnLogout.visibility = View.GONE
        }
    }
    private fun setupClickListeners() {
        // Gunakan binding untuk semua view
        binding.btnMasuk.setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
        }

        binding.btnDaftar.setOnClickListener {
            val intent = Intent(requireContext(), RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.btnLogout.setOnClickListener {
            // Panggil fungsi logout dari SessionManager
            SessionManager.SessionManager.logout(requireContext())

            // Beri tahu pengguna bahwa mereka telah logout
            Toast.makeText(requireContext(), "Anda telah keluar", Toast.LENGTH_SHORT).show()

            // Muat ulang activity utama untuk merefresh semua state
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        // Listener lain tidak berubah
        val instagramUrl1 = "https://www.instagram.com/ukopiaindonesia"
        val instagramUrl2 = "https://www.instagram.com/ruangseduhukopia"
        val youtubeUrl = "https://www.youtube.com/@Ukopia_Indonesia"

        binding.btnInstagram1.setOnClickListener { openUrl(instagramUrl1) }
        binding.btnInstagram2.setOnClickListener { openUrl(instagramUrl2) }
        binding.btnYoutube.setOnClickListener { openUrl(youtubeUrl) }
        binding.btnPeralatan.setOnClickListener { listener?.OnPeralatanClicked() }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}