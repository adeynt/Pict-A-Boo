package com.pictaboo.app

import android.app.ActionBar
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.android.material.button.MaterialButton

class LogoutDialogFragment : DialogFragment() {

    // Interface untuk berkomunikasi kembali dengan Activity
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
        // Menggunakan layout kustom yang baru saja kita buat
        return inflater.inflate(R.layout.dialog_logout_confirmation, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        // Hapus title bar default agar layout kustom kita yang terlihat penuh
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Pastikan latar belakang dialog transparan untuk CardView kita
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnCancel: MaterialButton = view.findViewById(R.id.btn_cancel_logout)
        val btnYesLogout: MaterialButton = view.findViewById(R.id.btn_yes_logout)

        btnCancel.setOnClickListener {
            dismiss() // Tutup dialog
        }

        btnYesLogout.setOnClickListener {
            listener?.onLogoutConfirmed() // Beri tahu Activity bahwa logout dikonfirmasi
            dismiss() // Tutup dialog
        }
    }

    // ... di dalam class LogoutDialogFragment

    override fun onStart() {
        super.onStart()
        val window: Window? = dialog?.window
        if (window != null) {
            val displayMetrics = DisplayMetrics()
            requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
            val screenWidth = displayMetrics.widthPixels
            val desiredWidth = (screenWidth * 0.85).toInt()
            window.setLayout(
                desiredWidth, ActionBar.LayoutParams.WRAP_CONTENT
            )
        }
    }

// ...
}