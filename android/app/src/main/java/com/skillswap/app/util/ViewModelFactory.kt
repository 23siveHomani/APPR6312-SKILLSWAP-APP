package com.skillswap.app.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/** Tiny generic factory so ViewModels can take constructor parameters without Hilt. */
class ViewModelFactory(private val creator: () -> ViewModel) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = creator() as T
}
