package ua.com.radiokot.pc.logic.repository

import io.reactivex.Single
import ua.com.radiokot.pc.logic.db.DbFactory
import ua.com.radiokot.pc.logic.db.entities.BookEntity
import ua.com.radiokot.pc.logic.model.Book
import ua.com.radiokot.pc.logic.repository.base.RepositoryCache
import kotlin.concurrent.thread

class BooksCache : RepositoryCache<Book>() {
    private var dao = DbFactory.getAppDatabase().bookDao

    fun deleteById(id: Long): Boolean {
        return mItems.find { it.id == id }?.let { delete(it) } ?: false
    }

    override fun sortItems() {
        mItems.sort()
    }

    override fun isContentSame(first: Book, second: Book): Boolean {
        return first.quotesCount == second.quotesCount
                && first.isTwitterBook == second.isTwitterBook
    }

    // region DB
    override fun getAllFromDb(): Single<List<Book>> {
        return dao.getAll().map { it.map { it.toBook() } }
    }

    override fun addToDb(items: List<Book>) {
        thread {
            dao.insert(*items.map { BookEntity.fromBook(it) }.toTypedArray())
        }
    }

    override fun updateInDb(items: List<Book>) {
        thread {
            dao.update(*items.map { BookEntity.fromBook(it) }.toTypedArray())
        }
    }

    override fun deleteFromDb(items: List<Book>) {
        thread {
            dao.delete(*items.map { BookEntity.fromBook(it) }.toTypedArray())
        }
    }

    override fun clearDb() {
        thread {
            dao.deleteAll()
        }
    }
    // endregion
}
