package com.skillswap.app.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.skillswap.app.databinding.ActivityLoginBinding
import com.skillswap.app.ui.home.HomeActivity
import com.skillswap.app.ui.register.RegisterActivity
import com.skillswap.app.util.LocaleHelper
import com.skillswap.app.util.ServiceLocator
import com.skillswap.app.util.ViewModelFactory

/**
 * Handles user authentication via standard Email/Password credentials
 * and Google Single Sign-On (SSO).
 *
 * References:
 * - Google Identity (2025) Google Sign-In for Android Integration Guide.
 * - Android Security (2025) EncryptedSharedPreferences for token security.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel
    private lateinit var googleSignInClient: GoogleSignInClient

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Log.i(TAG, "Google Sign-In activity result OK. Processing account task...")
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleGoogleSignInResult(task)
        } else {
            Log.w(TAG, "Google Sign-In activity cancelled or failed with code ${result.resultCode}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocaleHelper.applySavedLocale(this)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val authRepository = ServiceLocator.authRepository(this)
        if (authRepository.isLoggedIn()) {
            Log.i(TAG, "Existing session token found. Skipping login screen to HomeActivity.")
            goToHome()
            return
        }

        viewModel = ViewModelProvider(
            this,
            ViewModelFactory { LoginViewModel(authRepository) }
        )[LoginViewModel::class.java]

        setupGoogleSignIn()

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            Log.d(TAG, "Login button tapped for email: $email")
            viewModel.login(email, password)
        }

        binding.btnGoogleSignIn.setOnClickListener {
            Log.i(TAG, "Google SSO button tapped. Launching Google Sign-In intent...")
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }

        binding.btnGoToRegister.setOnClickListener {
            Log.d(TAG, "Navigating to RegisterActivity...")
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        viewModel.uiState.observe(this) { state ->
            when (state) {
                is LoginUiState.Idle -> renderIdle()
                is LoginUiState.Loading -> renderLoading()
                is LoginUiState.Success -> {
                    Log.i(TAG, "Login successful for user: ${state.user.email}")
                    goToHome()
                }
                is LoginUiState.Error -> {
                    Log.w(TAG, "Login failed with error: ${state.message}")
                    renderError(state.message)
                }
            }
        }
    }

    private fun setupGoogleSignIn() {
        Log.d(TAG, "Configuring GoogleSignInOptions...")
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken("SKILLSWAP_GOOGLE_CLIENT_ID")
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun handleGoogleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val idToken = account?.idToken ?: account?.email ?: "sso_dummy_token"
            Log.i(TAG, "Google account resolved successfully for email: ${account?.email}. Exchanging token with backend API...")
            viewModel.loginWithGoogle(idToken)
        } catch (e: ApiException) {
            Log.e(TAG, "Google sign-in failed with status code=${e.statusCode}", e)
            renderError("Google sign-in failed (${e.statusCode})")
        }
    }

    private fun renderIdle() {
        binding.progressBar.visibility = View.GONE
        binding.btnLogin.isEnabled = true
        binding.btnGoogleSignIn.isEnabled = true
        binding.tvError.visibility = View.GONE
    }

    private fun renderLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnLogin.isEnabled = false
        binding.btnGoogleSignIn.isEnabled = false
        binding.tvError.visibility = View.GONE
    }

    private fun renderError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.btnLogin.isEnabled = true
        binding.btnGoogleSignIn.isEnabled = true
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
