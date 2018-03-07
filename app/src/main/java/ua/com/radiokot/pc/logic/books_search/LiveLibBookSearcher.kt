package ua.com.radiokot.pc.logic.books_search

import io.reactivex.Observable
import org.jsoup.Jsoup
import ua.com.radiokot.pc.logic.api.ApiFactory
import ua.com.radiokot.pc.logic.model.ExternalSiteBook
import ua.com.radiokot.pc.logic.model.LiveLibSearchResult

/**
 * Searches for books on LiveLib.
 */
class LiveLibBookSearcher : BookSearcher {
    companion object {
        private val LIVELIB_ROOT_URL = "https://www.livelib.ru/"
    }
    override fun search(query: String): Observable<List<ExternalSiteBook>> {
        return ApiFactory.getLiveLibService()
                .search(query)
                .flatMap { parseLiveLibResponse(it) }
    }

    private fun parseLiveLibResponse(result: LiveLibSearchResult):
            Observable<List<ExternalSiteBook>> {
        return Observable.defer {
            val books = mutableListOf<ExternalSiteBook>()

            val resultHtmlString = result.contentHtml
                    ?: return@defer Observable.just(books)
            val resultHtml = Jsoup.parse(resultHtmlString)

            val rows = resultHtml.select(".object-edition")
            rows.forEach { row ->
                val linkElement = row.selectFirst(".title")
                linkElement.setBaseUri(LIVELIB_ROOT_URL)
                val authorElement = row.selectFirst(".description")

                val title = linkElement.text()
                val author = authorElement.text()
                val url = linkElement.absUrl("href")

                if (title.isNotEmpty() && author.isNotEmpty() && url.isNotEmpty()) {
                    books.add(ExternalSiteBook(title, author, url))
                }
            }

            Observable.just(books)
        }
    }
}