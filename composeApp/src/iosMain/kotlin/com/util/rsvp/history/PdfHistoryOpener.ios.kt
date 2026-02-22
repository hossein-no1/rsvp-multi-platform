package com.util.rsvp.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.util.rsvp.model.PdfHistoryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSURL
import platform.PDFKit.PDFDocument

@Composable
actual fun rememberPdfHistoryOpener(): PdfHistoryOpener =
    remember { IosPdfHistoryOpener() }

private class IosPdfHistoryOpener : PdfHistoryOpener {
    override suspend fun exists(item: PdfHistoryItem): Boolean = withContext(Dispatchers.Default) {
        val raw = item.uri ?: return@withContext false
        val url = NSURL(string = raw) ?: return@withContext false
        runCatching { PDFDocument(uRL = url) != null }.getOrDefault(false)
    }

    override suspend fun openText(item: PdfHistoryItem): String? = withContext(Dispatchers.Default) {
        val raw = item.uri ?: return@withContext null
        val url = NSURL(string = raw) ?: return@withContext null
        runCatching { PDFDocument(uRL = url)?.string }.getOrNull()
    }
}

