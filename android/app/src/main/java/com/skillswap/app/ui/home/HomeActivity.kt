package com.skillswap.app.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
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

/**
 * Main dashboard screen where users browse available skill listings,
 * create new skill listings, view offline indicators, and access Settings & Logout.
 */
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var viewModel: HomeViewModel
    private val adapter = ListingAdapter()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        Log.i(TAG, "Notification permission result: isGranted = $isGranted")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocaleHelper.applySavedLocale(this)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        Log.i(TAG, "HomeActivity created. Initializing UI components...")
        checkNotificationPermission()

        binding.rvListings.layoutManager = LinearLayoutManager(this)
        binding.rvListings.adapter = adapter

        viewModel = ViewModelProvider(
            this,
            ViewModelFactory { HomeViewModel(ServiceLocator.listingRepository(this)) }
        )[HomeViewModel::class.java]

        binding.swipeRefresh.setOnRefreshListener {
            Log.d(TAG, "Swipe to refresh triggered by user.")
            viewModel.loadListings()
        }

        binding.fabAddListing.setOnClickListener {
            Log.d(TAG, "Add Listing FAB clicked. Showing creation dialog...")
            showAddListingDialog()
        }

        viewModel.uiState.observe(this) { state ->
            Log.d(TAG, "Observed UI State update: listingsCount=${state.listings.size}, isFromCache=${state.isFromCache}, pendingCount=${state.pendingSyncCount}")
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
                Log.w(TAG, "Displaying error snackbar: $it")
                Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
            }
        }

        viewModel.loadListings()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return handleMenuClick(item) || super.onOptionsItemSelected(item)
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Requesting POST_NOTIFICATIONS runtime permission...")
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                Log.d(TAG, "POST_NOTIFICATIONS permission already granted.")
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
                Log.i(TAG, "Submitting new skill listing: title='$title', category='$category'")
                viewModel.createListing(title, category, description, imageUrl)
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                Log.d(TAG, "Add listing dialog cancelled.")
            }
            .show()
    }

    private fun handleMenuClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                Log.i(TAG, "Navigating to SettingsActivity.")
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_logout -> {
                Log.i(TAG, "Logging out user and clearing secure session...")
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

    companion object {
        private const val TAG = "HomeActivity"
    }
}
