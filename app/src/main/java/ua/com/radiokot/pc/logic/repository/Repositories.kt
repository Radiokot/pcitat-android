package ua.com.radiokot.pc.logic.repository

import ua.com.radiokot.pc.App
import ua.com.radiokot.pc.logic.AuthManager

/**
 * Manages repositories instances.
 */
object Repositories {
    init {
        if (AuthManager.isAuthorized()) {
            user().updateIfNotFresh()
        }
    }

    fun user(): UserRepository {
        return App.state.userRepository.let {
            if (it != null) {
                it
            } else {
                val new = UserRepository()
                App.state.userRepository = new
                new
            }
        }
    }

    fun books(): BooksRepository {
        return App.state.booksRepository.let {
            if (it != null) {
                it
            } else {
                val new = BooksRepository()
                App.state.booksRepository = new
                new
            }
        }
    }
}