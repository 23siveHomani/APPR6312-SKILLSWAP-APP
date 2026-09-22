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
    val error: String? = null
)

class HomeViewModel(private val listingRepository: ListingRepository) : ViewModel() {

    private val _uiState = MutableLiveData(HomeUiState())
    val uiState: LiveData<HomeUiState> = _uiState

    fun loadListings(category: String? = null) {
        _uiState.value = _uiState.value?.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val (listings, fromCache) = listingRepository.getListings(category)
                _uiState.value = HomeUiState(
                    listings = listings,
                    isLoading = false,
                    isFromCache = fromCache,
                    error = if (listings.isEmpty() && fromCache) "No cached listings available offline" else null
                )
            } catch (e: Exception) {
                _uiState.value = HomeUiState(isLoading = false, error = e.message ?: "Failed to load listings")
            }
        }
    }
}
