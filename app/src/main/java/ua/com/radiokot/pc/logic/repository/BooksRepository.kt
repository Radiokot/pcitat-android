package ua.com.radiokot.pc.logic.repository

import io.reactivex.Observable
import ua.com.radiokot.pc.logic.api.ApiFactory
import ua.com.radiokot.pc.logic.model.Book
import ua.com.radiokot.pc.logic.model.ExternalSiteBook
import ua.com.radiokot.pc.logic.repository.base.SimpleMultipleItemsRepository

/**
 * Holds user's books.
 */
class BooksRepository : SimpleMultipleItemsRepository<Book>() {
    override fun getItems(): Observable<List<Book>> {
        return ApiFactory.getBooksService().get()
                .map { it.data.items }
    }

    fun addExternalBook(externalSiteBook: ExternalSiteBook): Observable<Book> {
        return ApiFactory.getBooksService().add(externalSiteBook)
                .map { it.data }
                .doOnNext {
                    if (!itemsCache.contains(it)) {
                        itemsCache.add(0, it)
                        broadcast()
                    }
                }
    }
}