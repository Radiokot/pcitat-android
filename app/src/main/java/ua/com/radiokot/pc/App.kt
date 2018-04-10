package ua.com.radiokot.pc

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.support.v7.app.AppCompatDelegate
import com.crashlytics.android.Crashlytics
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.security.ProviderInstaller
import com.jakewharton.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import io.fabric.sdk.android.Fabric
import ua.com.radiokot.pc.logic.AppState
import ua.com.radiokot.pc.logic.AuthManager
import ua.com.radiokot.pc.util.Navigator
import ua.com.radiokot.pc.util.ObservableTransformers
import java.io.File

/**
 * Created by Oleg Koretsky on 2/19/18.
 */
class App : Application() {
    companion object {
        private val IMAGE_CACHE_DIR_NAME = "images"

        private lateinit var mInstance: App

        val instance: App
            get() = mInstance

        lateinit var state: AppState
            private set

        fun clearState() {
            state = AppState()
        }
    }

    private var currentActivity: Activity? = null

    val areGooglePlayServicesAvailable: Boolean
        get() {
            val googleApiAvailability = GoogleApiAvailability.getInstance()
            val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this)
            return resultCode == ConnectionResult.SUCCESS
        }

    override fun onCreate() {
        super.onCreate()

        mInstance = this
        clearState()

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        initTlsIfNeeded()
        initCrashlytics()
        initLifecycleCallbacks()
        initPicasso()

        subscribeToAuthStatus()
    }

    // region Init
    private fun initTlsIfNeeded() {
        if (areGooglePlayServicesAvailable) {
            try {
                ProviderInstaller.installIfNeeded(this)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun initCrashlytics() {
        if (BuildConfig.ENABLE_CRASHLYTICS) {
            val fabricConfig = Fabric.Builder(this)
                    .kits(Crashlytics())
                    .debuggable(true)
                    .build()
            Fabric.with(fabricConfig)
        }
    }

    private fun initLifecycleCallbacks() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityResumed(a: Activity) {}

            override fun onActivityPaused(a: Activity) {}

            override fun onActivityCreated(a: Activity, b: Bundle?) {}

            override fun onActivityStarted(a: Activity) {
                currentActivity = a
            }

            override fun onActivityStopped(a: Activity) {}

            override fun onActivitySaveInstanceState(a: Activity, b: Bundle?) {}

            override fun onActivityDestroyed(a: Activity) {}
        })
    }

    private fun subscribeToAuthStatus() {
        AuthManager.authorizedObservable
                .compose(ObservableTransformers.defaultSchedulers())
                .subscribe { isAuthorized ->
                    if (!isAuthorized) {
                        currentActivity?.let {
                            it.finishAffinity()
                            Navigator.toLoginActivity(it)
                        }
                    }
                }
    }

    private fun initPicasso() {
        val picasso = Picasso.Builder(this)
                .downloader(OkHttp3Downloader(getImageCacheDir()))
                .build()
        Picasso.setSingletonInstance(picasso)
    }
    // endregion

    private fun getImageCacheDir(): File {
        return File(externalCacheDir ?: cacheDir, IMAGE_CACHE_DIR_NAME)
    }

    fun clearImageCahce() {
        getImageCacheDir().deleteRecursively()
    }
}