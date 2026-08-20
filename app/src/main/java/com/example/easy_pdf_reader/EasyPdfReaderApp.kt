package com.example.easy_pdf_reader

import android.app.Application
import android.content.Context

class EasyPdfReaderApp : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.wrap(base))
    }
}
