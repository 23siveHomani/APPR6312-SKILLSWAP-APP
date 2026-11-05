package com.skillswap.app.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.skillswap.app.R
import com.skillswap.app.databinding.ActivityHomeBinding
import com.skillswap.app.databinding.DialogAddListingBinding
import com.skillswap.app.ui.login.LoginActivity
import com.skillswap.app.ui.settings.SettingsActivity
import com.skillswap.app.util.LocaleHelper
import com.skillswap.app.util.ServiceLocator
import com.skillswap.app.util.ViewModelFactory

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var viewModel: HomeViewModel
    private val adapter = ListingAdapter()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Notification permission callback
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocaleHelper.applySavedLocale(this)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        checkNotificationPermission()

        binding.toolbar.setOnMenuItemClickListener { handleMenuClick(it) }

        binding.rvListings.layoutManager = LinearLayoutManager(this)
        binding.rvListings.adapter = adapter

        viewModel = ViewModelProvider(
            this,
            ViewModelFactory { HomeViewModel(ServiceLocator.listingRepository(this)) }
        )[HomeViewModel::class.java]

        binding.swipeRefresh.setOnRefreshListener { viewModel.loadListings() }

        binding.fabAddListing.setOnClickListener { showAddListingDialog() }

        viewModel.uiState.observe(this) { state ->
            binding.swipeRefresh.isRefreshing = state.isLoading
            binding.tvOfflineBanner.visibility = if (state.isFromCache) View.VISIBLE else View.GONE
            binding.tvPendingSyncBanner.visibility = if (state.pendingSyncCount > 0) View.VISIBLE else View.GONE
            binding.tvEmpty.visibility = if (!state.isLoading && state.listings.isEmpty()) View.VISIBLE else View.GONE
            adapter.submitList(state.listings)

            state.userMessage?.let { msgKey ->
                val messageText = when (msgKey) {
                    "LISTING_CREATED" -> getString(R.string.listing_created)
                    "LISTING_SAVED_OFFLINE" -> getString(R.string.listing_saved_offline)
                    else -> msgKey
                }
                Snackbar.make(binding.root, messageText, Snackbar.LENGTH_LONG).show()
            }

            state.error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
            }
        }

        viewModel.loadListings()
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun showAddListingDialog() {
        val dialogBinding = DialogAddListingBinding.inflate(LayoutInflater.from(this))
        AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.add_button) { _, _ ->
                val title = dialogBinding.etTitle.text.toString()
                val category = dialogBinding.etCategory.text.toString()
                val description = dialogBinding.etDescription.text.toString()
                val imageUrl = dialogBinding.etImageUrl.text.toString()
                viewModel.createListing(title, category, description, imageUrl)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun handleMenuClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_logout -> {
                ServiceLocator.authRepository(this).logout()
                val intent = Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
                true
            }
            else -> false
        }
    }
}
