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

        binding.tvHeaderTitleRewards.text = getString(R.string.all_rewards_title) // Menggunakan string resource

        val context = requireContext()
        val uid = SessionManager.getUid(context)

        if (uid > 0) {
            viewModel.fetchRewardHistory(uid)
        } else {
            binding.emptyStateText.text = getString(R.string.login_required_message) // Menggunakan string resource
            binding.emptyStateText.visibility = View.VISIBLE
        }

        viewModel.rewardHistory.observe(viewLifecycleOwner) { historyList ->
            if (!historyList.isNullOrEmpty()) {
                val sortedList = historyList.sortedWith(compareByDescending<RewardHistoryItem> {
                    !it.statusKlaim.equals("Claimed", ignoreCase = true) || !it.statusKlaim.equals("Sudah Dipakai", ignoreCase = true) // true for UNCLAIMED (will be on top)
                }.thenByDescending { it.tanggalDapat })

                adapter.submitList(sortedList)
                binding.emptyStateText.visibility = View.GONE
            } else {
                adapter.submitList(emptyList())
                binding.emptyStateText.visibility = View.VISIBLE
                binding.emptyStateText.text = getString(R.string.no_rewards_defined) // Menggunakan string resource
            }
        }
    }

    private fun showCodePopup(item: RewardHistoryItem) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_reward_code, null)

        val tvTitle = dialogView.findViewById<TextView>(R.id.tv_dialog_title)
        val tvInstruction = dialogView.findViewById<TextView>(R.id.tv_dialog_instruction)
        val tvCode = dialogView.findViewById<TextView>(R.id.tv_reward_code)
        val btnClose = dialogView.findViewById<MaterialButton>(R.id.btn_dialog_close)
        val btnCopy = dialogView.findViewById<MaterialButton>(R.id.btn_dialog_copy)

        tvTitle.text = item.namaReward
        tvInstruction.text = getString(R.string.show_code_instruction)
        tvCode.text = item.kodeUnik ?: "-"

        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)

        val dialog = dialogBuilder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        btnCopy.setOnClickListener {
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            // MODIFIKASI: Menggunakan string resource untuk label ClipData
            val clip = ClipData.newPlainText(getString(R.string.clipboard_reward_label), item.kodeUnik)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(requireContext(), getString(R.string.code_copied_toast), Toast.LENGTH_SHORT).show()

            val uid = SessionManager.getUid(requireContext())
            if (uid > 0) {
                viewModel.fetchRewardHistory(uid)
                Toast.makeText(requireContext(), getString(R.string.show_code_to_staff_instruction), Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(requireContext(), getString(R.string.login_required_message), Toast.LENGTH_SHORT).show()
            }

            dialog.dismiss()
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