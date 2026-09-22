package com.skillswap.app.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skillswap.app.data.model.SettingsUpdateRequest
import com.skillswap.app.data.model.User
import com.skillswap.app.data.repository.ApiResult
import com.skillswap.app.data.repository.AuthRepository
import kotlinx.coroutines.launch

sealed class SettingsUiState {
    object Loading : SettingsUiState()
    data class Loaded(val user: User) : SettingsUiState()
    data class Saved(val user: User) : SettingsUiState()
    data class Error(val message: String) : SettingsUiState()
}

/**
 * Backs the Profile & Settings screen (Part 1 section 4.5): language,
 * notification preferences and sync behaviour. Every change is pushed
 * straight to PATCH /users/me so the server-side profile stays authoritative.
 */
class SettingsViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableLiveData<SettingsUiState>()
    val uiState: LiveData<SettingsUiState> = _uiState

    fun loadCurrentUser(user: User) {
        _uiState.value = SettingsUiState.Loaded(user)
    }

    fun updateLanguage(languagePref: String) = save(SettingsUpdateRequest(languagePref = languagePref))

    fun updateNotifications(enabled: Boolean) = save(SettingsUpdateRequest(notificationsEnabled = enabled))

    fun updateSyncOnWifiOnly(wifiOnly: Boolean) = save(SettingsUpdateRequest(syncOnWifiOnly = wifiOnly))

    private fun save(update: SettingsUpdateRequest) {
        viewModelScope.launch {
            when (val result = authRepository.updateSettings(update)) {
                is ApiResult.Success -> _uiState.value = SettingsUiState.Saved(result.data)
                is ApiResult.Failure -> _uiState.value = SettingsUiState.Error(result.message)
            }
        }
    }

    fun logout() = authRepository.logout()
}
