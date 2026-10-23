package com.example.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object EmergencyDialer {
    const val COMPANY_PHONE_NUMBER = "+92 333 6919922"
    const val CLEAN_TEL_URI = "tel:+923336919922"

    fun dialHelpline(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse(CLEAN_TEL_URI)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Unable to open dialer. Please call $COMPANY_PHONE_NUMBER",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
