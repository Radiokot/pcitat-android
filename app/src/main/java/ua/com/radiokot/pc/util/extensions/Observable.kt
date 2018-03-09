package ua.com.radiokot.pc.util.extensions

import io.reactivex.Observable

/**
 * Transform [Observable] of [List] to be empty if the list is empty.
 */
fun <T> Observable<List<T>>.doNotEmitEmptyList(): Observable<List<T>> {
    return flatMap { if (it.isEmpty()) Observable.empty() else Observable.just(it) }
}