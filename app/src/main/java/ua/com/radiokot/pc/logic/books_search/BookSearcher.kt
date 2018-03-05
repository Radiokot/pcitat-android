package ua.com.radiokot.pc.logic.books_search

import io.reactivex.Observable
import ua.com.radiokot.pc.logic.model.ExternalSiteBook

interface BookSearcher {
    fun search(query: String): Observable<List<ExternalSiteBook>>
}