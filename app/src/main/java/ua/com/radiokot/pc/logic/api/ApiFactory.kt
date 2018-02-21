package ua.com.radiokot.pc.logic.api

import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.reactivex.schedulers.Schedulers
import okhttp3.*
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
import java.net.HttpURLConnection
import java.util.*
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext

/**
 * Created by Oleg Koretsky on 2/20/18.
 */
object ApiFactory {
    private val API_URL = "https://pc.radiokot.com.ua/api/"
    private val REQUEST_TIMEOUT = 20 * 1000

    private var userService: UserService? = null
    private var booksService: BooksService? = null
    private var quotesService: QuotesService? = null

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
        val sslContext = SSLContext.getInstance("TLSv1.2")
        sslContext.init(null, null, null)
        val sslFactory = sslContext.socketFactory

        val clientBuilder = OkHttpClient.Builder()
                .readTimeout(REQUEST_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
                .connectTimeout(REQUEST_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
                .sslSocketFactory(sslFactory)

        val connectionSpec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_2)
                .cipherSuites(
                        CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                        CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                        CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256)
                .build()

        clientBuilder.connectionSpecs(Arrays.asList(connectionSpec, ConnectionSpec.CLEARTEXT))

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

    fun getBaseGson(): Gson {
        val builder = GsonBuilder()
                .serializeNulls()
        return builder.create()
    }

    fun getBaseCookieJar(): PersistentCookieJar {
        return PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(App.instance))
    }
    // endregion
}