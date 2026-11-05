package com.skillswap.app.ui.settings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.skillswap.app.data.model.User
import com.skillswap.app.databinding.ActivitySettingsBinding
import com.skillswap.app.ui.login.LoginActivity
import com.skillswap.app.util.LocaleHelper
import com.skillswap.app.util.ServiceLocator
import com.skillswap.app.util.SkillSwapMessagingService
import com.skillswap.app.util.ViewModelFactory
import kotlinx.coroutines.launch

/**
 * SettingsActivity manages user preferences (App language, Push Notification toggles,
 * Sync preferences, and testing Firebase Push Notifications).
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var viewModel: SettingsViewModel
    private var suppressLanguageCallback = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocaleHelper.applySavedLocale(this)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        val authRepository = ServiceLocator.authRepository(this)
        viewModel = ViewModelProvider(
            this,
            ViewModelFactory { SettingsViewModel(authRepository) }
        )[SettingsViewModel::class.java]

        lifecycleScope.launch {
            val api = ServiceLocator.apiService(this@SettingsActivity)
            val response = api.getMe()
            if (response.isSuccessful && response.body() != null) {
                viewModel.loadCurrentUser(response.body()!!.user)
            }
        }

        binding.rgLanguage.setOnCheckedChangeListener { group, checkedId ->
            if (suppressLanguageCallback) return@setOnCheckedChangeListener
            val tag = group.findViewById<View>(checkedId)?.tag as? String ?: return@setOnCheckedChangeListener
            Log.i(TAG, "Language changed to '$tag'. Applying locale and updating profile...")
            LocaleHelper.saveLanguage(this, tag)
            viewModel.updateLanguage(tag)
        }

        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            if (!suppressLanguageCallback) {
                Log.i(TAG, "Notification preference updated: enabled = $isChecked")
                viewModel.updateNotifications(isChecked)
            }
        }

        binding.btnTestNotification.setOnClickListener {
            val title = "SkillSwap Push Alert"
            val body = "You have a new skill swap request!"
            Log.i(TAG, "Test push notification button tapped. Triggering FCM system alert...")
            SkillSwapMessagingService.displaySystemNotification(this, title, body)
            lifecycleScope.launch {
                try {
                    val api = ServiceLocator.apiService(this@SettingsActivity)
                    api.sendTestNotification(mapOf("title" to title, "body" to body))
                } catch (e: Exception) {
                    Log.w(TAG, "Backend test-notification request failed (local notification displayed): ${e.message}")
                }
            }
            showStatus("Push notification triggered!")
        }

        binding.switchSyncWifiOnly.setOnCheckedChangeListener { _, isChecked ->
            if (!suppressLanguageCallback) {
                Log.i(TAG, "Sync Wi-Fi preference updated: wifiOnly = $isChecked")
                viewModel.updateSyncOnWifiOnly(isChecked)
            }
        }

        binding.btnLogout.setOnClickListener {
            Log.i(TAG, "User logged out. Redirecting to LoginActivity...")
            viewModel.logout()
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }

        viewModel.uiState.observe(this) { state ->
            when (state) {
                is SettingsUiState.Loading -> {}
                is SettingsUiState.Loaded -> bindUser(state.user)
                is SettingsUiState.Saved -> {
                    bindUser(state.user)
                    showStatus("Settings saved")
                }
                is SettingsUiState.Error -> showStatus(state.message, isError = true)
            }
        }
    }

    private fun bindUser(user: User) {
        suppressLanguageCallback = true
        when (user.languagePref) {
            "zu" -> binding.rbZulu.isChecked = true
            "af" -> binding.rbAfrikaans.isChecked = true
            else -> binding.rbEnglish.isChecked = true
        }
        binding.switchNotifications.isChecked = user.notificationsEnabled
        binding.switchSyncWifiOnly.isChecked = user.syncOnWifiOnly
        suppressLanguageCallback = false
    }

    private fun showStatus(message: String, isError: Boolean = false) {
        binding.tvStatus.text = message
        binding.tvStatus.setTextColor(if (isError) 0xFFD32F2F.toInt() else 0xFF388E3C.toInt())
        binding.tvStatus.visibility = View.VISIBLE
    }

    companion object {
        private const val TAG = "SettingsActivity"
    }
}
