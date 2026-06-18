package com.barandincsoy.rampdispatch.data.remote

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object NetworkModule {

    private const val BASE_URL =
        "https://raw.githubusercontent.com/baranDincsoy/RampDispatch/main/"

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    val dispatchApi: DispatchApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(DispatchApi::class.java)
    }
}