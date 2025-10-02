package com.example.ukopia.ui.akun

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.ukopia.MainActivity
import com.example.ukopia.R
import com.example.ukopia.SessionManager
import com.example.ukopia.databinding.FragmentLogoutConfirmationDialogBinding // Ini akan dibuat secara otomatis

class LogoutConfirmationDialogFragment : DialogFragment() {

    private var _binding: FragmentLogoutConfirmationDialogBinding ? = null
    private val binding get() = _binding!!

    // Callback untuk memberitahu Fragment induk (AkunFragment) tentang hasil logout
    interface LogoutListener {
        fun onLogoutConfirmed()
    }

    private var logoutListener: LogoutListener? = null

    // Metode untuk mengatur listener dari AkunFragment
    fun setLogoutListener(listener: LogoutListener) {
        this.logoutListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLogoutConfirmationDialogBinding .inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonDialogYes.setOnClickListener {
            // Panggil fungsi logout dari SessionManager
            SessionManager.SessionManager.logout(requireContext())

            Toast.makeText(requireContext(), "Anda telah keluar", Toast.LENGTH_SHORT).show()

            // Beritahu listener (AkunFragment) bahwa logout berhasil
            logoutListener?.onLogoutConfirmed()

            // Tutup dialog
            dismiss()

            // Kembali ke MainActivity dan clear back stack
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        binding.buttonDialogNo.setOnClickListener {
            dismiss() // Tutup dialog
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): LogoutConfirmationDialogFragment {
            return LogoutConfirmationDialogFragment()
        }
    }
}