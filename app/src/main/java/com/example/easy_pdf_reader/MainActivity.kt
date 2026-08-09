package com.example.easy_pdf_reader

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.pdf.ExperimentalPdfApi
import com.example.easy_pdf_reader.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var pdfViewerFragment: MyPdfViewerFragment? = null

    private val openDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { openPdf(it) }
    }

    private var currentDocumentUri: Uri? = null
    private var isDocumentUsable = false
    private var pageCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // As per PDF viewer documentation for search UI
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setSupportActionBar(binding.toolbar)

        ViewCompat.setOnApplyWindowInsetsListener(binding.mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.toolbar.updatePadding(top = systemBars.top)
            // We don't pad the bottom here because the PDF viewer handles its own insets for scroll/search
            // But we should ensure the container doesn't overlap with navigation bar if needed.
            // Actually, PdfViewerFragment handles insets for its search view.
            insets
        }

        if (savedInstanceState == null) {
            updateEmptyState()
        } else {
            // Restore fragment reference if it exists
            pdfViewerFragment = supportFragmentManager.findFragmentByTag(PDF_FRAGMENT_TAG) as? MyPdfViewerFragment
            setupFragmentCallbacks()
        }
    }

    private fun openPdf(uri: Uri) {
        currentDocumentUri = uri
        
        // Persist permissions for the URI if possible
        try {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (e: Exception) {
            // Might fail if not a content provider or not supported, ignore for now
        }

        if (pdfViewerFragment == null) {
            pdfViewerFragment = MyPdfViewerFragment()
            setupFragmentCallbacks()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, pdfViewerFragment!!, PDF_FRAGMENT_TAG)
                .commit()
        }

        pdfViewerFragment?.documentUri = uri
        updateEmptyState()
    }

    @OptIn(ExperimentalPdfApi::class)
    private fun setupFragmentCallbacks() {
        pdfViewerFragment?.onLoadSuccess = { count ->
            isDocumentUsable = true
            pageCount = count
            updateEmptyState()
            invalidateOptionsMenu()
            Toast.makeText(this, getString(R.string.pdf_loaded_message, count), Toast.LENGTH_SHORT).show()
        }
        pdfViewerFragment?.onLoadError = {
            isDocumentUsable = false
            updateEmptyState()
            invalidateOptionsMenu()
            Toast.makeText(this, R.string.error_loading_pdf, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateEmptyState() {
        val hasDocument = currentDocumentUri != null
        binding.emptyStateText.visibility = if (hasDocument) View.GONE else View.VISIBLE
        binding.fragmentContainerView.visibility = if (hasDocument) View.VISIBLE else View.GONE
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.action_go_to_page)?.isEnabled = isDocumentUsable
        menu.findItem(R.id.action_search)?.isEnabled = isDocumentUsable
        menu.findItem(R.id.action_share)?.isEnabled = isDocumentUsable
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_open -> {
                openDocumentLauncher.launch(arrayOf("application/pdf"))
                true
            }
            R.id.action_search -> {
                pdfViewerFragment?.isTextSearchActive = true
                true
            }
            R.id.action_go_to_page -> {
                showGoToPageDialog()
                true
            }
            R.id.action_share -> {
                sharePdf()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showGoToPageDialog() {
        val editText = android.widget.EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            hint = "1 - $pageCount"
        }
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.menu_go_to_page)
            .setView(editText)
            .setPositiveButton("Go") { _, _ ->
                val pageStr = editText.text.toString()
                if (pageStr.isNotEmpty()) {
                    val pageNum = pageStr.toIntOrNull()
                    if (pageNum != null && pageNum in 1..pageCount) {
                        pdfViewerFragment?.goToPage(pageNum - 1)
                    } else {
                        Toast.makeText(this, "Invalid page number", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun sharePdf() {
        currentDocumentUri?.let { uri ->
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, getString(R.string.menu_share)))
        }
    }

    companion object {
        private const val PDF_FRAGMENT_TAG = "PDF_VIEWER"
    }
}
