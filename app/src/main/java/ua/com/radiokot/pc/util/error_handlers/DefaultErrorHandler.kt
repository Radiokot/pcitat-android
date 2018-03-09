package ua.com.radiokot.pc.util.error_handlers

import ua.com.radiokot.pc.App
import ua.com.radiokot.pc.R
import ua.com.radiokot.pc.logic.exceptions.NotFoundException
import ua.com.radiokot.pc.util.ToastManager
import java.io.IOException
import java.util.concurrent.CancellationException

open class DefaultErrorHandler : ErrorHandler {
    /**
     * Handles given [Throwable]
     * @return [true] if [error] was handled, [false] otherwise
     */
    override fun handle(error: Throwable): Boolean {
        when (error) {
            is CancellationException ->
                return true
            else -> {
                return getErrorMessage(error)?.let {
                    ToastManager.short(it)
                    true
                } ?: false
            }
        }
    }

    /**
     * @return Localized error message for given [Throwable]
     */
    override fun getErrorMessage(error: Throwable): String? {
        return when (error) {
            is CancellationException ->
                null
            is NotFoundException ->
                App.instance.getString(R.string.error_not_found_try_again)
            is IOException ->
                App.instance.getString(R.string.error_io_try_again)
            else -> {
                App.instance.getString(R.string.error_occured_try_again)
            }
        }
    }
}