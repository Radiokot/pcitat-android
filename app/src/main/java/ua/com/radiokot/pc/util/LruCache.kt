package ua.com.radiokot.pc.util

import android.support.v4.util.LruCache

/**
 * [LruCache] with [LruCache.create] function passed as lambda.
 */
class LruCache<K, V>(maxSize: Int,
                     private val creator: (K) -> V) : LruCache<K, V>(maxSize) {
    override fun create(key: K): V {
        return creator(key)
    }
}