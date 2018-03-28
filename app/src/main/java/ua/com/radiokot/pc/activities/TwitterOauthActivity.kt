package ua.com.radiokot.pc.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.android.synthetic.main.activity_twitter_oauth.*
import kotlinx.android.synthetic.main.layout_progress.*
import ua.com.radiokot.pc.R
import ua.com.radiokot.pc.logic.api.ApiFactory
import ua.com.radiokot.pc.util.extensions.getStringExtra

class TwitterOauthActivity : BaseActivity() {
    enum class Mode {
        LOGIN,
        UPDATE_EXISTING
    }

    companion object {
        const val MODE_EXTRA = "mode"
        const val EMAIL_RESULT_EXTRA = "email"
        const val KEY_RESULT_EXTRA = "key"

        val OAUTH_REQUEST = "oauth".hashCode() and 0xffff

        private const val OAUTH_SUCCESS_KEY = "success"
        private const val OAUTH_EMAIL_KEY = "email"
        private const val OAUTH_KEY_KEY = "key"
    }

    private val mode: Mode
        get() = Mode.valueOf(intent.getStringExtra(MODE_EXTRA, Mode.LOGIN.toString()))
    private val modeUrl: String
        get() = "${ApiFactory.TWITTER_OAUTH_URL
        }?action=${mode.toString().toLowerCase()}&redirect=${ApiFactory.OAUTH_RESULT_URI}"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_twitter_oauth)

        initWebView()
    }

    override fun getToolbar(): Toolbar? = null

    private val oauthWebClient = object : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)

            if (url?.startsWith(ApiFactory.OAUTH_RESULT_URI) == true) {
                view?.visibility = View.INVISIBLE
                processOauthResult(Uri.parse(url))
            } else {
                progress.show()
            }
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            progress.hide()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        oauth_web_view.apply {
            scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
            webViewClient = oauthWebClient
            settings.domStorageEnabled = true
            settings.javaScriptEnabled = true

            loadUrl(modeUrl)
        }
    }

    private fun processOauthResult(resultUri: Uri) {
        val success = resultUri.getQueryParameter(OAUTH_SUCCESS_KEY)?.toBoolean() == true
        if (success) {
            if (mode == Mode.LOGIN) {
                val email: String? = resultUri.getQueryParameter(OAUTH_EMAIL_KEY)
                val key: String? = resultUri.getQueryParameter(OAUTH_KEY_KEY)

                if (email != null && key != null) {
                    finishWithCredentials(email, key)
                } else {
                    finish()
                }
            } else {
                finishWithSuccess()
            }
        } else {
            finish()
        }
    }

    private fun finishWithCredentials(email: String, key: String) {
        setResult(Activity.RESULT_OK,
                Intent().putExtra(EMAIL_RESULT_EXTRA, email)
                        .putExtra(KEY_RESULT_EXTRA, key))
        finish()
    }

    private fun finishWithSuccess() {
        setResult(Activity.RESULT_OK)
        finish()
    }
}
