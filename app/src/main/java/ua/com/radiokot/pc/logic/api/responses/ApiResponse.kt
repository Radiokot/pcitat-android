package ua.com.radiokot.pc.logic.api.responses

import com.google.gson.annotations.SerializedName

/**
 * Created by Oleg Koretsky on 2/20/18.
 */
open class ApiResponse<out T> {
    @SerializedName("response")
    private val mData: T? = null

    val data: T
        get() = mData!!
}