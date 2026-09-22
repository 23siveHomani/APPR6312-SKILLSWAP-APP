package com.skillswap.app.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skillswap.app.data.model.User
import com.skillswap.app.data.repository.ApiResult
import com.skillswap.app.data.repository.AuthRepository
import kotlinx.coroutines.launch

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val user: User) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableLiveData<LoginUiState>(LoginUiState.Idle)
    val uiState: LiveData<LoginUiState> = _uiState

    fun login(email: String, password: String) {
        val validationError = validate(email, password)
        if (validationError != null) {
            _uiState.value = LoginUiState.Error(validationError)
            return
        }

        _uiState.value = LoginUiState.Loading
        viewModelScope.launch {
            when (val result = authRepository.login(email.trim(), password)) {
                is ApiResult.Success -> _uiState.value = LoginUiState.Success(result.data)
                is ApiResult.Failure -> _uiState.value = LoginUiState.Error(result.message)
            }
        }
    }

    /**
     * Exposed as a standalone function so it can be unit tested with plain JUnit
     * (no Robolectric/instrumentation needed). Uses a simple regex rather than
     * android.util.Patterns, which is unavailable outside the Android runtime.
     */
    fun validate(email: String, password: String): String? {
        if (email.isBlank() || !EMAIL_REGEX.matches(email.trim())) {
            return "Enter a valid email address"
        }
        if (password.length < 6) {
            return "Password must be at least 6 characters"
        }
        return null
    }

    companion object {
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }
}
