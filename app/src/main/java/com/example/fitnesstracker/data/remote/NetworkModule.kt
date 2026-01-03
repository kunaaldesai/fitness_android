package com.example.fitnesstracker.data.remote

import com.example.fitnesstracker.AppConfig
import com.example.fitnesstracker.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object NetworkModule {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        isLenient = true
    }

    private fun loggingInterceptor(): HttpLoggingInterceptor {
        val level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.BASIC
        }
        return HttpLoggingInterceptor().apply { setLevel(level) }
    }

    private fun okHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor())
            .build()

    private fun retrofit(baseUrl: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .client(okHttpClient())
            .build()

    val workoutsApi: WorkoutsApi by lazy {
        retrofit(AppConfig.WORKOUTS_BASE_URL).create(WorkoutsApi::class.java)
    }

    val usersApi: UsersApi by lazy {
        retrofit(AppConfig.USERS_BASE_URL).create(UsersApi::class.java)
    }
}
