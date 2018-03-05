package ua.com.radiokot.pc.logic.api

import io.reactivex.Completable
import io.reactivex.Observable
import retrofit2.http.*
import ua.com.radiokot.pc.logic.api.responses.ApiArrayResponse
import ua.com.radiokot.pc.logic.api.responses.ApiResponse
import ua.com.radiokot.pc.logic.model.Book
import ua.com.radiokot.pc.logic.model.ExternalSiteBook
import ua.com.radiokot.pc.logic.model.containers.BookIdContainer

/**
 * Created by Oleg Koretsky on 2/20/18.
 */
interface BooksService {
    @GET("books")
    fun get(): Observable<ApiArrayResponse<Book>>

    @GET("books")
    fun getById(@Query("id") id: Long?): Observable<ApiResponse<Book>>

    @POST("books")
    fun add(@Body externalBook: ExternalSiteBook): Observable<ApiResponse<Book>>

    @DELETE("books")
    fun delete(@Query("id") id: Long?): Completable

    @POST("setTwitterBook")
    fun setTwitterBook(@Body bookId: BookIdContainer): Completable
}