package com.godgod.testinappbilling.di

import android.util.Log.DEBUG
import com.android.billingclient.BuildConfig
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private val BASE_URL by lazy { "https://n3ms3jhii1.execute-api.ap-northeast-2.amazonaws.com/dev/"  }
    @Provides
    fun provideGson(): Gson = Gson()
    @Provides
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .addNetworkInterceptor(
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
            ).build()

    @Provides
    fun provideBaseUrlRetrofit(gson: Gson, okHttpClient: OkHttpClient): Retrofit = createRetrofit(BASE_URL, gson, okHttpClient)
    private fun createRetrofit(url: String, gson: Gson, okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .build()
}