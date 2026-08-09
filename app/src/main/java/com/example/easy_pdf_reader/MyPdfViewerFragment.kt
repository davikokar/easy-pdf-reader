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
    private var isViewReady = false

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
        
        isViewReady = true
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

    override fun onDestroyView() {
        super.onDestroyView()
        internalPdfView = null
        isViewReady = false
    }

    fun goToPage(pageNumber: Int) {
        val view = internalPdfView
        if (isViewReady && view != null) {
            try {
                view.scrollToPage(pageNumber)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to scroll to page $pageNumber", e)
            }
        } else {
            Log.w(TAG, "Attempted to scroll before PdfView was ready")
        }
    }

    companion object {
        private const val TAG = "MyPdfViewerFragment"
    }
}
