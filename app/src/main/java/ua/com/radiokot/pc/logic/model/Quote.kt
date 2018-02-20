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
        val text: String? = null
)