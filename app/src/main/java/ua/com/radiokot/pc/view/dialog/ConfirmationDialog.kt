package ua.com.radiokot.pc.view.dialog

import android.content.Context
import androidx.appcompat.app.AlertDialog
import ua.com.radiokot.pc.R

class ConfirmationDialog(private val context: Context) {
    fun show(text: String, callback: () -> Unit) {
        AlertDialog.Builder(context)
            .setMessage(text)
            .setPositiveButton(R.string.yes) { _, _ -> callback() }
            .setNegativeButton(R.string.cancel) { _, _ -> }
            .show()
    }
}
