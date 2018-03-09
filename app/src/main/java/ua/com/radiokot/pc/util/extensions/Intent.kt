package ua.com.radiokot.pc.util.extensions

import android.content.Intent

/**
 * Allows to specify default value for string extra
 * @return extra value or [default] if there is no such extra
 */
fun Intent.getStringExtra(key: String, default: String): String {
    return extras?.getString(key, default) ?: default
}