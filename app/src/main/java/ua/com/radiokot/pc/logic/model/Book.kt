package ua.com.radiokot.pc.logic.model

import com.google.gson.annotations.SerializedName

/**
 * Created by Oleg Koretsky on 2/20/18.
 */
class Book(
        @SerializedName("id")
        val id: Long? = null,
        @SerializedName("title")
        val title: String? = null,
        @SerializedName("author")
        val authorName: String? = null,
        @SerializedName("cover")
        val coverUrl: String? = null,
        @SerializedName("quotesCount")
        val quotesCount: Int? = null,
        @SerializedName("twitterBook")
        val isTwitterBook: Boolean? = null
    )