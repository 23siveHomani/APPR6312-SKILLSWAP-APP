package com.skillswap.app.ui.register

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.skillswap.app.databinding.ActivityRegisterBinding
import com.skillswap.app.ui.home.HomeActivity
import com.skillswap.app.util.LocaleHelper
import com.skillswap.app.util.ServiceLocator
import com.skillswap.app.util.ViewModelFactory

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: RegisterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocaleHelper.applySavedLocale(this)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(
            this,
            ViewModelFactory { RegisterViewModel(ServiceLocator.authRepository(this)) }
        )[RegisterViewModel::class.java]

        binding.btnRegister.setOnClickListener {
            viewModel.register(
                binding.etName.text.toString(),
                binding.etEmail.text.toString(),
                binding.etPassword.text.toString(),
                binding.etConfirmPassword.text.toString()
            )
        }

        viewModel.uiState.observe(this) { state ->
            when (state) {
                is RegisterUiState.Idle -> setLoading(false)
                is RegisterUiState.Loading -> setLoading(true)
                is RegisterUiState.Success -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
                is RegisterUiState.Error -> {
                    setLoading(false)
                    binding.tvError.text = state.message
                    binding.tvError.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !loading
        if (loading) binding.tvError.visibility = View.GONE
    }
}
