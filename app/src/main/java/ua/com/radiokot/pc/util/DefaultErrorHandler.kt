package ua.com.radiokot.pc.util

import ua.com.radiokot.pc.App
import ua.com.radiokot.pc.R
import ua.com.radiokot.pc.logic.exceptions.NotFoundException
import java.io.IOException
import java.util.concurrent.CancellationException

object DefaultErrorHandler {
    fun handle(exception: Throwable? = null) {
        when (exception) {
            is CancellationException ->
                return
            else -> {
                getErrorMessage(exception)?.let {
                    ToastManager.short(it)
                }
            }
        }
    }

    fun getErrorMessage(exception: Throwable? = null): String? {
        exception?.printStackTrace()
        return when (exception) {
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