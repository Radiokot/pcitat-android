package ua.com.radiokot.pc.logic.model

import com.google.gson.annotations.SerializedName

data class ExternalSiteBook(
    val title: String? = null,
    val authorName: String? = null,
    @SerializedName("url")
    val externalUrl: String? = null
)
