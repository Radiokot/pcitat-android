package ua.com.radiokot.pc.logic

import ua.com.radiokot.pc.logic.repository.BooksRepository
import ua.com.radiokot.pc.logic.repository.QuotesByBookRepository
import ua.com.radiokot.pc.logic.repository.QuotesCache
import ua.com.radiokot.pc.logic.repository.QuotesRepository
import ua.com.radiokot.pc.logic.repository.UserRepository
import ua.com.radiokot.pc.util.LruCache

/**
 * Holds state of the application.
 */
class AppState(
    var userRepository: UserRepository? = null,
    var booksRepository: BooksRepository? = null,
    var quotesRepository: QuotesRepository? = null,
    val quotesByBookRepositoriesByBook: LruCache<Long, QuotesByBookRepository> =
        LruCache(10) { QuotesByBookRepository(it) },
    val quotesCache: QuotesCache = QuotesCache()
)
