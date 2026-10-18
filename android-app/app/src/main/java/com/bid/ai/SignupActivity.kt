
package com.bid.ai

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.bid.ai.data.api.RetrofitClient
import com.bid.ai.data.model.SignupRequest
import kotlinx.coroutines.launch
import org.json.JSONObject

import com.bid.ai.ui.login.LoginBackgroundDrawable

class SignupActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        window.decorView.setBackgroundDrawable(LoginBackgroundDrawable())

        val nameInput = findViewById<EditText>(R.id.nameInput)
        val mobileInput = findViewById<EditText>(R.id.mobileInput)
        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val confirmPasswordInput =
            findViewById<EditText>(R.id.confirmPasswordInput)

        val signupButton = findViewById<Button>(R.id.signupButton)
        val statusText = findViewById<TextView>(R.id.signupStatus)
        val progressBar = findViewById<ProgressBar>(R.id.signupProgress)
        val goToLogin = findViewById<TextView>(R.id.goToLogin)

        goToLogin.setOnClickListener {
            finish()
        }

        signupButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val mobile = mobileInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()
            val confirmPassword = confirmPasswordInput.text.toString()

            statusText.visibility = View.VISIBLE

            when {
                name.isBlank() ->
                    statusText.text = "Please enter your name"

                mobile.isBlank() ->
                    statusText.text = "Please enter your mobile number"

                email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS
                    .matcher(email).matches() ->
                    statusText.text = "Please enter a valid email address"

                password.length < 8 ->
                    statusText.text = "Password must be at least 8 characters"

                password != confirmPassword ->
                    statusText.text = "Passwords do not match"

                else -> {
                    statusText.visibility = View.GONE
                    signupButton.isEnabled = false
                    progressBar.visibility = View.VISIBLE

                    lifecycleScope.launch {
                        try {
                            val response = RetrofitClient.authApi.signup(
                                SignupRequest(
                                    name = name,
                                    email = email,
                                    mobile_number = mobile,
                                    password = password
                                )
                            )

                            if (response.isSuccessful) {
                                statusText.text =
                                    "Account created! Please log in."
                                statusText.setTextColor(
                                    android.graphics.Color.rgb(46, 125, 50)
                                )
                                statusText.visibility = View.VISIBLE

                                // Return to Login after successful signup.
                                kotlinx.coroutines.delay(1000)
                                finish()
                            } else {
                                val errorMessage = try {
                                    JSONObject(
                                        response.errorBody()?.string() ?: ""
                                    ).optString(
                                        "message",
                                        "Signup failed. Please try again."
                                    )
                                } catch (_: Exception) {
                                    "Signup failed. Please try again."
                                }

                                statusText.text = errorMessage
                                statusText.setTextColor(
                                    android.graphics.Color.RED
                                )
                                statusText.visibility = View.VISIBLE
                            }
                        } catch (e: Exception) {
                            statusText.text =
                                "Could not connect to server. Check your connection."
                            statusText.setTextColor(
                                android.graphics.Color.RED
                            )
                            statusText.visibility = View.VISIBLE
                        } finally {
                            progressBar.visibility = View.GONE
                            signupButton.isEnabled = true
                        }
                    }
                }
            }
        }
    }
}
