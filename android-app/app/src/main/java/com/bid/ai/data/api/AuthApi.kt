
package com.bid.ai.data.api

import com.bid.ai.data.model.LoginRequest
import com.bid.ai.data.model.LoginResponse
import com.bid.ai.data.model.SignupRequest
import com.bid.ai.data.model.SignupResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("api/auth/signup")
    suspend fun signup(
        @Body request: SignupRequest
    ): Response<SignupResponse>
}
