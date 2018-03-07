package ua.com.radiokot.pc.logic.repository.base

import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

/**
 * Repository that holds a list of [T] items.
 */
abstract class MultipleItemsRepository<T> : Repository() {
    protected val itemsCache = mutableListOf<T>()

    val itemsSubject: BehaviorSubject<List<T>> =
            BehaviorSubject.createDefault<List<T>>(listOf())

    protected open fun broadcast() {
        itemsSubject.onNext(itemsCache.toList())
    }

    abstract protected fun getItems(): Observable<List<T>>

    protected open fun getStoredItems(): Observable<List<T>> {
        return Observable.empty()
    }

    protected open fun storeItems(items: List<T>) {}

    protected open fun onNewItems(newItems: List<T>) {
        isNeverUpdated = false
        isFresh = true

        itemsCache.clear()
        itemsCache.addAll(newItems)

        broadcast()
    }
}