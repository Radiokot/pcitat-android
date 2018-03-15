package ua.com.radiokot.pc.logic.event_bus

import android.util.Log
import com.trello.rxlifecycle2.android.ActivityEvent
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity
import com.trello.rxlifecycle2.kotlin.bindUntilEvent
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import ua.com.radiokot.pc.logic.event_bus.events.PcEvent
import ua.com.radiokot.pc.util.ObservableTransformers

/**
 * Application event bus.
 */
object PcEvents {
    private val eventsSubject = PublishSubject.create<PcEvent>()

    val observable: Observable<PcEvent>
        get() = eventsSubject

    fun publish(event: PcEvent) {
        Log.i("Event Bus", "Emitted ${event.javaClass.simpleName}")
        eventsSubject.onNext(event)
    }

    fun subscribe(onEvent: (PcEvent) -> Unit): Disposable {
        return observable
                .compose(ObservableTransformers.defaultSchedulers())
                .subscribeBy(onEvent)
    }

    fun subscribeUntilDestroy(activity: RxAppCompatActivity,
                              onEvent: (PcEvent) -> Unit): Disposable {
        return observable
                .bindUntilEvent(activity.lifecycle(), ActivityEvent.DESTROY)
                .compose(ObservableTransformers.defaultSchedulers())
                .subscribeBy(onEvent)
    }
}