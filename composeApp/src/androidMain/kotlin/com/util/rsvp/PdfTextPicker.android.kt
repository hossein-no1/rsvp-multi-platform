package com.util.rsvp

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.util.rsvp.pdf.extractTextFromPdfUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
actual fun rememberPdfTextPicker(
    onResult: (String) -> Unit,
): PdfTextPicker? {
    val context = LocalContext.current
    // pdfbox-android needs one-time init.
    remember(context) { PDFBoxResourceLoader.init(context) }
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            scope.launch {
                val text = withContext(Dispatchers.IO) { context.extractTextFromPdfUri(uri) }
                if (!text.isNullOrBlank()) onResult(text)
            }
        },
    )

    return remember(launcher) {
        object : PdfTextPicker {
            override fun launch() {
                launcher.launch(arrayOf("application/pdf"))
            }
        }
    }
}

@Composable
actual fun rememberPdfTextDropListener(
    onResult: (String) -> Unit,
): Boolean = false

