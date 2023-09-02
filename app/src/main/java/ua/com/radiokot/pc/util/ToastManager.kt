package ua.com.radiokot.pc.util

import android.widget.Toast
import androidx.annotation.StringRes
import ua.com.radiokot.pc.App

object ToastManager {
    fun short(text: String?) {
        text?.let {
            Toast.makeText(App.instance, it, Toast.LENGTH_SHORT).show()
        }
    }

    fun short(@StringRes text: Int) {
        Toast.makeText(App.instance, text, Toast.LENGTH_SHORT).show()
    }

    fun long(text: String?) {
        text?.let {
            Toast.makeText(App.instance, it, Toast.LENGTH_LONG).show()
        }
    }

    fun long(@StringRes text: Int) {
        Toast.makeText(App.instance, text, Toast.LENGTH_LONG).show()
    }
}
