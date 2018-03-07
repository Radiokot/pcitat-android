package ua.com.radiokot.pc.logic

import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import okhttp3.HttpUrl
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import ua.com.radiokot.pc.App
import ua.com.radiokot.pc.logic.api.ApiFactory
import ua.com.radiokot.pc.logic.db.DbFactory
import ua.com.radiokot.pc.logic.model.LoginData
import ua.com.radiokot.pc.logic.model.User

/**
 * Created by Oleg Koretsky on 2/21/18.
 */
object AuthManager {
    private val AUTH_KEY_COOKIE_NAME = "HTTP_X_AUTH_KEY"

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
                    updateSubject()
                }
    }

    fun logOut() {
        doAsync {
            ApiFactory.getBaseCookieJar().clear()
            App.clearState()
            App.instance.clearImageCahce()

            DbFactory.getAppDatabase().apply {
                bookDao.clear()
                userDao.clear()
            }

            uiThread {
                updateSubject()
            }
        }
    }
}