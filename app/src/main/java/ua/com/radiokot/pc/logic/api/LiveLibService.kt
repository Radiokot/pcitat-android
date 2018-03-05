package ua.com.radiokot.pc.logic.api

import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST
import ua.com.radiokot.pc.logic.model.LiveLibSearchResult

interface LiveLibService {
    @FormUrlEncoded
    @Headers("Accept: text/javascript, text/html, application/xml, text/xml, */*",
            "X-Requested-With: XMLHttpRequest")
    @POST("https://www.livelib.ru/main/search")
    fun search(@Field("text") query: String,
               @Field("object_alias") objectAlias: String = "booksauthors"):
            Observable<LiveLibSearchResult>
}