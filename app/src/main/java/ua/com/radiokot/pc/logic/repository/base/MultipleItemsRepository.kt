package ua.com.radiokot.pc.logic.repository.base

import io.reactivex.subjects.BehaviorSubject

/**
 * Repository that holds a list of [T] items.
 */
abstract class MultipleItemsRepository<T>: Repository() {
    protected val itemsCache = mutableListOf<T>()

    val itemsSubject: BehaviorSubject<List<T>> =
            BehaviorSubject.createDefault<List<T>>(listOf())

    protected open fun broadcast() {
        itemsSubject.onNext(itemsCache.toList())
    }
}