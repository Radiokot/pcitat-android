package ua.com.radiokot.pc.view.dialog

import android.content.Context
import android.support.v7.app.AlertDialog
import ua.com.radiokot.pc.R

class ConfirmationDialog(private val context: Context) {
    fun show(text: String, callback: () -> Unit) {
        val dialog = AlertDialog.Builder(context)
                .setMessage(text)
                .setPositiveButton(R.string.yes, { _, _ -> callback() })
                .setNegativeButton(R.string.cancel, { _, _ -> })
                .show()
    }
}