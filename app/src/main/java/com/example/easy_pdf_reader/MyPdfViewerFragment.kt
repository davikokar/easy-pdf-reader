package com.example.easy_pdf_reader

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.OptIn
import androidx.pdf.viewer.fragment.PdfViewerFragment
import androidx.pdf.PdfDocument
import androidx.pdf.ExperimentalPdfApi
import androidx.pdf.view.PdfView

@OptIn(ExperimentalPdfApi::class)
class MyPdfViewerFragment : PdfViewerFragment() {

    private var internalPdfView: PdfView? = null

    var onLoadSuccess: ((pageCount: Int) -> Unit)? = null
    var onLoadError: ((Throwable) -> Unit)? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "PdfView created")
    }

    @ExperimentalPdfApi
    override fun onPdfViewCreated(pdfView: PdfView) {
        super.onPdfViewCreated(pdfView)
        internalPdfView = pdfView
        
        // Configure PdfView per requirements
        pdfView.pagesPerRow = 1
        
        // 12dp spacing
        val spacingPx = (12 * resources.displayMetrics.density).toInt()
        pdfView.verticalPageSpacing = spacingPx
        
        Log.d(TAG, "PdfView configured: pagesPerRow=1, verticalPageSpacing=$spacingPx")
    }

    override fun onLoadDocumentSuccess(document: PdfDocument) {
        super.onLoadDocumentSuccess(document)
        val pageCount = document.pageCount
        Log.d(TAG, "Document loaded successfully: $pageCount pages")
        onLoadSuccess?.invoke(pageCount)
    }

    override fun onLoadDocumentError(error: Throwable) {
        super.onLoadDocumentError(error)
        Log.e(TAG, "Error loading document", error)
        onLoadError?.invoke(error)
    }

    fun goToPage(pageNumber: Int) {
        internalPdfView?.scrollToPage(pageNumber)
    }

    companion object {
        private const val TAG = "MyPdfViewerFragment"
    }
}
