package ua.com.radiokot.pc.logic.api

import io.reactivex.Completable
import io.reactivex.Observable
import retrofit2.http.*
import ua.com.radiokot.pc.logic.api.responses.ApiArrayResponse
import ua.com.radiokot.pc.logic.api.responses.ApiResponse
import ua.com.radiokot.pc.logic.model.Quote

/**
 * Created by Oleg Koretsky on 2/20/18.
 */
interface QuotesService {
    @GET("quotes")
    fun get(): Observable<ApiArrayResponse<Quote>>

    @GET("quotes")
    fun getByBookId(@Query("book") bookId: Long?): Observable<ApiArrayResponse<Quote>>

    @POST("quotes")
    fun add(@Query("book") bookId: Long?, @Body quote: Quote): Observable<ApiResponse<Quote>>

    @PATCH("quotes")
    fun update(@Query("id") id: Long?, @Body quote: Quote): Observable<ApiResponse<Quote>>

    @DELETE("quotes")
    fun delete(@Query("id") id: Long?): Completable
}