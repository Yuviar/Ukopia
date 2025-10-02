package com.example.ukopia.ui.akun

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.ukopia.LoginActivity
import com.example.ukopia.LupaPasswordActivity
import com.example.ukopia.MainActivity // <<-- TAMBAHKAN INI
import com.example.ukopia.R
import com.example.ukopia.RegisterActivity
import com.example.ukopia.SessionManager
import com.example.ukopia.databinding.FragmentAkunBinding

import android.app.AlertDialog
import androidx.fragment.app.viewModels

class AkunFragment : Fragment(), LogoutConfirmationDialogFragment.LogoutListener {
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

        // ▼▼▼ Pastikan nav bar terlihat di AkunFragment ▼▼▼
        (requireActivity() as MainActivity).setBottomNavVisibility(View.VISIBLE)

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
        if (SessionManager.SessionManager.isLoggedIn(requireContext())) {
            binding.displayBtn.visibility = View.GONE
            binding.displayName.visibility = View.VISIBLE
            binding.opsiAkun.visibility = View.VISIBLE

            val userName = SessionManager.SessionManager.getUserName(requireContext())
            val userEmail = SessionManager.SessionManager.getUserEmail(requireContext())

            binding.tvNama.text = userName ?: "Nama Tidak Ditemukan"
            binding.tvEmail.text = userEmail ?: "Email Tidak Ditemukan"

        } else {
            binding.displayBtn.visibility = View.VISIBLE
            binding.displayName.visibility = View.GONE
            binding.opsiAkun.visibility = View.GONE
        }
    }

    private fun setupClickListeners() {
        binding.btnMasuk.setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
        }

        binding.btnDaftar.setOnClickListener {
            val intent = Intent(requireContext(), RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.btnForgotPassword.setOnClickListener {
            val intent = Intent(requireContext(), LupaPasswordActivity::class.java).apply {
                putExtra(LupaPasswordActivity.EXTRA_SOURCE, LupaPasswordActivity.SOURCE_AKUN)
            }
            startActivity(intent)
        }

        binding.btnLogout.setOnClickListener {
            val dialog = LogoutConfirmationDialogFragment.newInstance()
            dialog.setLogoutListener(this)
            dialog.show(parentFragmentManager, "LogoutConfirmationDialog")
        }

        val instagramUrl1 = "https://www.instagram.com/ukopiaindonesia"
        val instagramUrl2 = "https://www.instagram.com/ruangseduhukopia"
        val youtubeUrl = "https://www.youtube.com/@Ukopia_Indonesia"

        binding.btnInstagram1.setOnClickListener { openUrl(instagramUrl1) }
        binding.btnInstagram2.setOnClickListener { openUrl(instagramUrl2) }
        binding.btnYoutube.setOnClickListener { openUrl(youtubeUrl) }

        binding.btnPeralatan.setOnClickListener { listener!!.OnPeralatanClicked() }
    }

    override fun onLogoutConfirmed() {
        setupUserInterface()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}