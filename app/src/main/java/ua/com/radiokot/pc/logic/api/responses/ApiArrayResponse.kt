package ua.com.radiokot.pc.logic.api.responses

import com.google.gson.annotations.SerializedName

/**
 * Created by Oleg Koretsky on 2/20/18.
 */
class ApiArrayResponse<out T> : ApiResponse<ApiArrayResponse.ApiArray<T>>() {
    class ApiArray<out T> {
        @SerializedName("items")
        private val mItems: List<T>? = null

        @SerializedName("count")
        val count: Int = 0

        val items: List<T>
            get() = mItems!!
    }
}
