package com.bid.ai.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bid.ai.data.repository.AuthRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LoginUiState(
    val loading: Boolean = false,
    val message: String? = null,
    val success: Boolean = false,
    val token: String? = null
)

class LoginViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState(
                message = "Enter your email and password"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState(loading = true)

            try {
                val response = repository.login(email.trim(), password)

                if (response.isSuccessful) {
                    _uiState.value = LoginUiState(
                        message = "Login successful",
                        success = true,
                        token = response.body()?.token
                    )
                } else {
                    _uiState.value = LoginUiState(
                        message = when (response.code()) {
                            401 -> "Invalid email or password"
                            else -> "Login failed (${response.code()})"
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = LoginUiState(
                    message = "Could not connect to server. Check your network."
                )
            }
        }
    }
}
