package ua.com.radiokot.pc.logic.model

import com.google.gson.annotations.SerializedName

/**
 * Created by Oleg Koretsky on 2/20/18.
 */
class LoginData(
        @SerializedName("email")
        val email: String,
        @SerializedName("password")
        val password: String
)