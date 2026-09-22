package com.skillswap.app.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.skillswap.app.databinding.ActivityHomeBinding
import com.skillswap.app.ui.login.LoginActivity
import com.skillswap.app.ui.settings.SettingsActivity
import com.skillswap.app.util.ServiceLocator
import com.skillswap.app.util.ViewModelFactory

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var viewModel: HomeViewModel
    private val adapter = ListingAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        binding.toolbar.setOnMenuItemClickListener { handleMenuClick(it) }

        binding.rvListings.layoutManager = LinearLayoutManager(this)
        binding.rvListings.adapter = adapter

        viewModel = ViewModelProvider(
            this,
            ViewModelFactory { HomeViewModel(ServiceLocator.listingRepository(this)) }
        )[HomeViewModel::class.java]

        binding.swipeRefresh.setOnRefreshListener { viewModel.loadListings() }

        viewModel.uiState.observe(this) { state ->
            binding.swipeRefresh.isRefreshing = state.isLoading
            binding.tvOfflineBanner.visibility = if (state.isFromCache) View.VISIBLE else View.GONE
            binding.tvEmpty.visibility = if (!state.isLoading && state.listings.isEmpty()) View.VISIBLE else View.GONE
            adapter.submitList(state.listings)

            state.error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
            }
        }

        viewModel.loadListings()
    }

    private fun handleMenuClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            com.skillswap.app.R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            com.skillswap.app.R.id.action_logout -> {
                ServiceLocator.authRepository(this).logout()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }
            else -> false
        }
    }
}
