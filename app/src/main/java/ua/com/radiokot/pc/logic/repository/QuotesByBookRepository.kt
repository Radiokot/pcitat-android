package ua.com.radiokot.pc.logic.repository

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.doAsync
import ua.com.radiokot.pc.logic.api.ApiFactory
import ua.com.radiokot.pc.logic.db.DbFactory
import ua.com.radiokot.pc.logic.db.entities.QuoteEntity
import ua.com.radiokot.pc.logic.model.Quote
import ua.com.radiokot.pc.util.extensions.doNotEmitEmptyList

/**
 * Holds quotes from a certain book.
 */
class QuotesByBookRepository(private val bookId: Long) : QuotesRepository() {
    override fun getItems(): Observable<List<Quote>> {
        return ApiFactory.getQuotesService().getByBookId(bookId)
                .map { it.data.items }
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
}