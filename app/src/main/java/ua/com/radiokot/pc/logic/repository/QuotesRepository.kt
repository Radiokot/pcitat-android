package ua.com.radiokot.pc.logic.repository

import io.reactivex.Observable
import ua.com.radiokot.pc.App
import ua.com.radiokot.pc.logic.api.ApiFactory
import ua.com.radiokot.pc.logic.event_bus.events.BookDeletedEvent
import ua.com.radiokot.pc.logic.event_bus.events.BookQuotesUpdatedEvent
import ua.com.radiokot.pc.logic.event_bus.events.PcEvent
import ua.com.radiokot.pc.logic.event_bus.events.QuoteAddedEvent
import ua.com.radiokot.pc.logic.event_bus.events.QuoteDeletedEvent
import ua.com.radiokot.pc.logic.event_bus.events.QuoteUpdatedEvent
import ua.com.radiokot.pc.logic.model.Quote
import ua.com.radiokot.pc.logic.repository.base.RepositoryCache
import ua.com.radiokot.pc.logic.repository.base.SimpleMultipleItemsRepository

/**
 * Holds user's quotes.
 */
open class QuotesRepository : SimpleMultipleItemsRepository<Quote>() {
    private val quotesCache = App.state.quotesCache
    override val itemsCache: RepositoryCache<Quote>
        get() = quotesCache

    override fun getItems(): Observable<List<Quote>> {
        return ApiFactory.getQuotesService().get()
            .map { it.data.items }
    }

    override fun handleEvent(event: PcEvent) {
        super.handleEvent(event)
        when (event) {
            is BookDeletedEvent -> {
                quotesCache.mergeForBook(event.bookId, listOf())
                broadcast()
            }

            is BookQuotesUpdatedEvent, is QuoteAddedEvent,
            is QuoteDeletedEvent, is QuoteUpdatedEvent -> {
                broadcast()
            }
        }
    }
}
