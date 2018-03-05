package ua.com.radiokot.pc.logic.model

import com.google.gson.annotations.SerializedName

class LiveLibSearchResult(
        @SerializedName("error_code")
        val errorCode: Int? = null,
        @SerializedName("content")
        val contentHtml: String? = null
)