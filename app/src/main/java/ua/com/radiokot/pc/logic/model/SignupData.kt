package ua.com.radiokot.pc.logic.model

import com.google.gson.annotations.SerializedName

/**
 * Created by Oleg Koretsky on 2/20/18.
 */
class SignupData(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("name")
    val name: String
)
