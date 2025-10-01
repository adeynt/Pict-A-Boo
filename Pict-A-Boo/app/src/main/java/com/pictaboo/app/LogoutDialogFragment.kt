package com.pictaboo.app

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton

// Konstanta PREFS_NAME, KEY_USERNAME, dan KEY_EMAIL diasumsikan ada di ProfileActivity.kt

class LogoutDialogFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Hapus background default dialog
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        // Panggil layout activity_logout.xml
        return inflater.inflate(R.layout.activity_logout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnCancel = view.findViewById<MaterialButton>(R.id.btnCancel)
        val btnYesLogout = view.findViewById<MaterialButton>(R.id.btnYesLogout)

        // Listener untuk tombol Cancel
        btnCancel.setOnClickListener {
            dismiss() // Tutup dialog
        }

        // Listener untuk tombol Yes, Logout
        btnYesLogout.setOnClickListener {
            // 1. Hapus data Shared Preferences
            val sharedPref = requireContext().getSharedPreferences(PREFS_NAME, AppCompatActivity.MODE_PRIVATE)
            with (sharedPref.edit()) {
                remove(KEY_USERNAME)
                remove(KEY_EMAIL)
                apply()
            }

            // 2. Navigasi ke WelcomeActivity
            val intent = Intent(requireContext(), WelcomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

            dismiss() // Tutup dialog
        }
    }
}