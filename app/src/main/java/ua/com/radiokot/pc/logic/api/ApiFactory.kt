package ua.com.radiokot.pc.logic.api

import android.util.Log
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.reactivex.schedulers.Schedulers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.internal.platform.Platform
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import ua.com.radiokot.pc.App
import ua.com.radiokot.pc.logic.exceptions.BadRequestException
import ua.com.radiokot.pc.logic.exceptions.ConflictException
import ua.com.radiokot.pc.logic.exceptions.NotAuthorizedException
import ua.com.radiokot.pc.logic.exceptions.NotFoundException
import ua.com.radiokot.pc.util.TLSSocketFactory
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit

/**
 * Created by Oleg Koretsky on 2/20/18.
 */
object ApiFactory {
    const val API_URL = "https://pc.radiokot.com.ua/api/"
    val TWITTER_OAUTH_URL: String
        get() = API_URL + "twitterOauth"
    const val OAUTH_RESULT_URI = "pcitat://oauth_result"

    private val REQUEST_TIMEOUT = 20 * 1000

    private var userService: UserService? = null
    private var booksService: BooksService? = null
    private var quotesService: QuotesService? = null
    private var liveLibService: LiveLibService? = null

    // region Service creation
    fun getUserService(): UserService {
        return userService ?: createBaseRetrofitConfig(getBaseHttpClient())
                .build()
                .create(UserService::class.java)
                .also { userService = it }
    }

    fun getBooksService(): BooksService {
        return booksService ?: createBaseRetrofitConfig(getBaseHttpClient())
                .build()
                .create(BooksService::class.java)
                .also { booksService = it }
    }

    fun getQuotesService(): QuotesService {
        return quotesService ?: createBaseRetrofitConfig(getBaseHttpClient())
                .build()
                .create(QuotesService::class.java)
                .also { quotesService = it }
    }

    fun getLiveLibService(): LiveLibService {
        return liveLibService ?: createBaseRetrofitConfig(getBaseHttpClient())
                .build()
                .create(LiveLibService::class.java)
                .also { liveLibService = it }
    }
    // endregion

    // region Util
    private fun createBaseRetrofitConfig(httpClient: OkHttpClient = getBaseHttpClient()):
            Retrofit.Builder {
        return Retrofit.Builder()
                .baseUrl(API_URL)
                .client(httpClient)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(createBaseJsonConverterFactory())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(
                        Schedulers.newThread()))
    }

    private fun createBaseJsonConverterFactory(): GsonConverterFactory {
        return GsonConverterFactory.create(getBaseGson())
    }

    // region Interceptors
    private fun createLoggingInterceptor(): Interceptor {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        return loggingInterceptor
    }

    private fun createHttpCodesWrappingInterceptor(): Interceptor {
        return Interceptor { chain ->
            val response = chain.proceed(chain.request())

            when (response.code()) {
                HttpURLConnection.HTTP_CONFLICT -> throw ConflictException()
                HttpURLConnection.HTTP_BAD_REQUEST -> throw BadRequestException()
                HttpURLConnection.HTTP_NOT_FOUND -> throw NotFoundException()
                HttpURLConnection.HTTP_UNAUTHORIZED -> throw NotAuthorizedException()
            }

            response
        }
    }
    // endregion

    fun getBaseHttpClient(): OkHttpClient {
        synchronized(this) {
            val clientBuilder = OkHttpClient.Builder()
                    .readTimeout(REQUEST_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
                    .connectTimeout(REQUEST_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)

            val socketFactory = TLSSocketFactory()
            if (Platform.get().trustManager(socketFactory) != null) {
                clientBuilder.sslSocketFactory(socketFactory)
            } else {
                Log.e("ApiFactory", "Unable to use modern TLS socket factory")
            }

            clientBuilder
                    .addInterceptor(createLoggingInterceptor())
                    .addInterceptor(createHttpCodesWrappingInterceptor())

            clientBuilder.cookieJar(getBaseCookieJar())

            val simulateLongResponses = false
            if (simulateLongResponses) {
                clientBuilder.addInterceptor { chain ->
                    Thread.sleep(3000)
                    chain.proceed(chain.request())
                }
            }

            return clientBuilder.build()
        }
    }

    fun getBaseGson(): Gson {
        val builder = GsonBuilder()
                .serializeNulls()
        return builder.create()
    }

    private var cookieJar: PersistentCookieJar? = null
    fun getBaseCookieJar(): PersistentCookieJar {
        return cookieJar ?:
            PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(App.instance))
                    .also { cookieJar = it }
    }
    // endregion
}