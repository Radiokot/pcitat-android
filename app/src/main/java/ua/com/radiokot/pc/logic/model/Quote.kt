package ua.com.radiokot.pc.logic.model

import com.google.gson.annotations.SerializedName

/**
 * Created by Oleg Koretsky on 2/20/18.
 */
class Quote(
    @SerializedName("id")
    val id: Long? = null,
    @SerializedName("bookId")
    val bookId: Long? = null,
    @SerializedName("bookTitle")
    val bookTitle: String? = null,
    @SerializedName("text")
    var text: String?,
    @SerializedName("is_public")
    var isPublic: Boolean,
) : Comparable<Quote> {
    override fun hashCode(): Int {
        return id?.hashCode() ?: super.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return id?.equals((other as? Quote)?.id) ?: super.equals(other)
    }

    override fun compareTo(other: Quote): Int {
        return if (this.id != null && other.id != null) other.id.compareTo(this.id)
        else 0
    }
}
