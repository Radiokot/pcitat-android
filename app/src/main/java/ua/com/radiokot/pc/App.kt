package ua.com.radiokot.pc

import android.app.Application
import android.support.v7.app.AppCompatDelegate
import com.crashlytics.android.Crashlytics
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.security.ProviderInstaller
import io.fabric.sdk.android.Fabric

/**
 * Created by Oleg Koretsky on 2/19/18.
 */
class App : Application() {
    val areGooglePlayServicesAvailable: Boolean
        get() {
            val googleApiAvailability = GoogleApiAvailability.getInstance()
            val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this)
            return resultCode == ConnectionResult.SUCCESS
        }

    override fun onCreate() {
        super.onCreate()

        if (areGooglePlayServicesAvailable) {
            try {
                ProviderInstaller.installIfNeeded(this)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        if (BuildConfig.ENABLE_CRASHLYTICS) {
            val fabricConfig = Fabric.Builder(this)
                    .kits(Crashlytics())
                    .debuggable(true)
                    .build()
            Fabric.with(fabricConfig)
        }
    }
}