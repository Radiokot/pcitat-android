package ua.com.radiokot.pc.util.text_validators

import android.support.annotation.StringRes
import android.widget.EditText
import ua.com.radiokot.pc.util.SoftInputUtil

fun EditText.hasError(): Boolean {
    return error != null
}

fun EditText.setErrorAndFocus(@StringRes errorId: Int) {
    setErrorAndFocus(context.getString(errorId))
}

fun EditText.setErrorAndFocus(error: String) {
    this.error = error
    setSelection(text.length)
    requestFocus()
    SoftInputUtil.showSoftInputOnView(this)
}