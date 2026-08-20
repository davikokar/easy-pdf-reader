package com.example.easy_pdf_reader

import android.content.Context
import androidx.appcompat.app.AppCompatActivity

/**
 * Base activity that keeps the in-app language override applied consistently.
 *
 * Each activity gets [LocaleHelper.wrap] applied in [attachBaseContext], but that alone only
 * affects the activity being created: other already-running activities on the back stack (e.g.
 * MainActivity while SettingsActivity is on top) keep the resources context they were created
 * with. [onResume] detects a mismatch against the currently persisted language and recreates the
 * activity so every screen and menu picks up the new language, not just the one where it was changed.
 */
abstract class LocaleAwareActivity : AppCompatActivity() {

    private var appliedLanguageTag: String = ""

    override fun attachBaseContext(newBase: Context) {
        appliedLanguageTag = LocaleHelper.getPersistedLanguageTag(newBase)
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }

    override fun onResume() {
        super.onResume()
        if (LocaleHelper.getPersistedLanguageTag(this) != appliedLanguageTag) {
            recreate()
        }
    }
}
