package ua.com.radiokot.pc.logic.repository.base

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject

abstract class SimpleMultipleItemsRepository<T> : MultipleItemsRepository<T>() {
    private var updateResultSubject: PublishSubject<Boolean>? = null

    private var updateDisposable: Disposable? = null
    override fun update(): Observable<Boolean> {
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

            val storedItemsObservable =
                    if (isNeverUpdated) getStoredItems() else Observable.empty()

            updateDisposable?.dispose()
            updateDisposable = storedItemsObservable.concatWith(
                    getItems()
                            .map {
                                storeItems(it)
                                it
                            }
            )
                    .subscribeBy(
                            onNext = { items ->
                                onNewItems(items)
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

            resultSubject
        }
    }
}