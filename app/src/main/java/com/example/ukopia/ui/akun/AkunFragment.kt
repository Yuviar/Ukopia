// D:/github_rama/Ukopia/app/src/main/java/com/example/ukopia/ui/akun/AkunFragment.kt

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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.ukopia.MainActivity
import com.example.ukopia.R
import com.example.ukopia.SessionManager
import com.example.ukopia.databinding.FragmentAkunBinding
import com.example.ukopia.databinding.FragmentLogoutConfirmationDialogBinding
import com.example.ukopia.ui.auth.LoginActivity
import com.example.ukopia.ui.auth.LupaPasswordActivity
import com.example.ukopia.ui.auth.RegisterActivity
import com.example.ukopia.ui.loyalty.LoyaltyViewModel
import android.content.res.ColorStateList
import android.content.pm.PackageManager // Tambahkan ini
import android.content.pm.ResolveInfo // Tambahkan ini

class AkunFragment : Fragment() {

    private var _binding: FragmentAkunBinding? = null
    private val binding get() = _binding!!

    private val loyaltyViewModel: LoyaltyViewModel by activityViewModels()
    private val TAG = "AkunFragment"

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
                        requireActivity().recreate()
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

        binding.btnMasuk.setOnClickListener {
            val targetBackgroundTint = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white))
            val targetTextColors = ContextCompat.getColorStateList(requireContext(), R.color.black)

            val flashColorBackground = ContextCompat.getColor(requireContext(), R.color.black)
            val flashColorText = ContextCompat.getColor(requireContext(), R.color.white)

            binding.btnMasuk.backgroundTintList = ColorStateList.valueOf(flashColorBackground)
            binding.btnMasuk.setTextColor(flashColorText)

            Handler(Looper.getMainLooper()).postDelayed({
                binding.btnMasuk.backgroundTintList = targetBackgroundTint
                binding.btnMasuk.setTextColor(targetTextColors)
                startActivity(Intent(requireContext(), LoginActivity::class.java))
            }, 150)
        }

        binding.btnDaftar.setOnClickListener {
            val targetBackgroundTint = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white))
            val targetTextColors = ContextCompat.getColorStateList(requireContext(), R.color.black)

            val flashColorBackground = ContextCompat.getColor(requireContext(), R.color.black)
            val flashColorText = ContextCompat.getColor(requireContext(), R.color.white)

            binding.btnDaftar.backgroundTintList = ColorStateList.valueOf(flashColorBackground)
            binding.btnDaftar.setTextColor(flashColorText)

            Handler(Looper.getMainLooper()).postDelayed({
                binding.btnDaftar.backgroundTintList = targetBackgroundTint
                binding.btnDaftar.setTextColor(targetTextColors)
                startActivity(Intent(requireContext(), RegisterActivity::class.java))
            }, 150)
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
            openUrl("https://www.instagram.com/ukopiaindonesia?igsh=MWN2ODBrdTZ4bDJoOA==")
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
        if (_binding == null) {
            Log.w(TAG, "updateUI: View destroyed, skipping UI update.")
            return
        }
        binding.progressBar.visibility = View.GONE

        if (SessionManager.isLoggedIn(requireContext())) {
            Log.d("AkunFragmentDebug", "User is logged in.")
            binding.displayName.visibility = View.VISIBLE
            binding.displayBtn.visibility = View.GONE
            binding.opsiAkun.visibility = View.VISIBLE
            binding.ivProfilePhoto.visibility = View.VISIBLE

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

            if (_binding == null) {
                Log.w(TAG, "showLogoutConfirmationDialog: View destroyed before progressBar access.")
                return@setOnClickListener
            }
            binding.progressBar.visibility = View.VISIBLE

            SessionManager.logout(requireContext())
            Toast.makeText(requireContext(), getString(R.string.logged_out_toast), Toast.LENGTH_SHORT).show()

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
        intent.addCategory(Intent.CATEGORY_BROWSABLE)

        val packageManager = requireActivity().packageManager

        // --- Logging Detail START ---
        Log.d(TAG, "--- Attempting to open URL ---")
        Log.d(TAG, "URL: $url")
        Log.d(TAG, "Intent Action: ${intent.action}")
        Log.d(TAG, "Intent Data: ${intent.data}")
        Log.d(TAG, "Intent Categories: ${intent.categories}")
        // --- Logging Detail END ---

        // Menggunakan 0 untuk flag untuk mendapatkan *semua* aktivitas yang mungkin, tidak hanya default.
        // Ini adalah perubahan utama untuk debugging.
        val resolvedActivity: ResolveInfo? = try {
            packageManager.resolveActivity(intent, 0) // Perubahan di sini: dari MATCH_DEFAULT_ONLY menjadi 0
        } catch (e: Exception) {
            Log.e(TAG, "Error resolving activity: ${e.message}")
            null
        }

        if (resolvedActivity != null) {
            Log.d(TAG, "Resolved activity found!")
            Log.d(TAG, "Handler Package: ${resolvedActivity.activityInfo.packageName}")
            Log.d(TAG, "Handler Class: ${resolvedActivity.activityInfo.name}")
            startActivity(intent)
        } else {
            Toast.makeText(requireContext(), getString(R.string.error_no_app_to_handle_link), Toast.LENGTH_SHORT).show()
            Log.e(TAG, "FATAL: No app resolved by resolveActivity for URL: $url")

            // Log semua aktivitas yang *dapat* menangani intent, tanpa filter default
            val availableHandlers = packageManager.queryIntentActivities(intent, 0) // Perubahan di sini: dari MATCH_DEFAULT_ONLY menjadi 0
            if (availableHandlers.isEmpty()) {
                Log.e(TAG, "CRITICAL: queryIntentActivities also found NO activities that can handle this intent.")
            } else {
                Log.w(TAG, "queryIntentActivities found ${availableHandlers.size} potential handlers, but none resolved as default:")
                availableHandlers.forEachIndexed { index, info ->
                    Log.w(TAG, "  [$index] Handler: ${info.activityInfo.packageName}/${info.activityInfo.name} (Priority: ${info.priority}, Preferred: ${info.isDefault})" )
                    info.filter?.let { filter ->
                        Log.w(TAG, "    Filter schemes: ${filter.schemesIterator().asSequence().joinToString()}")
                        Log.w(TAG, "    Filter categories: ${filter.categoriesIterator().asSequence().joinToString()}")
                    }
                }
            }

            // Test dengan URL Google yang sangat umum sebagai fallback
            val testGoogleIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))
            testGoogleIntent.addCategory(Intent.CATEGORY_BROWSABLE)
            val resolvedGoogleActivity = packageManager.resolveActivity(testGoogleIntent, 0) // Perubahan di sini: dari MATCH_DEFAULT_ONLY menjadi 0
            if (resolvedGoogleActivity != null) {
                Log.w(TAG, "TEST: https://www.google.com CAN be resolved by: ${resolvedGoogleActivity.activityInfo.packageName}")
            } else {
                Log.e(TAG, "CRITICAL TEST: Even https://www.google.com CANNOT be resolved. This indicates a very deep problem with the device's browser setup or manifest processing.")
            }
        }
        Log.d(TAG, "--- End URL attempt ---")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}