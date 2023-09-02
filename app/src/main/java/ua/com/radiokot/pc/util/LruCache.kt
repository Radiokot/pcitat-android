package ua.com.radiokot.pc.util

/**
 * [LruCache] with [LruCache.create] function passed as lambda.
 */
class LruCache<K, V>(
    maxSize: Int,
    private val creator: (K) -> V
) : android.util.LruCache<K, V>(maxSize) {
    override fun create(key: K): V {
        return creator(key)
    }
}
