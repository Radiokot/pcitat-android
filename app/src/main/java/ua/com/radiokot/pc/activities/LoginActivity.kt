package ua.com.radiokot.pc.activities

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.Editable
import com.trello.rxlifecycle2.android.ActivityEvent
import com.trello.rxlifecycle2.kotlin.bindUntilEvent
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.layout_progress.*
import org.jetbrains.anko.enabled
import org.jetbrains.anko.onClick
import ua.com.radiokot.pc.R
import ua.com.radiokot.pc.logic.AuthManager
import ua.com.radiokot.pc.logic.exceptions.NotFoundException
import ua.com.radiokot.pc.logic.model.LoginData
import ua.com.radiokot.pc.logic.repository.Repositories
import ua.com.radiokot.pc.util.Navigator
import ua.com.radiokot.pc.util.ObservableTransformers
import ua.com.radiokot.pc.util.SoftInputUtil
import ua.com.radiokot.pc.util.error_handlers.ErrorHandlerFactory
import ua.com.radiokot.pc.util.text_validators.hasError
import ua.com.radiokot.pc.util.text_validators.setErrorAndFocus
import ua.com.radiokot.pc.view.util.edittext.EditTextUtil
import ua.com.radiokot.pc.view.util.edittext.SimpleTextWatcher


class LoginActivity : BaseActivity() {
    private var isLoading: Boolean = false
        set(value) {
            field = value
            if (value) {
                progress.show()
            } else {
                progress.hide()
            }
            updateLoginAvailability()
        }

    private var canLogIn: Boolean = false
        set(value) {
            field = value
            login_button.enabled = value
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        window.setBackgroundDrawable(
                ColorDrawable(ContextCompat.getColor(this, R.color.md_white_1000)))

        initFields()
        initButtons()

        canLogIn = false
    }

    // region Init
    private fun initFields() {
        object : SimpleTextWatcher() {
            override fun afterTextChanged(p0: Editable?) {
                password_edit_text.error = null
                updateLoginAvailability()
            }
        }.also {
            email_edit_text.addTextChangedListener(it)
            password_edit_text.addTextChangedListener(it)
        }

        EditTextUtil.onEditorAction(password_edit_text) {
            tryToLogIn()
        }
    }

    private fun initButtons() {
        login_button.onClick {
            tryToLogIn()
        }
    }
    // endregion

    private fun updateLoginAvailability() {
        canLogIn = !isLoading
                && email_edit_text.text.isNotBlank()
                && password_edit_text.text.isNotEmpty()
                && !password_edit_text.hasError()
                && !email_edit_text.hasError()
    }

    private fun tryToLogIn() {
        if (email_edit_text.text.isBlank()) {
            email_edit_text.setErrorAndFocus(R.string.error_cannot_be_empty)
        } else if (password_edit_text.text.isEmpty()) {
            password_edit_text.setErrorAndFocus(R.string.error_cannot_be_empty)
        } else if (canLogIn) {
            SoftInputUtil.hideSoftInput(this)
            logIn()
        }
    }

    private fun logIn() {
        val loginData =
                LoginData(email_edit_text.text.toString(), password_edit_text.text.toString())

        AuthManager.logIn(loginData)
                .doOnNext { Repositories.user().set(it) }
                .compose(ObservableTransformers.defaultSchedulers())
                .bindUntilEvent(lifecycle(), ActivityEvent.DESTROY)
                .doOnSubscribe {
                    isLoading = true
                }
                .doOnTerminate {
                    isLoading = false
                }
                .subscribeBy(
                        onNext = {
                            onSuccessLogin()
                        },
                        onError = {
                            when (it) {
                                is NotFoundException ->
                                    EditTextUtil.displayErrorWithFocus(password_edit_text,
                                            R.string.error_invalid_pass)
                                else -> ErrorHandlerFactory.getDefault().handle(it)
                            }

                            updateLoginAvailability()
                        }
                )
    }

    private fun onSuccessLogin() {
        Navigator.toMainActivity(this)
        finish()
    }
}
