package ua.com.radiokot.pc.logic.repository

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.doAsync
import ua.com.radiokot.pc.logic.api.ApiFactory
import ua.com.radiokot.pc.logic.db.DbFactory
import ua.com.radiokot.pc.logic.db.entities.QuoteEntity
import ua.com.radiokot.pc.logic.event_bus.events.*
import ua.com.radiokot.pc.logic.model.Quote
import ua.com.radiokot.pc.logic.repository.base.SimpleMultipleItemsRepository
import ua.com.radiokot.pc.util.extensions.doNotEmitEmptyList

/**
 * Holds user's quotes.
 */
open class QuotesRepository : SimpleMultipleItemsRepository<Quote>() {
    override fun getItems(): Observable<List<Quote>> {
        return ApiFactory.getQuotesService().get()
                .map { it.data.items }
    }

    override fun getStoredItems(): Observable<List<Quote>> {
        return DbFactory.getAppDatabase().quoteDao.getAll()
                .subscribeOn(Schedulers.io())
                .map { it.map { it.toQuote() } }
                .toObservable()
                .doNotEmitEmptyList()
    }

    override fun storeItems(items: List<Quote>) {
        super.storeItems(items)
        doAsync {
            DbFactory.getAppDatabase().quoteDao.apply {
                insert(*items
                        .map {
                            QuoteEntity.fromQuote(it)
                        }
                        .toTypedArray()
                )

                leaveOnlyIds(*items
                        .map {
                            it.id
                        }
                        .toTypedArray()
                )
            }
        }
    }

    override fun handleEvent(event: PcEvent) {
        super.handleEvent(event)
        when (event) {
            is BookDeletedEvent ->
                deleteFromBookLocally(event.bookId)
            is QuoteAddedEvent -> {
                if (!itemsCache.contains(event.quote)) {
                    itemsCache.add(0, event.quote)
                    broadcast()
                }
            }
            is QuoteDeletedEvent -> {
                if (itemsCache.removeAll { it.id == event.quoteId }) {
                    broadcast()
                }
            }
            is QuoteUpdatedEvent -> {
                val index = itemsCache.indexOf(event.quote)
                if (index >= 0) {
                    itemsCache[index] = event.quote
                    broadcast()
                }
            }
        }
    }

    private fun deleteFromBookLocally(bookId: Long) {
        itemsCache.removeAll {
            it.bookId == bookId
        }
        broadcast()

        deleteFromBookInDb(bookId)
    }

    private fun deleteFromBookInDb(bookId: Long) {
        doAsync {
            DbFactory.getAppDatabase().quoteDao.deleteFromBook(bookId)
        }
    }
}