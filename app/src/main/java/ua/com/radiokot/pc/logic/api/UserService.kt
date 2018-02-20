package ua.com.radiokot.pc.logic.api

import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import ua.com.radiokot.pc.logic.api.responses.ApiResponse
import ua.com.radiokot.pc.logic.model.LoginData
import ua.com.radiokot.pc.logic.model.SignupData
import ua.com.radiokot.pc.logic.model.User

/**
 * Created by Oleg Koretsky on 2/20/18.
 */
interface UserService {
    @POST("signup")
    fun signup(@Body signupData: SignupData): Observable<ApiResponse<User>>

    @POST("login")
    fun login(@Body loginData: LoginData): Observable<ApiResponse<User>>

    @GET("getUserInfo")
    fun getInfo(): Observable<ApiResponse<User>>
}