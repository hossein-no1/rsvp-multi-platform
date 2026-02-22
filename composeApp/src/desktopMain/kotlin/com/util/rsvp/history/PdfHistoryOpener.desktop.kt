package com.util.rsvp.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.util.rsvp.model.PdfHistoryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import java.io.File

@Composable
actual fun rememberPdfHistoryOpener(): PdfHistoryOpener =
    remember { DesktopPdfHistoryOpener() }

private class DesktopPdfHistoryOpener : PdfHistoryOpener {
    override suspend fun exists(item: PdfHistoryItem): Boolean = withContext(Dispatchers.IO) {
        val path = item.uri ?: return@withContext false
        runCatching { File(path).exists() && File(path).isFile }.getOrDefault(false)
    }

    override suspend fun openText(item: PdfHistoryItem): String? = withContext(Dispatchers.IO) {
        val path = item.uri ?: return@withContext null
        val file = File(path)
        if (!file.exists() || !file.isFile) return@withContext null
        runCatching {
            PDDocument.load(file).use { doc ->
                PDFTextStripper().getText(doc)
            }
        }.getOrNull()
    }
}

