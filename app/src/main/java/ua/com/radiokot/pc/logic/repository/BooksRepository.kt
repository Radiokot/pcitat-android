package ua.com.radiokot.pc.logic.repository

import io.reactivex.Completable
import io.reactivex.Observable
import ua.com.radiokot.pc.logic.api.ApiFactory
import ua.com.radiokot.pc.logic.event_bus.PcEvents
import ua.com.radiokot.pc.logic.event_bus.events.BookAddedEvent
import ua.com.radiokot.pc.logic.event_bus.events.BookDeletedEvent
import ua.com.radiokot.pc.logic.event_bus.events.BookQuotesUpdatedEvent
import ua.com.radiokot.pc.logic.event_bus.events.PcEvent
import ua.com.radiokot.pc.logic.event_bus.events.QuoteAddedEvent
import ua.com.radiokot.pc.logic.event_bus.events.QuoteDeletedEvent
import ua.com.radiokot.pc.logic.event_bus.events.TwitterBookChangedEvent
import ua.com.radiokot.pc.logic.model.Book
import ua.com.radiokot.pc.logic.model.ExternalSiteBook
import ua.com.radiokot.pc.logic.model.containers.BookIdContainer
import ua.com.radiokot.pc.logic.repository.base.RepositoryCache
import ua.com.radiokot.pc.logic.repository.base.SimpleMultipleItemsRepository

/**
 * Holds user's books.
 */
class BooksRepository : SimpleMultipleItemsRepository<Book>() {
    private val booksCache = BooksCache()
    override val itemsCache: RepositoryCache<Book>
        get() = booksCache

    override fun getItems(): Observable<List<Book>> {
        return ApiFactory.getBooksService().get()
            .map { it.data.items }
    }

    fun addExternalBook(externalSiteBook: ExternalSiteBook): Observable<Book> {
        return ApiFactory.getBooksService().add(externalSiteBook)
            .map { it.data }
            .doOnNext {
                booksCache.add(it)
                broadcast()
                PcEvents.publish(BookAddedEvent(it))
            }
    }

    fun setTwitterBook(bookId: Long): Completable {
        return ApiFactory.getBooksService().setTwitterBook(BookIdContainer(bookId))
            .doOnComplete {
                val prevTwitterBook = itemsCache.items.find { it.isTwitterBook == true }
                val selectedBook = itemsCache.items.find { it.id == bookId } ?: return@doOnComplete

                prevTwitterBook?.isTwitterBook = false
                selectedBook.isTwitterBook = true

                if (prevTwitterBook != null) {
                    itemsCache.update(prevTwitterBook)
                }
                itemsCache.update(selectedBook)

                broadcast()

                PcEvents.publish(TwitterBookChangedEvent(bookId))
            }
    }

    fun delete(bookId: Long): Completable {
        return ApiFactory.getBooksService().delete(bookId)
            .doOnComplete {
                booksCache.deleteById(bookId)
                broadcast()
                PcEvents.publish(BookDeletedEvent(bookId))
            }
    }

    override fun handleEvent(event: PcEvent) {
        super.handleEvent(event)
        when (event) {
            is BookQuotesUpdatedEvent ->
                updateBookQuotesCount(event.bookId, event.quotes.size)

            is QuoteAddedEvent -> {
                itemsCache.items.find { it.id == event.quote.bookId }
                    ?.let {
                        updateBookQuotesCount(
                            it.id ?: -1,
                            (it.quotesCount ?: 0) + 1
                        )
                        broadcast()
                    }
            }

            is QuoteDeletedEvent -> {
                itemsCache.items.find { it.id == event.bookId }
                    ?.let {
                        updateBookQuotesCount(
                            it.id ?: -1,
                            (it.quotesCount ?: 1) - 1
                        )
                    }
            }
        }
    }

    private fun updateBookQuotesCount(bookId: Long, quotesCount: Int) {
        val updatingBook = itemsCache.items.find { it.id == bookId }

        if (updatingBook != null && updatingBook.quotesCount != quotesCount) {
            updatingBook.quotesCount = quotesCount
            itemsCache.update(updatingBook)
            broadcast()
        }
    }
}
