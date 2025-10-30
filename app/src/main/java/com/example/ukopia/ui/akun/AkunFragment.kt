package com.example.ukopia.ui.akun

import android.content.Context
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
import com.example.ukopia.LocaleHelper
import com.example.ukopia.LoginActivity
import com.example.ukopia.MainActivity
import com.example.ukopia.R
import com.example.ukopia.RegisterActivity
import com.example.ukopia.SessionManager
import com.example.ukopia.databinding.FragmentAkunBinding
import com.example.ukopia.databinding.FragmentLogoutConfirmationDialogBinding
import com.example.ukopia.ui.auth.LupaPasswordActivity
import com.example.ukopia.ui.loyalty.LoyaltyViewModel

class AkunFragment : Fragment() {

    private var _binding: FragmentAkunBinding? = null
    private val binding get() = _binding!!

    // loyaltyViewModel tetap dipertahankan karena mungkin digunakan untuk hal lain (misal: mengambil total poin, dll)
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

        // --- HAPUS blok ini yang berkaitan dengan badge ---
        // loyaltyViewModel.loyaltyUserStatus.observe(viewLifecycleOwner) {
        //     Log.d("AkunFragmentDebug", "Loyalty status observed change. Updating badge.")
        //     updateLoyaltyBadge()
        // }
        // --- Akhir blok yang dihapus ---

        // --- Tambahkan ini untuk mendengarkan hasil dari LanguageChooserDialogFragment ---
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
        // --- Akhir penambahan ---
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun setupClickListeners() {
        binding.btnMasuk.setOnClickListener {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
        }

        binding.btnDaftar.setOnClickListener {
            startActivity(Intent(requireContext(), RegisterActivity::class.java))
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
            // Gunakan URL handle Instagram langsung
            openUrl("https://www.instagram.com/ukopiaindonesia/")
        }

        binding.btnInstagram2.setOnClickListener {
            // Gunakan URL handle Instagram langsung
            openUrl("https://www.instagram.com/ruangseduhukopia/")
        }

        binding.btnYoutube.setOnClickListener {
            // Gunakan URL handle YouTube langsung
            openUrl("https://www.youtube.com/@Ukopia_Indonesia")
        }
    }

    private fun updateUI() {
        Log.d("AkunFragmentDebug", "updateUI() called")
        // Pastikan progressBar selalu tersembunyi setelah updateUI selesai,
        // Ini harus selalu menjadi baris pertama agar ProgressBar hilang setelah operasi selesai.
        binding.progressBar.visibility = View.GONE
        if (SessionManager.SessionManager.isLoggedIn(requireContext())) {
            Log.d("AkunFragmentDebug", "User is logged in.")
            binding.displayName.visibility = View.VISIBLE
            binding.displayBtn.visibility = View.GONE
            binding.opsiAkun.visibility = View.VISIBLE
            binding.ivProfilePhoto.visibility = View.VISIBLE

            val userName = SessionManager.SessionManager.getUserName(requireContext())
            val userEmail = SessionManager.SessionManager.getUserEmail(requireContext())

            Log.d("AkunFragmentDebug", "Retrieved Name: '$userName', Email: '$userEmail'")

            binding.tvNama.text = userName ?: getString(R.string.name_not_found)
            binding.tvEmail.text = userEmail ?: getString(R.string.email_not_found)

            // --- HAPUS panggilan ini ---
            // updateLoyaltyBadge()
            // --- Akhir yang dihapus ---
        } else {
            Log.d("AkunFragmentDebug", "User is NOT logged in.")
            binding.displayName.visibility = View.GONE
            binding.displayBtn.visibility = View.VISIBLE
            binding.opsiAkun.visibility = View.GONE
            binding.ivProfilePhoto.visibility = View.GONE

            // --- HAPUS panggilan ini ---
            // binding.ivLoyaltyBadge.visibility = View.GONE
            // --- Akhir yang dihapus ---
        }
    }

    // --- HAPUS seluruh fungsi updateLoyaltyBadge() ini ---
    /*
    private fun updateLoyaltyBadge() {
        if (SessionManager.SessionManager.isLoggedIn(requireContext())) {
            val (_, badgeDrawableId) = loyaltyViewModel.getLoyaltyLevel(requireContext())
            binding.ivLoyaltyBadge.setImageResource(badgeDrawableId)
            binding.ivLoyaltyBadge.visibility = View.VISIBLE
            Log.d("AkunFragmentDebug", "Loyalty badge updated with drawable ID: $badgeDrawableId")
        } else {
            binding.ivLoyaltyBadge.visibility = View.GONE
            Log.d("AkunFragmentDebug", "User not logged in, hiding loyalty badge.")
        }
    }
    */
    // --- Akhir blok yang dihapus ---

    private fun showLogoutConfirmationDialog() {
        Log.d("AkunFragmentDebug", "Showing custom logout confirmation dialog.")

        val dialogBinding = FragmentLogoutConfirmationDialogBinding.inflate(layoutInflater)
        val customAlertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.buttonDialogYes.setOnClickListener {
            Log.d("AkunFragmentDebug", "Logout confirmed. Calling SessionManager.logout().")
            customAlertDialog.dismiss() // Tutup dialog konfirmasi terlebih dahulu

            binding.progressBar.visibility = View.VISIBLE // <--- Tampilkan progress bar

            // Lakukan logout
            SessionManager.SessionManager.logout(requireContext())
            Toast.makeText(requireContext(), getString(R.string.logged_out_toast), Toast.LENGTH_SHORT).show()

            // Perbarui UI setelah logout dengan sedikit penundaan
            // Ini memberi waktu ProgressBar untuk terlihat dan beranimasi sebentar.
            // Sesuaikan durasi penundaan sesuai kebutuhan (misalnya, 500ms atau 1000ms).
            Handler(Looper.getMainLooper()).postDelayed({
                // Pastikan fragment masih melekat ke aktivitas sebelum memanggil updateUI()
                if (isAdded && activity != null) {
                    updateUI() // updateUI akan menyembunyikan progressBar secara otomatis setelah selesai
                }
            }, 500) // Penundaan 500 milidetik (0.5 detik)
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
        // Periksa apakah ada aplikasi yang bisa menangani intent ini
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        } else {
            // Beri tahu pengguna jika tidak ada aplikasi yang sesuai
            Toast.makeText(requireContext(), getString(R.string.error_no_app_to_handle_link), Toast.LENGTH_SHORT).show()
            Log.w("AkunFragment", "No app can handle the URL: $url")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}