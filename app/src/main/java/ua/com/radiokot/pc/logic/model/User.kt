package ua.com.radiokot.pc.logic.model

import com.google.gson.annotations.SerializedName

/**
 * Created by Oleg Koretsky on 2/20/18.
 */
class User(
    @SerializedName("id")
    val id: Long,
    @SerializedName("email")
    val email: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("twitter")
    val twitterIntegration: TwitterIntegration? = null,
    @SerializedName("key")
    val authKey: String
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
