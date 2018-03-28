package ua.com.radiokot.pc.logic

import android.webkit.CookieManager
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import okhttp3.Cookie
import okhttp3.HttpUrl
import okhttp3.internal.http.HttpDate
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import ua.com.radiokot.pc.App
import ua.com.radiokot.pc.logic.api.ApiFactory
import ua.com.radiokot.pc.logic.db.DbFactory
import ua.com.radiokot.pc.logic.model.LoginData
import ua.com.radiokot.pc.logic.model.User
import ua.com.radiokot.pc.logic.repository.Repositories

/**
 * Created by Oleg Koretsky on 2/21/18.
 */
object AuthManager {
    private const val AUTH_KEY_COOKIE_NAME = "HTTP_X_AUTH_KEY"
    private const val AUTH_EMAIL_COOKIE_NAME = "HTTP_X_AUTH_EMAIL"

    private val authorizedSubject = BehaviorSubject.createDefault(isAuthorized())

    val authorizedObservable: Observable<Boolean>
        get() = authorizedSubject

    fun isAuthorized(): Boolean {
        return HttpUrl.parse(ApiFactory.API_URL)?.let {
            ApiFactory.getBaseCookieJar().loadForRequest(it).find {
                it.name() == AUTH_KEY_COOKIE_NAME
            } != null
        } ?: false
    }

    private fun updateSubject() {
        authorizedSubject.onNext(isAuthorized())
    }

    fun logIn(loginData: LoginData): Observable<User> {
        return ApiFactory.getUserService().login(loginData)
                .map { it.data }
                .doOnNext {
                    Repositories.user().set(it)
                    updateSubject()
                }
    }

    fun logInWithKey(email: String, key: String): Boolean {
        val cookieUrl = HttpUrl.parse(ApiFactory.API_URL) ?: return false
        ApiFactory.getBaseCookieJar()
                .saveFromResponse(cookieUrl,
                        listOf(
                                buildCredentialCookie(cookieUrl, AUTH_EMAIL_COOKIE_NAME, email),
                                buildCredentialCookie(cookieUrl, AUTH_KEY_COOKIE_NAME, key)
                        )
                )

        Repositories.user().update()
        updateSubject()

        return true
    }

    private fun buildCredentialCookie(url: HttpUrl, name: String, value: String): Cookie {
        return Cookie.Builder()
                .path(url.encodedPath())
                .domain(url.host())
                .name(name)
                .expiresAt(HttpDate.MAX_DATE)
                .value(value)
                .build()
    }

    fun logOut() {
        doAsync {
            ApiFactory.getBaseCookieJar().clear()
            App.clearState()
            App.instance.clearImageCahce()

            CookieManager.getInstance().removeAllCookie()

            DbFactory.getAppDatabase().clearAllTables()

            uiThread {
                updateSubject()
            }
        }
    }
}