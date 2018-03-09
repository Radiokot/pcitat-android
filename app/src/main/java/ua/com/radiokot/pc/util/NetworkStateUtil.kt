package ua.com.radiokot.pc.util

import android.content.Context
import android.net.ConnectivityManager
import ua.com.radiokot.pc.App

object NetworkStateUtil {
    fun isNetworkAvailable(): Boolean {
        val connectivityManager =
                App.Companion.instance.getSystemService(Context.CONNECTIVITY_SERVICE)
                        as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }
}