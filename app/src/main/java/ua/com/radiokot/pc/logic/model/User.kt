package ua.com.radiokot.pc.logic.model

import com.google.gson.annotations.SerializedName

/**
 * Created by Oleg Koretsky on 2/20/18.
 */
class User(
        @SerializedName("id")
        val id: Long? = null,
        @SerializedName("email")
        val email: String? = null,
        @SerializedName("name")
        val name: String? = null,
        @SerializedName("twitter")
        val twitterIntegration: TwitterIntegration? = null,
        @SerializedName("key")
        val authKey: String? = null
) {
    class TwitterIntegration(
            @SerializedName("username")
            var username: String? = null,
            @SerializedName("book")
            var bookId: Long? = null
    )

    /**
     * User's avatar from Twitter if present.
     */
    val avatarUrl: String?
        get() = twitterIntegration?.username?.let {
            "https://unavatar.now.sh/twitter/$it"
        }
}