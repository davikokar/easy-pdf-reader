package com.example.easy_pdf_reader

import android.app.Application
import android.content.Context
import android.os.Process

class EasyPdfReaderApp : Application() {
    override fun attachBaseContext(base: Context) {
        // Isolated sandboxed processes (e.g. androidx.pdf's PdfDocumentServiceImpl) have no
        // UserManager, so touching SharedPreferences here would crash them on startup.
        if (Process.isIsolated()) {
            super.attachBaseContext(base)
            return
        }
        super.attachBaseContext(LocaleHelper.wrap(base))
    }
}
