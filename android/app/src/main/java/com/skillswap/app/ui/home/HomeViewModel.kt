package com.skillswap.app.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skillswap.app.data.model.SkillListing
import com.skillswap.app.data.repository.ListingRepository
import kotlinx.coroutines.launch

data class HomeUiState(
    val listings: List<SkillListing> = emptyList(),
    val isLoading: Boolean = false,
    val isFromCache: Boolean = false,
    val pendingSyncCount: Int = 0,
    val userMessage: String? = null,
    val error: String? = null
)

class HomeViewModel(private val listingRepository: ListingRepository) : ViewModel() {

    private val _uiState = MutableLiveData(HomeUiState())
    val uiState: LiveData<HomeUiState> = _uiState

    fun loadListings(category: String? = null) {
        _uiState.value = _uiState.value?.copy(isLoading = true, error = null, userMessage = null)
        viewModelScope.launch {
            try {
                val (listings, fromCache) = listingRepository.getListings(category)
                val pendingCount = listingRepository.getPendingSyncCount()
                _uiState.value = HomeUiState(
                    listings = listings,
                    isLoading = false,
                    isFromCache = fromCache,
                    pendingSyncCount = pendingCount,
                    error = if (listings.isEmpty() && fromCache) "No cached listings available offline" else null
                )
            } catch (e: Exception) {
                _uiState.value = HomeUiState(isLoading = false, error = e.message ?: "Failed to load listings")
            }
        }
    }

    fun createListing(title: String, category: String, description: String?, imageUrl: String?) {
        if (title.isBlank() || category.isBlank()) {
            _uiState.value = _uiState.value?.copy(error = "Title and category are required")
            return
        }

        _uiState.value = _uiState.value?.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val result = listingRepository.createListing(title.trim(), category.trim(), description?.trim(), imageUrl?.trim())
            if (result.isSuccess) {
                _uiState.value = _uiState.value?.copy(userMessage = "LISTING_CREATED")
                loadListings()
            } else {
                val isOffline = result.exceptionOrNull()?.message == "OFFLINE_SAVED"
                val pendingCount = listingRepository.getPendingSyncCount()
                _uiState.value = _uiState.value?.copy(
                    isLoading = false,
                    pendingSyncCount = pendingCount,
                    userMessage = if (isOffline) "LISTING_SAVED_OFFLINE" else null,
                    error = if (!isOffline) result.exceptionOrNull()?.message else null
                )
                if (isOffline) loadListings()
            }
        }
    }
}
