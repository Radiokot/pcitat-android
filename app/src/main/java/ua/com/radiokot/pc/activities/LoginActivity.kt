package ua.com.radiokot.pc.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import com.trello.rxlifecycle2.android.ActivityEvent
import com.trello.rxlifecycle2.kotlin.bindUntilEvent
import io.reactivex.rxkotlin.subscribeBy
import ua.com.radiokot.pc.R
import ua.com.radiokot.pc.databinding.ActivityLoginBinding
import ua.com.radiokot.pc.logic.AuthManager
import ua.com.radiokot.pc.logic.exceptions.NotFoundException
import ua.com.radiokot.pc.logic.model.LoginData
import ua.com.radiokot.pc.util.Navigator
import ua.com.radiokot.pc.util.ObservableTransformers
import ua.com.radiokot.pc.util.SoftInputUtil
import ua.com.radiokot.pc.util.error_handlers.ErrorHandlerFactory
import ua.com.radiokot.pc.util.extensions.getStringExtra
import ua.com.radiokot.pc.util.text_validators.hasError
import ua.com.radiokot.pc.util.text_validators.setErrorAndFocus
import ua.com.radiokot.pc.view.util.edittext.EditTextUtil
import ua.com.radiokot.pc.view.util.edittext.SimpleTextWatcher


class LoginActivity : BaseActivity() {
    private lateinit var view: ActivityLoginBinding

    private var isLoading: Boolean = false
        set(value) {
            field = value
            if (value) {
                view.includeProgress.progress.show()
            } else {
                view.includeProgress.progress.hide()
            }
            updateLoginAvailability()
        }

    private var canLogIn: Boolean = false
        set(value) {
            field = value
            view.loginButton.isEnabled = value
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        view = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(view.root)

        window.setBackgroundDrawable(ColorDrawable(Color.WHITE))

        initFields()
        initButtons()

        canLogIn = false
    }

    // region Init
    private fun initFields() {
        object : SimpleTextWatcher() {
            override fun afterTextChanged(p0: Editable?) {
                view.passwordEditText.error = null
                updateLoginAvailability()
            }
        }.also {
            view.emailEditText.addTextChangedListener(it)
            view.emailEditText.addTextChangedListener(it)
        }

        EditTextUtil.onEditorAction(view.passwordEditText) {
            tryToLogIn()
        }
    }

    private fun initButtons() {
        view.loginButton.setOnClickListener {
            tryToLogIn()
        }

        view.twitterLoginButton.setOnClickListener {
            Navigator.openTwitterOauthActivity(this, TwitterOauthActivity.Mode.LOGIN)
        }
    }
    // endregion

    private fun updateLoginAvailability() {
        canLogIn = !isLoading
                && !view.emailEditText.text.isNullOrBlank()
                && !view.passwordEditText.text.isNullOrEmpty()
                && !view.passwordEditText.hasError()
                && !view.emailEditText.hasError()
    }

    private fun tryToLogIn() {
        if (view.emailEditText.text.isNullOrBlank()) {
            view.emailEditText.setErrorAndFocus(R.string.error_cannot_be_empty)
        } else if (view.passwordEditText.text.isNullOrEmpty()) {
            view.passwordEditText.setErrorAndFocus(R.string.error_cannot_be_empty)
        } else if (canLogIn) {
            SoftInputUtil.hideSoftInput(this)
            logIn()
        }
    }

    private fun logIn() {
        val loginData = LoginData(
            view.emailEditText.text.toString(),
            view.passwordEditText.text.toString()
        )

        AuthManager.logIn(loginData)
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
                            EditTextUtil.displayErrorWithFocus(
                                view.passwordEditText,
                                R.string.error_invalid_pass
                            )

                        else -> ErrorHandlerFactory.getDefault().handle(it)
                    }

                    updateLoginAvailability()
                }
            )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == TwitterOauthActivity.OAUTH_REQUEST
            && resultCode == Activity.RESULT_OK
        ) {
            val email = data?.getStringExtra(TwitterOauthActivity.EMAIL_RESULT_EXTRA, "")
            val key = data?.getStringExtra(TwitterOauthActivity.KEY_RESULT_EXTRA, "")

            if (email?.isEmpty() == false && key?.isEmpty() == false
                && AuthManager.logInWithKey(email, key)
            ) {
                onSuccessLogin()
            }
        }
    }

    private fun onSuccessLogin() {
        Navigator.toMainActivity(this)
        finish()
    }
}
