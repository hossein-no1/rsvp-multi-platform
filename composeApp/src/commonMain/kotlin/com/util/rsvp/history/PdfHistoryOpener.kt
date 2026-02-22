package com.util.rsvp.history

import androidx.compose.runtime.Composable
import com.util.rsvp.model.PdfHistoryItem

interface PdfHistoryOpener {
    suspend fun exists(item: PdfHistoryItem): Boolean
    suspend fun openText(item: PdfHistoryItem): String?
}

@Composable
expect fun rememberPdfHistoryOpener(): PdfHistoryOpener

