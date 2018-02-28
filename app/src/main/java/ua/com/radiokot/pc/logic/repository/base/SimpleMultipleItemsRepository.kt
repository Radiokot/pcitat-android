package ua.com.radiokot.pc.logic.repository.base

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject

abstract class SimpleMultipleItemsRepository<T> : MultipleItemsRepository<T>() {
    abstract protected fun getItems(): Observable<List<T>>

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

            itemsCache.clear()
            isLoading = true

            updateDisposable?.dispose()
            updateDisposable = getItems()
                    .subscribeBy(
                            onNext = { items ->
                                isNewerUpdated = false
                                isFresh = true
                                isLoading = false

                                itemsCache.addAll(items)

                                broadcast()

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