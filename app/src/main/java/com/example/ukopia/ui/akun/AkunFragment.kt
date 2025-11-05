package com.example.ukopia.ui.akun

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.ukopia.ui.akun.LocaleHelper // Dipertahankan sesuai konteks Anda
import com.example.ukopia.MainActivity
import com.example.ukopia.R
import com.example.ukopia.SessionManager // Dipertahankan sesuai konteks Anda
import com.example.ukopia.databinding.FragmentAkunBinding
import com.example.ukopia.databinding.FragmentLogoutConfirmationDialogBinding
import com.example.ukopia.ui.auth.LoginActivity
import com.example.ukopia.ui.auth.LupaPasswordActivity
import com.example.ukopia.ui.auth.RegisterActivity
import com.example.ukopia.ui.loyalty.LoyaltyViewModel
import android.content.res.ColorStateList // Tambahan untuk ColorStateList
import androidx.core.content.ContextCompat // Tambahan untuk ContextCompat

class AkunFragment : Fragment() {

    private var _binding: FragmentAkunBinding? = null
    private val binding get() = _binding!!

    // loyaltyViewModel tetap dipertahankan karena mungkin digunakan untuk hal lain
    private val loyaltyViewModel: LoyaltyViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAkunBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as MainActivity).setBottomNavVisibility(View.VISIBLE)

        setupClickListeners()
        updateUI()

        // Mendengarkan hasil dari LanguageChooserDialogFragment
        parentFragmentManager.setFragmentResultListener(
            LanguageChooserDialogFragment.REQUEST_KEY,
            viewLifecycleOwner
        ) { requestKey, bundle ->
            if (requestKey == LanguageChooserDialogFragment.REQUEST_KEY) {
                val selectedLanguageCode = bundle.getString(LanguageChooserDialogFragment.BUNDLE_KEY_LANGUAGE_CODE)
                selectedLanguageCode?.let { newLang ->
                    val currentLang = LocaleHelper.getLanguage(requireContext())
                    if (newLang != currentLang) {
                        LocaleHelper.setLocale(requireContext(), newLang)
                        requireActivity().recreate() // Recreate activity untuk menerapkan bahasa baru
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun setupClickListeners() {
        // Animasi flash untuk tombol Masuk
        binding.btnMasuk.setOnClickListener {
            // Definisikan warna TARGET yang diinginkan setelah flash (White Background, Black Text)
            val targetBackgroundTint = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white))
            val targetTextColors = ContextCompat.getColorStateList(requireContext(), R.color.black)

            // Definisikan warna saat flash terjadi (Black Background, White Text)
            val flashColorBackground = ContextCompat.getColor(requireContext(), R.color.black)
            val flashColorText = ContextCompat.getColor(requireContext(), R.color.white)

            // Terapkan warna flash
            binding.btnMasuk.backgroundTintList = ColorStateList.valueOf(flashColorBackground)
            binding.btnMasuk.setTextColor(flashColorText)

            // Setelah delay, kembalikan ke warna target yang diinginkan dan lakukan aksi
            Handler(Looper.getMainLooper()).postDelayed({
                binding.btnMasuk.backgroundTintList = targetBackgroundTint
                binding.btnMasuk.setTextColor(targetTextColors)
                startActivity(Intent(requireContext(), LoginActivity::class.java))
            }, 150) // Durasi animasi flash: 150 milidetik
        }

        // Animasi flash untuk tombol Daftar
        binding.btnDaftar.setOnClickListener {
            // Definisikan warna TARGET yang diinginkan setelah flash (White Background, Black Text)
            val targetBackgroundTint = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white))
            val targetTextColors = ContextCompat.getColorStateList(requireContext(), R.color.black)

            // Definisikan warna saat flash terjadi (Black Background, White Text)
            val flashColorBackground = ContextCompat.getColor(requireContext(), R.color.black)
            val flashColorText = ContextCompat.getColor(requireContext(), R.color.white)

            // Terapkan warna flash
            binding.btnDaftar.backgroundTintList = ColorStateList.valueOf(flashColorBackground)
            binding.btnDaftar.setTextColor(flashColorText)

            // Setelah delay, kembalikan ke warna target yang diinginkan dan lakukan aksi
            Handler(Looper.getMainLooper()).postDelayed({
                binding.btnDaftar.backgroundTintList = targetBackgroundTint
                binding.btnDaftar.setTextColor(targetTextColors)
                startActivity(Intent(requireContext(), RegisterActivity::class.java))
            }, 150) // Durasi animasi flash: 150 milidetik
        }

        binding.btnForgotPassword.setOnClickListener {
            val intent = Intent(requireContext(), LupaPasswordActivity::class.java).apply {
                putExtra(LupaPasswordActivity.EXTRA_SOURCE, LupaPasswordActivity.SOURCE_ACCOUNT)
            }
            startActivity(intent)
        }

        binding.btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        binding.btnChangeLanguage.setOnClickListener {
            showLanguageChooserDialog()
        }

        binding.btnInstagram1.setOnClickListener {
            openUrl("https://www.instagram.com/ukopiaindonesia/")
        }

        binding.btnInstagram2.setOnClickListener {
            openUrl("https://www.instagram.com/ruangseduhukopia/")
        }

        binding.btnYoutube.setOnClickListener {
            openUrl("https://www.youtube.com/@Ukopia_Indonesia")
        }
    }

    private fun updateUI() {
        Log.d("AkunFragmentDebug", "updateUI() called")
        binding.progressBar.visibility = View.GONE

        // PERBAIKAN: Memanggil SessionManager yang sudah disederhanakan
        if (SessionManager.isLoggedIn(requireContext())) {
            Log.d("AkunFragmentDebug", "User is logged in.")
            binding.displayName.visibility = View.VISIBLE
            binding.displayBtn.visibility = View.GONE
            binding.opsiAkun.visibility = View.VISIBLE
            binding.ivProfilePhoto.visibility = View.VISIBLE

            // PERBAIKAN: Memanggil SessionManager yang sudah disederhanakan
            val userName = SessionManager.getUserName(requireContext())
            val userEmail = SessionManager.getUserEmail(requireContext())

            Log.d("AkunFragmentDebug", "Retrieved Name: '$userName', Email: '$userEmail'")

            binding.tvNama.text = userName ?: getString(R.string.name_not_found)
            binding.tvEmail.text = userEmail ?: getString(R.string.email_not_found)

        } else {
            Log.d("AkunFragmentDebug", "User is NOT logged in.")
            binding.displayName.visibility = View.GONE
            binding.displayBtn.visibility = View.VISIBLE
            binding.opsiAkun.visibility = View.GONE
            binding.ivProfilePhoto.visibility = View.GONE
        }
    }

    private fun showLogoutConfirmationDialog() {
        Log.d("AkunFragmentDebug", "Showing custom logout confirmation dialog.")

        val dialogBinding = FragmentLogoutConfirmationDialogBinding.inflate(layoutInflater)
        val customAlertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.buttonDialogYes.setOnClickListener {
            Log.d("AkunFragmentDebug", "Logout confirmed. Calling SessionManager.logout().")
            customAlertDialog.dismiss()

            binding.progressBar.visibility = View.VISIBLE

            // PERBAIKAN: Memanggil SessionManager.logout() yang sudah ada dan disederhanakan
            SessionManager.logout(requireContext())
            Toast.makeText(requireContext(), getString(R.string.logged_out_toast), Toast.LENGTH_SHORT).show()

            // Perbarui UI setelah logout dengan sedikit penundaan
            Handler(Looper.getMainLooper()).postDelayed({
                if (isAdded && activity != null) {
                    updateUI()
                }
            }, 500)
        }

        dialogBinding.buttonDialogNo.setOnClickListener {
            Log.d("AkunFragmentDebug", "Logout cancelled.")
            customAlertDialog.dismiss()
        }

        customAlertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        customAlertDialog.show()
    }

    private fun showLanguageChooserDialog() {
        Log.d("AkunFragmentDebug", "Showing custom language chooser dialog using DialogFragment.")
        val dialog = LanguageChooserDialogFragment.newInstance()
        dialog.show(parentFragmentManager, "LanguageChooserDialogFragment")
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(requireContext(), getString(R.string.error_no_app_to_handle_link), Toast.LENGTH_SHORT).show()
            Log.w("AkunFragment", "No app can handle the URL: $url")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}