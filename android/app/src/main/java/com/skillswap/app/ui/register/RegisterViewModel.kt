package com.skillswap.app.ui.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skillswap.app.data.model.User
import com.skillswap.app.data.repository.ApiResult
import com.skillswap.app.data.repository.AuthRepository
import kotlinx.coroutines.launch

sealed class RegisterUiState {
    object Idle : RegisterUiState()
    object Loading : RegisterUiState()
    data class Success(val user: User) : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
}

class RegisterViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableLiveData<RegisterUiState>(RegisterUiState.Idle)
    val uiState: LiveData<RegisterUiState> = _uiState

    fun register(name: String, email: String, password: String, confirmPassword: String) {
        val error = validate(name, email, password, confirmPassword)
        if (error != null) {
            _uiState.value = RegisterUiState.Error(error)
            return
        }

        _uiState.value = RegisterUiState.Loading
        viewModelScope.launch {
            when (val result = authRepository.register(name.trim(), email.trim(), password)) {
                is ApiResult.Success -> _uiState.value = RegisterUiState.Success(result.data)
                is ApiResult.Failure -> _uiState.value = RegisterUiState.Error(result.message)
            }
        }
    }

    fun validate(name: String, email: String, password: String, confirmPassword: String): String? {
        if (name.isBlank()) return "Enter your name"
        if (!EMAIL_REGEX.matches(email.trim())) return "Enter a valid email address"
        // Matches the server-side minimum in auth.js so the user sees the
        // problem immediately instead of round-tripping to the API first.
        if (password.length < 8) return "Password must be at least 8 characters"
        if (password != confirmPassword) return "Passwords do not match"
        return null
    }

    companion object {
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }
}
