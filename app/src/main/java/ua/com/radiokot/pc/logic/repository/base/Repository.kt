package ua.com.radiokot.pc.logic.repository.base

import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import ua.com.radiokot.pc.logic.event_bus.events.PcEvent

/**
 * Contains common repository logic. Is a parent of all repositories.
 */
abstract class Repository {
    val errorsSubject: PublishSubject<Throwable> =
            PublishSubject.create<Throwable>()

    val loadingSubject: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)
    var isLoading: Boolean = false
        protected set(value) {
            if (field != value) {
                field = value
                loadingSubject.onNext(value)
            }
            field = value
        }

    var isFresh = false
        protected set
    var isNeverUpdated = true
        protected set

    abstract fun update(): Observable<Boolean>

    open fun invalidate() {
        synchronized(this) {
            isFresh = false
        }
    }

    open fun updateIfNotFresh(): Observable<Boolean> {
        return synchronized(this) {
            if (!isFresh) {
                update()
            } else {
                Observable.just(false)
            }
        }
    }

    open fun handleEvent(event: PcEvent) {}
}