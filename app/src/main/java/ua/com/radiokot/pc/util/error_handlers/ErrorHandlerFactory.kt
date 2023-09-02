package ua.com.radiokot.pc.util.error_handlers

import ua.com.radiokot.pc.util.NetworkStateUtil

object ErrorHandlerFactory {
    private var defaultErrorHandler: ErrorHandler? = null
    fun getDefault(): ErrorHandler {
        return defaultErrorHandler ?: DefaultErrorHandler()
            .also { defaultErrorHandler = it }
    }

    private var offlineDefaultErrorHandler: ErrorHandler? = null
    fun getOfflineDefault(): ErrorHandler {
        return offlineDefaultErrorHandler ?: OfflineErrorHandler()
            .also { offlineDefaultErrorHandler = it }

    }

    fun getByNetworkState(): ErrorHandler {
        return if (NetworkStateUtil.isNetworkAvailable()) getDefault() else getOfflineDefault()
    }
}
