package ua.com.radiokot.pc.logic.repository.base

import io.reactivex.subjects.BehaviorSubject

/**
 * Repository that holds a single [T] item.
 */
abstract class SingleItemRepository<T>: Repository() {
    protected var item: T? = null

    val itemSubject: BehaviorSubject<T> = BehaviorSubject.create()

    protected open fun broadcast() {
        item?.let { itemSubject.onNext(it) }
    }
}