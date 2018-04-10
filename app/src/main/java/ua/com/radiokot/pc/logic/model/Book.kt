package ua.com.radiokot.pc.logic.model

import com.google.gson.annotations.SerializedName

/**
 * Created by Oleg Koretsky on 2/20/18.
 */
class Book(
        @SerializedName("id")
        val id: Long? = null,
        @SerializedName("pagingToken")
        val pagingToken: Int? = null,
        @SerializedName("title")
        val title: String? = null,
        @SerializedName("author")
        val authorName: String? = null,
        @SerializedName("cover")
        val coverUrl: String? = null,
        @SerializedName("quotesCount")
        var quotesCount: Int? = null,
        @SerializedName("twitterBook")
        var isTwitterBook: Boolean? = null
) : Comparable<Book> {
    override fun hashCode(): Int {
        return id?.hashCode() ?: super.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return id?.equals((other as? Book)?.id) ?: super.equals(other)
    }

    override fun compareTo(other: Book): Int {
        return if (this.pagingToken != null && other.pagingToken != null)
            other.pagingToken.compareTo(this.pagingToken)
        else 0
    }
}