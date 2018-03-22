package ua.com.radiokot.pc.logic.repository

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.doAsync
import ua.com.radiokot.pc.logic.api.ApiFactory
import ua.com.radiokot.pc.logic.db.DbFactory
import ua.com.radiokot.pc.logic.db.entities.QuoteEntity
import ua.com.radiokot.pc.logic.event_bus.PcEvents
import ua.com.radiokot.pc.logic.event_bus.events.BookQuotesUpdatedEvent
import ua.com.radiokot.pc.logic.event_bus.events.QuoteAddedEvent
import ua.com.radiokot.pc.logic.event_bus.events.QuoteDeletedEvent
import ua.com.radiokot.pc.logic.event_bus.events.QuoteUpdatedEvent
import ua.com.radiokot.pc.logic.model.Quote
import ua.com.radiokot.pc.logic.repository.base.SimpleMultipleItemsRepository
import ua.com.radiokot.pc.util.extensions.doNotEmitEmptyList

/**
 * Holds quotes from a certain book.
 */
class QuotesByBookRepository(private val bookId: Long) : SimpleMultipleItemsRepository<Quote>() {
    override fun getItems(): Observable<List<Quote>> {
        return ApiFactory.getQuotesService().getByBookId(bookId)
                .map { it.data.items }
                .doOnNext {
                    PcEvents.publish(BookQuotesUpdatedEvent(bookId, it))
                }
    }

    override fun getStoredItems(): Observable<List<Quote>> {
        return DbFactory.getAppDatabase().quoteDao.getByBookId(bookId)
                .subscribeOn(Schedulers.io())
                .map { it.map { it.toQuote() } }
                .toObservable()
                .doNotEmitEmptyList()
    }

    override fun storeItems(items: List<Quote>) {
        doAsync {
            DbFactory.getAppDatabase().quoteDao.apply {
                insert(*items
                        .map {
                            QuoteEntity.fromQuote(it)
                        }
                        .toTypedArray()
                )

                leaveOnlyIdsForBook(bookId, items
                        .map {
                            it.id
                        }
                )
            }
        }
    }

    fun add(text: String): Observable<Quote> {
        val newQuote = Quote(
                bookId = bookId,
                text = text
        )
        return ApiFactory.getQuotesService()
                .add(bookId, newQuote)
                .map { it.data }
                .doOnNext {
                    if (!itemsCache.contains(it)) {
                        itemsCache.add(0, it)
                        storeSingleQuote(it)
                        broadcast()
                    }

                    PcEvents.publish(QuoteAddedEvent(it))
                }
    }

    fun update(id: Long, text: String): Observable<Quote> {
        return ApiFactory.getQuotesService()
                .update(id, Quote(text = text))
                .map { it.data }
                .doOnComplete {
                    itemsCache
                            .find { it.id == id }
                            ?.let {
                                it.text = text
                                broadcast()
                                updateQuotes(it)

                                PcEvents.publish(QuoteUpdatedEvent(it))
                            }
                }
    }

    fun delete(id: Long): Completable {
        return ApiFactory.getQuotesService()
                .delete(id)
                .doOnComplete {
                    val deletingQuote = itemsCache.find { it.id == id }
                    itemsCache.remove(deletingQuote)
                    broadcast()
                    deleteQuotes(deletingQuote)

                    PcEvents.publish(QuoteDeletedEvent(id, bookId))
                }
    }

    private fun storeSingleQuote(quote: Quote) {
        doAsync {
            DbFactory.getAppDatabase().quoteDao.insert(QuoteEntity.fromQuote(quote))
        }
    }

    private fun updateQuotes(vararg quotes: Quote?) {
        doAsync {
            DbFactory.getAppDatabase().quoteDao.update(
                    *quotes
                            .filterNotNull()
                            .map {
                                QuoteEntity.fromQuote(it)
                            }
                            .toTypedArray()
            )
        }
    }

    private fun deleteQuotes(vararg quotes: Quote?) {
        doAsync {
            DbFactory.getAppDatabase().quoteDao.delete(
                    *quotes
                            .filterNotNull()
                            .map {
                                QuoteEntity.fromQuote(it)
                            }
                            .toTypedArray()
            )
        }
    }
}