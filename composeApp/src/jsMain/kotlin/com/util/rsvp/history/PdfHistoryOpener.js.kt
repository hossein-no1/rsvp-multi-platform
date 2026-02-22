package com.util.rsvp.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.util.rsvp.model.PdfHistoryItem

@Composable
actual fun rememberPdfHistoryOpener(): PdfHistoryOpener =
    remember { JsPdfHistoryOpener() }

private class JsPdfHistoryOpener : PdfHistoryOpener {
    override suspend fun exists(item: PdfHistoryItem): Boolean = false
    override suspend fun openText(item: PdfHistoryItem): String? = null
}

