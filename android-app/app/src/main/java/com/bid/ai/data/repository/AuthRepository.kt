
package com.bid.ai.data.repository

import com.bid.ai.data.api.RetrofitClient
import com.bid.ai.data.model.LoginRequest
import com.bid.ai.data.model.SignupRequest

class AuthRepository {

    private val api = RetrofitClient.authApi

    suspend fun login(email: String, password: String) =
        api.login(LoginRequest(email, password))

    suspend fun signup(
        name: String,
        email: String,
        mobileNumber: String?,
        password: String
    ) = api.signup(
        SignupRequest(name, email, mobileNumber, password)
    )
}
