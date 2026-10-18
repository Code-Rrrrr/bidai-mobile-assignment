package com.bid.ai

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bid.ai.ui.login.LoginViewModel
import kotlinx.coroutines.launch

import com.bid.ai.ui.login.LoginBackgroundDrawable

import android.content.Intent
import com.bid.ai.data.SessionManager

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        window.decorView.setBackgroundDrawable(LoginBackgroundDrawable())

        val goToSignup = findViewById<TextView>(R.id.goToSignup)

        goToSignup.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val statusText = findViewById<TextView>(R.id.statusText)
        val progress = findViewById<ProgressBar>(R.id.loginProgress)

        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()

            emailInput.error = null
            passwordInput.error = null

            if (email.isBlank()) {
                emailInput.error = "Email is required"
                return@setOnClickListener
            }

            if (password.isBlank()) {
                passwordInput.error = "Password is required"
                return@setOnClickListener
            }

            viewModel.login(email, password)
        }

        val sessionManager = SessionManager(this)
        var navigatedToHome = false


        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->

                    progress.visibility =
                        if (state.loading) View.VISIBLE else View.GONE

                    loginButton.isEnabled = !state.loading

                    statusText.text = state.message.orEmpty()
                    statusText.visibility =
                        if (state.message != null) View.VISIBLE
                        else View.GONE

                    if (state.success && !navigatedToHome) {
                        val token = state.token

                        if (!token.isNullOrBlank()) {
                            sessionManager.saveToken(token)
                            navigatedToHome = true

                            startActivity(
                                Intent(this@MainActivity, HomeActivity::class.java)
                            )
                            finish()
                        } else {
                            statusText.text =
                                "Login succeeded but no token was received"
                            statusText.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }
}
