package ua.com.radiokot.pc.logic.repository

import io.reactivex.Single
import org.jetbrains.anko.doAsync
import ua.com.radiokot.pc.logic.db.DbFactory
import ua.com.radiokot.pc.logic.db.entities.QuoteEntity
import ua.com.radiokot.pc.logic.model.Quote
import ua.com.radiokot.pc.logic.repository.base.RepositoryCache

class QuotesCache : RepositoryCache<Quote>() {
    private var dao = DbFactory.getAppDatabase().quoteDao

    fun deleteById(id: Long): Boolean {
        return mItems.find { it.id == id }?.let { delete(it) } ?: false
    }

    fun mergeForBook(bookId: Long, quotes: List<Quote>): Boolean {
        return merge(quotes) {
            it.bookId == bookId
        }
    }

    override fun isContentSame(first: Quote, second: Quote): Boolean {
        return first.text == second.text
    }

    override fun sortItems() {
        mItems.sort()
    }

    // region DB
    override fun getAllFromDb(): Single<List<Quote>> {
        return dao.getAll().map { it.map { it.toQuote() } }
    }

    override fun addToDb(items: List<Quote>) {
        doAsync {
            dao.insert(*items.map { QuoteEntity.fromQuote(it) }.toTypedArray())
        }
    }

    override fun updateInDb(items: List<Quote>) {
        doAsync {
            dao.update(*items.map { QuoteEntity.fromQuote(it) }.toTypedArray())
        }
    }

    override fun deleteFromDb(items: List<Quote>) {
        doAsync {
            dao.delete(*items.map { QuoteEntity.fromQuote(it) }.toTypedArray())
        }
    }

    override fun clearDb() {
        doAsync {
            dao.deleteAll()
        }
    }
    // endregion
}