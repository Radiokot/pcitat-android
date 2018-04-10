package ua.com.radiokot.pc.logic.repository

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import ua.com.radiokot.pc.logic.api.ApiFactory
import ua.com.radiokot.pc.logic.model.Quote
import ua.com.radiokot.pc.logic.repository.base.Repository
import ua.com.radiokot.pc.util.ObservableTransformers

class AdvancedQuotesRepository : Repository() {
    private val cache = QuotesCache()

    val itemsSubject: BehaviorSubject<List<Quote>> =
            BehaviorSubject.createDefault<List<Quote>>(listOf())

    init {
        cache.loadFromDb()
                .compose(ObservableTransformers.defaultSchedulersCompletable())
                .subscribeBy(
                        onComplete = { broadcast() },
                        onError = { errorsSubject.onNext(it) }
                )
    }

    private fun broadcast() {
        itemsSubject.onNext(cache.items)

    }

    private var updateResultSubject: PublishSubject<Boolean>? = null

    private var updateDisposable: Disposable? = null

    override fun update(): Observable<Boolean> {
        return updateQuotes()
    }

    fun updateBook(bookId: Long): Observable<Boolean> {
        return updateQuotes(bookId)
    }

    private fun updateQuotes(bookId: Long? = null): Observable<Boolean> {
        return synchronized(this) {
            val resultSubject = updateResultSubject.let {
                if (it == null) {
                    val new = PublishSubject.create<Boolean>()
                    updateResultSubject = new
                    new
                } else {
                    it
                }
            }

            isLoading = true

            updateDisposable?.dispose()
            updateDisposable = getItems(bookId)
                    .subscribeBy(
                            onNext = {
                                onNewItems(it, bookId)
                            },
                            onComplete = {
                                isLoading = false

                                updateResultSubject = null
                                resultSubject.onNext(true)
                                resultSubject.onComplete()
                            },
                            onError = {
                                isLoading = false
                                errorsSubject.onNext(it)

                                updateResultSubject = null
                                resultSubject.onError(it)
                                resultSubject.onComplete()
                            }
                    )

            return@synchronized resultSubject
        }
    }

    private fun getItems(bookId: Long?): Observable<List<Quote>> {
        return ApiFactory.getQuotesService()
                .let {
                    if (bookId != null)
                        it.getByBookId(bookId)
                    else
                        it.get()
                }
                .map { it.data.items }
    }

    private fun onNewItems(newItems: List<Quote>, bookId: Long?) {
        if (bookId != null) {
            cache.mergeForBook(bookId, newItems)
        } else {
            cache.merge(newItems)
        }

        isFresh = true

        broadcast()
    }
}