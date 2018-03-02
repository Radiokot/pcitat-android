package ua.com.radiokot.pc.logic.repository

import ua.com.radiokot.pc.App

/**
 * Manages repositories instances.
 */
object Repositories {
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