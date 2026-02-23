package com.util.rsvp.pdf

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.io.MemoryUsageSetting
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.File
import java.io.FileOutputStream

private const val MAX_PDF_BYTES_TO_CACHE: Long = 512L * 1024L * 1024L // 512 MiB safety cap

internal fun Context.extractTextFromPdfUri(uri: Uri): String? {
    val cacheDir = cacheDir ?: return null
    val tmp = File.createTempFile("rsvp_", ".pdf", cacheDir)

    try {
        contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tmp).use { output ->
                copyToWithLimit(input = input, output = output, maxBytes = MAX_PDF_BYTES_TO_CACHE)
            }
        } ?: return null

        return PDDocument
            .load(tmp, MemoryUsageSetting.setupTempFileOnly())
            .use { doc -> PDFTextStripper().getText(doc) }
    } finally {
        runCatching { tmp.delete() }
    }
}

private fun copyToWithLimit(
    input: java.io.InputStream,
    output: java.io.OutputStream,
    maxBytes: Long,
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
) {
    val buffer = ByteArray(bufferSize)
    var total = 0L
    while (true) {
        val read = input.read(buffer)
        if (read <= 0) break
        total += read
        if (total > maxBytes) error("PDF too large to cache ($total bytes).")
        output.write(buffer, 0, read)
    }
}
