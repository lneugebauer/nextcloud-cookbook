package de.lukasneugebauer.nextcloudcookbook.recipe.presentation.share

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.MainActivity
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.state.DownloadRecipeScreenState
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ImportShareActivity : ComponentActivity() {
    private val viewModel: ImportShareViewModel by viewModels()

    private lateinit var progressBar: ProgressBar
    private lateinit var statusText: TextView
    private lateinit var openButton: Button
    private lateinit var retryButton: Button
    private lateinit var closeButton: Button

    private var loadedRecipeId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_import_share)

        progressBar = findViewById(R.id.import_progress)
        statusText = findViewById(R.id.import_status)
        openButton = findViewById(R.id.button_open)
        retryButton = findViewById(R.id.button_retry)
        closeButton = findViewById(R.id.button_close)

        openButton.setOnClickListener {
            loadedRecipeId?.let { id ->
                openRecipeDetail(id)
            }
        }
        retryButton.setOnClickListener { handleIncomingShare() }
        closeButton.setOnClickListener { finish() }

        handleIncomingShare()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is DownloadRecipeScreenState.Initial -> showInitial()
                        is DownloadRecipeScreenState.Loading -> showLoading()
                        is DownloadRecipeScreenState.Loaded -> showSuccess(state.id)
                        is DownloadRecipeScreenState.Error -> showError(uiTextToString(state.uiText))
                    }
                }
            }
        }
    }

    private fun handleIncomingShare() {
        val action = intent?.action
        val type = intent?.type
        if (Intent.ACTION_SEND == action && type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            viewModel.importFromSharedText(sharedText)
        } else {
            showError(getString(R.string.error_invalid_url))
        }
    }

    private fun showInitial() {
        progressBar.visibility = View.GONE
        statusText.text = getString(R.string.import_share_initial)
        openButton.visibility = View.GONE
        retryButton.visibility = View.GONE
        closeButton.visibility = View.VISIBLE
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        statusText.text = getString(R.string.import_share_loading)
        openButton.visibility = View.GONE
        retryButton.visibility = View.GONE
        closeButton.visibility = View.VISIBLE
    }

    private fun showSuccess(id: String) {
        loadedRecipeId = id
        progressBar.visibility = View.GONE
        statusText.text = getString(R.string.import_share_success)
        openButton.visibility = View.VISIBLE
        retryButton.visibility = View.GONE
        closeButton.visibility = View.VISIBLE

        // Optionally auto-open after a short time; for now, keep button based per acceptance criteria
    }

    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        statusText.text = getString(R.string.import_share_error, message)
        openButton.visibility = View.GONE
        retryButton.visibility = View.VISIBLE
        closeButton.visibility = View.VISIBLE
    }

    private fun openRecipeDetail(id: String) {
        val uri = Uri.parse("nccookbook://lneugebauer.github.io/recipe/$id")
        val intent = Intent(Intent.ACTION_VIEW, uri, this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        finish()
    }

    private fun uiTextToString(uiText: UiText): String {
        return when (uiText) {
            is UiText.DynamicString -> uiText.value
            is UiText.StringResource -> getString(uiText.resId, *uiText.args)
        }
    }
}
