package com.skillswap.app.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.skillswap.app.data.model.User
import com.skillswap.app.databinding.ActivitySettingsBinding
import com.skillswap.app.ui.login.LoginActivity
import com.skillswap.app.util.ServiceLocator
import com.skillswap.app.util.ViewModelFactory
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var viewModel: SettingsViewModel
    private var suppressLanguageCallback = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        val authRepository = ServiceLocator.authRepository(this)
        viewModel = ViewModelProvider(
            this,
            ViewModelFactory { SettingsViewModel(authRepository) }
        )[SettingsViewModel::class.java]

        // Fetch the current profile fresh from the API so the switches reflect
        // whatever was last saved (possibly from a different device/session).
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
            viewModel.updateLanguage(tag)
        }

        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            if (!suppressLanguageCallback) viewModel.updateNotifications(isChecked)
        }

        binding.switchSyncWifiOnly.setOnCheckedChangeListener { _, isChecked ->
            if (!suppressLanguageCallback) viewModel.updateSyncOnWifiOnly(isChecked)
        }

        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            startActivity(Intent(this, LoginActivity::class.java))
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
}
