package com.example.easy_pdf_reader

import android.graphics.Color
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.SystemBarStyle
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

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
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setSupportActionBar(binding.toolbar)

        ViewCompat.setOnApplyWindowInsetsListener(binding.mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.toolbar.updatePadding(top = systemBars.top)
            // Pad the container bottom to ensure the PDF viewer's bottom buttons (e.g. edit FAB)
            // are not obscured by the system navigation bar.
            binding.fragmentContainerView.updatePadding(bottom = systemBars.bottom)
            insets
        }

        binding.emptyStateText.setOnClickListener {
            openDocumentLauncher.launch(arrayOf("application/pdf"))
        }

        if (savedInstanceState == null) {
            handleIntent(intent)
        } else {
            currentDocumentUri = savedInstanceState.getParcelable(KEY_DOCUMENT_URI)
            isDocumentUsable = savedInstanceState.getBoolean(KEY_IS_USABLE)
            pageCount = savedInstanceState.getInt(KEY_PAGE_COUNT)
            pdfViewerFragment = supportFragmentManager.findFragmentByTag(PDF_FRAGMENT_TAG) as? MyPdfViewerFragment
            setupFragmentCallbacks()
            updateUIState()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_VIEW) {
            val uri = intent.data
            val mimeType = intent.type ?: contentResolver.getType(uri ?: Uri.EMPTY)
            
            if (uri != null && (mimeType == "application/pdf" || uri.path?.lowercase()?.endsWith(".pdf") == true)) {
                openPdf(uri)
            } else if (uri != null) {
                Toast.makeText(this, R.string.error_invalid_uri, Toast.LENGTH_SHORT).show()
            }
        } else {
            updateUIState()
        }
    }

    private fun openPdf(uri: Uri) {
        showLoading(true)
        currentDocumentUri = uri
        isDocumentUsable = false
        invalidateOptionsMenu()

        try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (e: SecurityException) {
            // Not persistable, proceed with temporary permission
        }

        if (pdfViewerFragment == null) {
            pdfViewerFragment = MyPdfViewerFragment()
            setupFragmentCallbacks()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, pdfViewerFragment!!, PDF_FRAGMENT_TAG)
                .commitNow()
        }

        pdfViewerFragment?.documentUri = uri
        updateUIState()
    }

    private fun closePdf() {
        currentDocumentUri = null
        isDocumentUsable = false
        pageCount = 0
        pdfViewerFragment?.let {
            supportFragmentManager.beginTransaction().remove(it).commitNow()
            pdfViewerFragment = null
        }
        updateUIState()
        invalidateOptionsMenu()
    }

    @OptIn(ExperimentalPdfApi::class)
    private fun setupFragmentCallbacks() {
        pdfViewerFragment?.onLoadSuccess = { count ->
            showLoading(false)
            isDocumentUsable = true
            pageCount = count
            updateUIState()
            invalidateOptionsMenu()
        }
        pdfViewerFragment?.onLoadError = {
            showLoading(false)
            isDocumentUsable = false
            updateUIState()
            invalidateOptionsMenu()
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.error_title)
                .setMessage(R.string.error_loading_pdf)
                .setPositiveButton(R.string.action_ok, null)
                .show()
        }
    }

    private fun showLoading(loading: Boolean) {
        binding.loadingIndicator.visibility = if (loading) View.VISIBLE else View.GONE
        if (loading) {
            binding.emptyStateText.visibility = View.GONE
        }
    }

    private fun updateUIState() {
        val hasDocument = currentDocumentUri != null
        val loading = binding.loadingIndicator.visibility == View.VISIBLE
        
        binding.emptyStateText.visibility = if (hasDocument || loading) View.GONE else View.VISIBLE
        binding.fragmentContainerView.visibility = if (hasDocument && !loading) View.VISIBLE else View.GONE
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val openItem = menu.findItem(R.id.action_open)
        if (currentDocumentUri != null) {
            openItem.title = getString(R.string.menu_close)
        } else {
            openItem.title = getString(R.string.menu_open)
        }

        menu.findItem(R.id.action_go_to_page)?.isEnabled = isDocumentUsable
        menu.findItem(R.id.action_search)?.isEnabled = isDocumentUsable
        menu.findItem(R.id.action_share)?.isEnabled = isDocumentUsable
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_open -> {
                if (currentDocumentUri != null) {
                    closePdf()
                } else {
                    openDocumentLauncher.launch(arrayOf("application/pdf"))
                }
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
        val context = this
        val layout = FrameLayout(context)
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val margin = (24 * resources.displayMetrics.density).toInt()
        params.setMargins(margin, (8 * resources.displayMetrics.density).toInt(), margin, 0)
        
        val textInputLayout = TextInputLayout(context)
        textInputLayout.layoutParams = params
        textInputLayout.hint = getString(R.string.menu_go_to_page)
        textInputLayout.helperText = "1 - $pageCount"
        
        val editText = TextInputEditText(context)
        editText.inputType = InputType.TYPE_CLASS_NUMBER
        textInputLayout.addView(editText)
        layout.addView(textInputLayout)

        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle(R.string.menu_go_to_page)
            .setView(layout)
            .setPositiveButton(R.string.action_go, null) // Set null to override listener for validation
            .setNegativeButton(R.string.action_cancel, null)
            .create()

        dialog.show()

        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val pageStr = editText.text.toString()
            if (pageStr.isBlank()) {
                textInputLayout.error = getString(R.string.error_invalid_page, pageCount)
            } else {
                val pageNum = pageStr.toIntOrNull()
                if (pageNum != null && pageNum in 1..pageCount) {
                    pdfViewerFragment?.goToPage(pageNum - 1)
                    dialog.dismiss()
                } else {
                    textInputLayout.error = getString(R.string.error_invalid_page, pageCount)
                }
            }
        }
    }

    private fun sharePdf() {
        val uri = currentDocumentUri ?: return
        
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newRawUri(null, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            val chooser = Intent.createChooser(shareIntent, getString(R.string.share_title))
            startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(this, R.string.error_no_share_target, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(KEY_DOCUMENT_URI, currentDocumentUri)
        outState.putBoolean(KEY_IS_USABLE, isDocumentUsable)
        outState.putInt(KEY_PAGE_COUNT, pageCount)
    }

    companion object {
        private const val PDF_FRAGMENT_TAG = "PDF_VIEWER"
        private const val KEY_DOCUMENT_URI = "key_document_uri"
        private const val KEY_IS_USABLE = "key_is_usable"
        private const val KEY_PAGE_COUNT = "key_page_count"
    }
}
