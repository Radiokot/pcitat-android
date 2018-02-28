package ua.com.radiokot.pc.logic.repository.base

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject

abstract class SimpleSingleItemRepository<T : Any> : SingleItemRepository<T>() {
    abstract protected fun getItem(): Observable<T>

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

            item = null
            isLoading = true

            updateDisposable?.dispose()
            updateDisposable = getItem()
                    .subscribeBy(
                            onNext = { newItem: T ->
                                isNewerUpdated = false
                                isFresh = true
                                isLoading = false

                                item = newItem

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