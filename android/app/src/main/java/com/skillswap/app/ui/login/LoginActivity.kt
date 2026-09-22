package com.skillswap.app.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.skillswap.app.databinding.ActivityLoginBinding
import com.skillswap.app.ui.home.HomeActivity
import com.skillswap.app.ui.register.RegisterActivity
import com.skillswap.app.util.ServiceLocator
import com.skillswap.app.util.ViewModelFactory

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // If a valid session already exists, skip straight past the login
        // screen - Part 1 4.1: "no further screen is reachable without a
        // valid session token" implies the reverse too, on relaunch.
        val authRepository = ServiceLocator.authRepository(this)
        if (authRepository.isLoggedIn()) {
            goToHome()
            return
        }

        viewModel = ViewModelProvider(
            this,
            ViewModelFactory { LoginViewModel(authRepository) }
        )[LoginViewModel::class.java]

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            viewModel.login(email, password)
        }

        binding.btnGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        viewModel.uiState.observe(this) { state ->
            when (state) {
                is LoginUiState.Idle -> renderIdle()
                is LoginUiState.Loading -> renderLoading()
                is LoginUiState.Success -> {
                    Log.i(TAG, "Login succeeded for ${state.user.email}")
                    goToHome()
                }
                is LoginUiState.Error -> renderError(state.message)
            }
        }
    }

    private fun renderIdle() {
        binding.progressBar.visibility = View.GONE
        binding.btnLogin.isEnabled = true
        binding.tvError.visibility = View.GONE
    }

    private fun renderLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnLogin.isEnabled = false
        binding.tvError.visibility = View.GONE
    }

    private fun renderError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.btnLogin.isEnabled = true
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
    }

    private fun goToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    companion object {
        private const val TAG = "LoginActivity"
    }
}
