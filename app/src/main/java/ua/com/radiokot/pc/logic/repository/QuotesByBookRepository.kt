package ua.com.radiokot.pc.logic.repository

import io.reactivex.Completable
import io.reactivex.Observable
import ua.com.radiokot.pc.App
import ua.com.radiokot.pc.logic.api.ApiFactory
import ua.com.radiokot.pc.logic.event_bus.PcEvents
import ua.com.radiokot.pc.logic.event_bus.events.BookQuotesUpdatedEvent
import ua.com.radiokot.pc.logic.event_bus.events.QuoteAddedEvent
import ua.com.radiokot.pc.logic.event_bus.events.QuoteDeletedEvent
import ua.com.radiokot.pc.logic.event_bus.events.QuoteUpdatedEvent
import ua.com.radiokot.pc.logic.model.Quote
import ua.com.radiokot.pc.logic.repository.base.RepositoryCache
import ua.com.radiokot.pc.logic.repository.base.SimpleMultipleItemsRepository

/**
 * Holds quotes from a certain book.
 */
class QuotesByBookRepository(private val bookId: Long) : SimpleMultipleItemsRepository<Quote>() {
    private val quotesCache = App.state.quotesCache
    override val itemsCache: RepositoryCache<Quote>
        get() = quotesCache

    override fun getItems(): Observable<List<Quote>> {
        return ApiFactory.getQuotesService().getByBookId(bookId)
            .map { it.data.items }
    }

    fun add(text: String, isPublic: Boolean): Observable<Quote> {
        val newQuote = Quote(
            bookId = bookId,
            text = text,
            isPublic = isPublic,
        )
        return ApiFactory.getQuotesService()
            .add(bookId, newQuote)
            .map { it.data }
            .doOnNext {
                quotesCache.add(it)
                broadcast()
                PcEvents.publish(QuoteAddedEvent(it))
            }
    }

    fun update(id: Long, text: String, isPublic: Boolean): Observable<Quote> {
        return ApiFactory.getQuotesService()
            .update(id, Quote(text = text, isPublic = isPublic))
            .map { it.data }
            .doOnNext {
                quotesCache.items
                    .find { it.id == id }
                    ?.let {
                        it.text = text
                        it.isPublic = isPublic
                        quotesCache.update(it)
                        broadcast()
                        PcEvents.publish(QuoteUpdatedEvent(it))
                    }
            }
    }

    fun delete(id: Long): Completable {
        return ApiFactory.getQuotesService()
            .delete(id)
            .doOnComplete {
                quotesCache.deleteById(id)
                broadcast()
                PcEvents.publish(QuoteDeletedEvent(id, bookId))
            }
    }

    override fun onNewItems(newItems: List<Quote>) {
        isNeverUpdated = false
        isFresh = true

        quotesCache.mergeForBook(bookId, newItems)

        broadcast()

        PcEvents.publish(BookQuotesUpdatedEvent(bookId, newItems))
    }

    override fun broadcast() {
        itemsSubject.onNext(quotesCache.items.filter { it.bookId == bookId })
    }
}
