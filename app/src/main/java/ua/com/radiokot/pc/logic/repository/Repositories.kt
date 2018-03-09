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
        return App.state.userRepository
                ?: UserRepository().also { App.state.userRepository = it }
    }

    fun books(): BooksRepository {
        return App.state.booksRepository
                ?: BooksRepository().also { App.state.booksRepository = it }
    }

    fun quotes(): QuotesRepository {
        return App.state.quotesRepository
                ?: QuotesRepository().also { App.state.quotesRepository = it }
    }

    fun quotes(bookId: Long): QuotesRepository {
        return App.state.quotesByBookRepositoriesByBook.get(bookId)
    }
}