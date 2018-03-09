package ua.com.radiokot.pc.logic

import android.support.v4.util.LruCache
import ua.com.radiokot.pc.logic.repository.BooksRepository
import ua.com.radiokot.pc.logic.repository.QuotesByBookRepository
import ua.com.radiokot.pc.logic.repository.QuotesRepository
import ua.com.radiokot.pc.logic.repository.UserRepository

/**
 * Holds state of the application.
 */
class AppState(
        var userRepository: UserRepository? = null,
        var booksRepository: BooksRepository? = null,
        var quotesRepository: QuotesRepository? = null,
        val quotesByBookRepositoriesByBook: LruCache<Long, QuotesRepository> =
        ua.com.radiokot.pc.util.LruCache(10) { QuotesByBookRepository(it) }
)