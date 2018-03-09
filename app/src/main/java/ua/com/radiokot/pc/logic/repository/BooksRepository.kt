package ua.com.radiokot.pc.logic.repository

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.doAsync
import ua.com.radiokot.pc.logic.api.ApiFactory
import ua.com.radiokot.pc.logic.db.DbFactory
import ua.com.radiokot.pc.logic.db.entities.BookEntity
import ua.com.radiokot.pc.logic.model.Book
import ua.com.radiokot.pc.logic.model.ExternalSiteBook
import ua.com.radiokot.pc.logic.repository.base.SimpleMultipleItemsRepository
import ua.com.radiokot.pc.util.extensions.doNotEmitEmptyList

/**
 * Holds user's books.
 */
class BooksRepository : SimpleMultipleItemsRepository<Book>() {

    override fun getItems(): Observable<List<Book>> {
        return ApiFactory.getBooksService().get()
                .map { it.data.items }
    }

    override fun getStoredItems(): Observable<List<Book>> {
        return DbFactory.getAppDatabase().bookDao.getAll()
                .subscribeOn(Schedulers.io())
                .map { it.map { it.toBook() } }
                .toObservable()
                .doNotEmitEmptyList()
    }

    fun addExternalBook(externalSiteBook: ExternalSiteBook): Observable<Book> {
        return ApiFactory.getBooksService().add(externalSiteBook)
                .map { it.data }
                .doOnNext {
                    if (!itemsCache.contains(it)) {
                        itemsCache.add(0, it)
                        storeSingleBook(it)
                        broadcast()
                    }
                }
    }

    override fun storeItems(items: List<Book>) {
        super.storeItems(items)
        doAsync {
            var order = items.size
            DbFactory.getAppDatabase().bookDao.apply {
                insert(*items
                        .map {
                            BookEntity.fromBook(it, --order)
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

    private fun storeSingleBook(book: Book) {
        doAsync {
            DbFactory.getAppDatabase().bookDao.insert(BookEntity.fromBook(book,
                    itemsCache.size - 1))
        }
    }
}