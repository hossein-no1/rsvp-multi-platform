package com.util.rsvp.history

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.util.rsvp.pdf.extractTextFromPdfUri
import com.util.rsvp.model.PdfHistoryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
actual fun rememberPdfHistoryOpener(): PdfHistoryOpener {
    val context = LocalContext.current.applicationContext
    remember(context) { PDFBoxResourceLoader.init(context) }
    return remember(context) { AndroidPdfHistoryOpener(context) }
}

private class AndroidPdfHistoryOpener(
    private val context: Context,
) : PdfHistoryOpener {
    override suspend fun exists(item: PdfHistoryItem): Boolean = withContext(Dispatchers.IO) {
        val raw = item.uri ?: return@withContext false
        val uri = runCatching { Uri.parse(raw) }.getOrNull() ?: return@withContext false
        runCatching { context.contentResolver.openInputStream(uri)?.use { true } ?: false }
            .getOrDefault(false)
    }

    override suspend fun openText(item: PdfHistoryItem): String? = withContext(Dispatchers.IO) {
        val raw = item.uri ?: return@withContext null
        val uri = runCatching { Uri.parse(raw) }.getOrNull() ?: return@withContext null
        runCatching {
            context.extractTextFromPdfUri(uri)
        }.getOrNull()
    }
}

