package `in`.sendbirdpoc.api.di

import android.app.Application
import android.content.Context
import androidx.annotation.StringRes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import `in`.sendbirdpoc.api.ApiInterface
import `in`.sendbirdpoc.api.utils.UnauthorizedInterceptor
import `in`.sendbirdpoc.api.utils.hasInternetConnection
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class ApiModule {

    private val connectTimeUnit = 1L
    private val readTimeUnit = 1L
    private val writeTimeUnit = 1L

    @Provides
    @Singleton
    internal fun provideGson(): Gson {
        val gsonBuilder = GsonBuilder()
        return gsonBuilder.create()
    }

    @Provides
    @Singleton
    internal fun provideCache(application: Application): Cache {
        val cacheSize = (10 * 1024 * 1024).toLong() // 10 MB
        val httpCacheDirectory = File(application.cacheDir, "http-cache")
        return Cache(httpCacheDirectory, cacheSize)
    }

    @Provides
    internal fun providesUnAuthorizedInterceptor(application: Application): UnauthorizedInterceptor =
        UnauthorizedInterceptor(application)

    @Provides
    @Singleton
    internal fun provideOkhttpClient(
        cache: Cache, unauthorizedInterceptor: UnauthorizedInterceptor
    ): OkHttpClient {
        val httpClient =
            OkHttpClient.Builder().cache(cache).connectTimeout(connectTimeUnit, TimeUnit.MINUTES)
                .readTimeout(readTimeUnit, TimeUnit.MINUTES)
                .writeTimeout(writeTimeUnit, TimeUnit.MINUTES)
                .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .addInterceptor(unauthorizedInterceptor)
        return httpClient.build()
    }


    @Provides
    @Singleton
    internal fun provideRetrofit(gson: Gson, okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder().addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl("https://pf-chat.onrender.com/").client(okHttpClient).build()
    }

    @Provides
    @Singleton
    fun provideApiCallService(retrofit: Retrofit): ApiInterface {
        return retrofit.create(ApiInterface::class.java)
    }

    @Singleton
    class StringResourcesProvider @Inject constructor(
        @ApplicationContext private val context: Context
    ) {
        fun getString(@StringRes stringResId: Int): String {
            return context.getString(stringResId)
        }
    }

    @Singleton
    class IsInterNetConnectionAvailable @Inject constructor(
        @ApplicationContext private val context: Context
    ) {
        fun isConnected(): Boolean {
            return context.hasInternetConnection()
        }
    }
}