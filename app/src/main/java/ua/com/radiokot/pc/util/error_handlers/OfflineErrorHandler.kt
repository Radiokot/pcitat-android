package ua.com.radiokot.pc.util.error_handlers

import java.io.IOException

/**
 * Default error handler for offline mode.
 * It simply ignores [IOException].
 */
class OfflineErrorHandler : DefaultErrorHandler() {
    override fun handle(error: Throwable): Boolean {
        if (error is IOException) {
            return true
        } else {
            return super.handle(error)
        }
    }
}