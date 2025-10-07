package com.pictaboo.app

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.ViewGroup.LayoutParams
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton

class LogoutDialogFragment : DialogFragment() {

    interface LogoutDialogListener {
        fun onLogoutConfirmed()
    }

    private var listener: LogoutDialogListener? = null

    fun setLogoutDialogListener(listener: LogoutDialogListener) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_logout_confirmation, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnCancel: MaterialButton = view.findViewById(R.id.btn_cancel_logout)
        val btnYesLogout: MaterialButton = view.findViewById(R.id.btn_yes_logout)

        btnCancel.setOnClickListener { dismiss() }

        btnYesLogout.setOnClickListener {
            listener?.onLogoutConfirmed()
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        val window = dialog?.window
        if (window != null) {
            val displayMetrics = DisplayMetrics()
            requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
            val screenWidth = displayMetrics.widthPixels
            val desiredWidth = (screenWidth * 0.85).toInt()
            window.setLayout(desiredWidth, LayoutParams.WRAP_CONTENT)
        }
    }
}