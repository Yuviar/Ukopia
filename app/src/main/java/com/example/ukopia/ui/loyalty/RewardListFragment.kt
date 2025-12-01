package com.example.ukopia.ui.loyalty

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ukopia.R
import com.example.ukopia.SessionManager
import com.example.ukopia.data.RewardHistoryItem
import com.example.ukopia.databinding.FragmentRewardListBinding
import com.google.android.material.button.MaterialButton

class RewardListFragment : Fragment(R.layout.fragment_reward_list) {

    private var _binding: FragmentRewardListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoyaltyViewModel by activityViewModels()

    private val adapter = RewardHistoryAdapter { item ->
        showCodePopup(item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRewardListBinding.bind(view)

        binding.recyclerViewRewards.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewRewards.adapter = adapter

        binding.btnBackRewards.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val context = requireContext()
        val uid = SessionManager.getUid(context)

        if (uid > 0) {
            viewModel.fetchRewardHistory(uid)
        } else {
            binding.emptyStateText.text = "Silakan login terlebih dahulu."
            binding.emptyStateText.visibility = View.VISIBLE
        }

        viewModel.rewardHistory.observe(viewLifecycleOwner) { historyList ->
            if (!historyList.isNullOrEmpty()) {
                adapter.submitList(historyList)
                binding.emptyStateText.visibility = View.GONE
            } else {
                adapter.submitList(emptyList())
                binding.emptyStateText.visibility = View.VISIBLE
                binding.emptyStateText.text = "Belum ada reward yang diklaim."
            }
        }
    }

    // --- FUNGSI POPUP BARU (CUSTOM LAYOUT) ---
    private fun showCodePopup(item: RewardHistoryItem) {
        // 1. Inflate Layout Custom yang baru dibuat
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_reward_code, null)

        // 2. Inisialisasi View dalam Dialog
        val tvTitle = dialogView.findViewById<TextView>(R.id.tv_dialog_title)
        val tvCode = dialogView.findViewById<TextView>(R.id.tv_reward_code)
        val btnClose = dialogView.findViewById<MaterialButton>(R.id.btn_dialog_close)
        val btnCopy = dialogView.findViewById<MaterialButton>(R.id.btn_dialog_copy)

        // 3. Set Data
        tvTitle.text = item.namaReward
        tvCode.text = item.kodeUnik ?: "-"

        // 4. Buat Dialog
        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)

        val dialog = dialogBuilder.create()

        // 5. PENTING: Set Background Transparan agar rounded corner dari XML terlihat
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // 6. Listener Tombol
        btnCopy.setOnClickListener {
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Kode Reward", item.kodeUnik)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(requireContext(), "Kode disalin!", Toast.LENGTH_SHORT).show()
        }

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}