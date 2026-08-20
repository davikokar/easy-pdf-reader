package com.example.easy_pdf_reader

import android.content.Intent
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.easy_pdf_reader.databinding.ActivitySettingsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private var billingHelper: BillingHelper? = null

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        billingHelper = BillingHelper(this)

        binding.itemChangeLanguage.setOnClickListener { showLanguageDialog() }
        binding.itemShareApp.setOnClickListener { shareApp() }
        binding.itemRateApp.setOnClickListener { rateApp() }
        binding.itemCustomerSupport.setOnClickListener { contactSupport() }
        binding.itemAbout.setOnClickListener { showAboutDialog() }
        binding.itemBuyCoffee.setOnClickListener { billingHelper?.buyCoffee() }
        binding.itemPrivacy.setOnClickListener {
            openUrl("https://davikokar.github.io/android-docs/easy-pdf-reader/privacy.html")
        }
        binding.itemTerms.setOnClickListener {
            openUrl("https://davikokar.github.io/android-docs/easy-pdf-reader/terms.html")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        billingHelper?.endConnection()
    }

    private fun showLanguageDialog() {
        val currentTag = LocaleHelper.getPersistedLanguageTag(this)
        // "" represents "follow the system language".
        val tags = listOf("") + LocaleHelper.supportedLanguageTags
        val labels = tags.map { tag ->
            if (tag.isEmpty()) {
                getString(R.string.language_system_default)
            } else {
                val locale = Locale.forLanguageTag(tag)
                locale.getDisplayName(locale).replaceFirstChar { it.uppercase(locale) }
            }
        }.toTypedArray()
        var selectedIndex = tags.indexOf(currentTag).coerceAtLeast(0)

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.settings_change_language)
            .setSingleChoiceItems(labels, selectedIndex) { _, which -> selectedIndex = which }
            .setPositiveButton(R.string.action_ok) { _, _ ->
                val tag = tags[selectedIndex]
                if (tag != currentTag) {
                    LocaleHelper.persistLanguageTag(this, tag)
                    recreate()
                }
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    private fun showAboutDialog() {
        val versionName = try {
            packageManager.getPackageInfo(packageName, 0).versionName ?: ""
        } catch (e: PackageManager.NameNotFoundException) {
            ""
        }
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.settings_about)
            .setMessage(getString(R.string.app_name) + "\n" + getString(R.string.about_version, versionName))
            .setPositiveButton(R.string.action_ok, null)
            .show()
    }

    private fun shareApp() {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app_text, packageName))
            type = "text/plain"
        }
        startActivity(Intent.createChooser(sendIntent, null))
    }

    private fun rateApp() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
        try {
            startActivity(intent)
        } catch (e: Exception) {
            val webIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            )
            startActivity(webIntent)
        }
    }

    private fun contactSupport() {
        val email = getString(R.string.support_email_address)
        val subject = getString(R.string.support_email_subject)
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email?subject=${Uri.encode(subject)}")
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, R.string.error_no_email_app, Toast.LENGTH_SHORT).show()
        }
    }

    private fun openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}
