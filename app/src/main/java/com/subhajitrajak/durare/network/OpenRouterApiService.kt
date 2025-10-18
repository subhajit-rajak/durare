package com.subhajitrajak.durare.network

import com.subhajitrajak.durare.data.models.OpenRouterRequest
import com.subhajitrajak.durare.data.models.OpenRouterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenRouterApiService {
    @POST("v1/chat/completions")
    suspend fun chatWithAI(
        @Body request: OpenRouterRequest,
        @Header("Authorization") auth: String
    ): Response<OpenRouterResponse>
}