package ua.com.radiokot.pc.logic.repository

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.doAsync
import ua.com.radiokot.pc.logic.api.ApiFactory
import ua.com.radiokot.pc.logic.db.DbFactory
import ua.com.radiokot.pc.logic.db.entities.BookEntity
import ua.com.radiokot.pc.logic.model.Book
import ua.com.radiokot.pc.logic.model.ExternalSiteBook
import ua.com.radiokot.pc.logic.model.containers.BookIdContainer
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

    fun setTwitterBook(bookId: Long): Completable {
        return ApiFactory.getBooksService().setTwitterBook(BookIdContainer(bookId))
                .doOnComplete {
                    val prevTwitterBook = itemsCache.find { it.isTwitterBook == true }
                    val selectedBook = itemsCache.find { it.id == bookId } ?: return@doOnComplete

                    prevTwitterBook?.isTwitterBook = false
                    selectedBook.isTwitterBook = true
                    broadcast()

                    updateBooks(selectedBook, prevTwitterBook)
                }
    }

    fun delete(bookId: Long): Completable {
        return ApiFactory.getBooksService().delete(bookId)
                .doOnComplete {
                    val deleted = itemsCache.find { it.id == bookId }

                    itemsCache.remove(deleted)
                    broadcast()

                    deleteBooks(deleted)
                }
    }

    // region Db
    override fun storeItems(items: List<Book>) {
        super.storeItems(items)
        doAsync {
            DbFactory.getAppDatabase().bookDao.apply {
                insert(*items
                        .map {
                            BookEntity.fromBook(it)
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
            DbFactory.getAppDatabase().bookDao.insert(BookEntity.fromBook(book))
        }
    }

    private fun updateBooks(vararg books: Book?) {
        doAsync {
            DbFactory.getAppDatabase().bookDao.update(
                    *books
                            .filterNotNull()
                            .map {
                                BookEntity.fromBook(it)
                            }
                            .toTypedArray()
            )
        }
    }

    private fun deleteBooks(vararg books: Book?) {
        doAsync {
            DbFactory.getAppDatabase().bookDao.delete(
                    *books
                            .filterNotNull()
                            .map {
                                BookEntity.fromBook(it)
                            }
                            .toTypedArray()
            )
        }
    }
    // endregion
}