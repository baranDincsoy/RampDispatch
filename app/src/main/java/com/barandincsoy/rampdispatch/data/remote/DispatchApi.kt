package com.barandincsoy.rampdispatch.data.remote

import com.barandincsoy.rampdispatch.data.remote.dto.DispatchResponseDto
import retrofit2.http.GET

interface DispatchApi {

    @GET("fuel_orders.json")
    suspend fun getDispatchData(): DispatchResponseDto
}