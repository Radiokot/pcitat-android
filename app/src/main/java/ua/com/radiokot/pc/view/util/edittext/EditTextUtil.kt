package ua.com.radiokot.pc.view.util.edittext

import android.widget.EditText
import androidx.annotation.StringRes
import androidx.core.widget.doAfterTextChanged
import ua.com.radiokot.pc.R
import ua.com.radiokot.pc.util.SoftInputUtil
import ua.com.radiokot.pc.util.text_validators.EmailValidator
import ua.com.radiokot.pc.util.text_validators.PasswordValidator

object EditTextUtil {
    fun initEmailEditText(editText: EditText) {
        editText.doAfterTextChanged { s ->
            if (s.isNullOrEmpty() || EmailValidator.isValid(s)) {
                editText.error = null
            } else {
                editText.error = editText.context.getString(R.string.error_invalid_email)
            }
        }
    }

    fun initPasswordEditText(editText: EditText) {
        editText.doAfterTextChanged { s ->
            if (s.isNullOrEmpty() || PasswordValidator.isValid(s)) {
                editText.error = null
            } else {
                editText.error = editText.context.getString(R.string.error_weak_pass)
            }
        }
    }

    fun onEditorAction(editText: EditText, callback: () -> Unit) {
        editText.setOnEditorActionListener { _, _, _ ->
            callback()
            true
        }
    }

    fun displayErrorWithFocus(field: EditText?, @StringRes errorId: Int) {
        field?.let {
            displayErrorWithFocus(it, it.context.getString(errorId))
        }
    }

    fun displayErrorWithFocus(field: EditText?, error: String) {
        field?.error = error
        field?.setSelection(field.text.length)
        field?.requestFocus()
        SoftInputUtil.showSoftInputOnView(field)
    }
}
