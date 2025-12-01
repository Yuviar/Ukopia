package com.example.ukopia.ui.auth

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.ukopia.databinding.DialogOtpBinding

class OtpDialogFragment : DialogFragment() {

    private var _binding: DialogOtpBinding? = null
    private val binding get() = _binding!!

    var onVerifyListener: ((String) -> Unit)? = null
    var onResendListener: (() -> Unit)? = null

    private val otpInputs = arrayOfNulls<EditText>(6)
    private var resendTimer: CountDownTimer? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogOtpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupOtpInputs()
        startResendTimer()

        binding.buttonDialogVerify.setOnClickListener {
            val code = getOtpCode()
            if (code.length == 6) {
                onVerifyListener?.invoke(code)
                dismiss()
            } else {
                Toast.makeText(context, "Please enter the 6-digit code", Toast.LENGTH_SHORT).show()
            }
        }

        binding.textViewResendOtp.setOnClickListener {
            if (binding.textViewResendOtp.isEnabled) {
                onResendListener?.invoke()
                startResendTimer()
            }
        }
    }

    private fun setupOtpInputs() {
        otpInputs[0] = binding.otpEditText1
        otpInputs[1] = binding.otpEditText2
        otpInputs[2] = binding.otpEditText3
        otpInputs[3] = binding.otpEditText4
        otpInputs[4] = binding.otpEditText5
        otpInputs[5] = binding.otpEditText6

        for (i in 0 until otpInputs.size) {
            otpInputs[i]?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1 && i < otpInputs.size - 1) {
                        otpInputs[i + 1]?.requestFocus()
                    } else if (s?.length == 0 && i > 0) {
                        otpInputs[i - 1]?.requestFocus()
                    }
                }
            })
        }
    }

    private fun getOtpCode(): String {
        val sb = StringBuilder()
        for (editText in otpInputs) {
            sb.append(editText?.text.toString())
        }
        return sb.toString()
    }

    private fun startResendTimer() {
        binding.textViewResendOtp.isEnabled = false
        binding.textViewResendOtp.setTextColor(Color.GRAY)

        resendTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.textViewResendOtp.text = "Resend OTP in ${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                binding.textViewResendOtp.text = "Resend OTP"
                binding.textViewResendOtp.isEnabled = true
                binding.textViewResendOtp.setTextColor(Color.BLACK)
            }
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        resendTimer?.cancel()
        _binding = null
    }
}