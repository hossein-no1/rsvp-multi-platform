package com.util.rsvp

import com.tom_roush.pdfbox.io.MemoryUsageSetting
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.PushbackInputStream
import java.net.HttpURLConnection
import java.net.URL

private const val MAX_URL_TEXT_BYTES: Int = 2 * 1024 * 1024 // 2 MiB
private const val MAX_URL_PDF_BYTES: Long = 512L * 1024L * 1024L // 512 MiB safety cap

internal actual suspend fun downloadTextFromUrl(
    url: String,
): String = withContext(Dispatchers.IO) {
    val conn = (URL(url).openConnection() as HttpURLConnection).apply {
        instanceFollowRedirects = true
        connectTimeout = 15_000
        readTimeout = 30_000
    }

    try {
        val contentType = conn.contentType.orEmpty()
        val looksLikePdfByHint = contentType.contains("pdf", ignoreCase = true) ||
            url.substringBefore('?').endsWith(".pdf", ignoreCase = true)

        val contentLength = runCatching { conn.contentLengthLong }.getOrDefault(-1L)
        if (contentLength > MAX_URL_PDF_BYTES) return@withContext ""

        return@withContext conn.inputStream.use { raw ->
            val input = PushbackInputStream(BufferedInputStream(raw), /* size = */ 8)

            val header = ByteArray(4)
            val headerRead = input.read(header)
            if (headerRead > 0) input.unread(header, 0, headerRead)

            val looksLikePdfByMagic = headerRead >= 4 &&
                header[0] == '%'.code.toByte() &&
                header[1] == 'P'.code.toByte() &&
                header[2] == 'D'.code.toByte() &&
                header[3] == 'F'.code.toByte()

            val looksLikePdf = looksLikePdfByHint || looksLikePdfByMagic

            if (!looksLikePdf) {
                return@use readTextUpTo(input, maxBytes = MAX_URL_TEXT_BYTES)
            }

            val tmp = createTempPdfFile()
            try {
                FileOutputStream(tmp).use { out ->
                    copyToWithLimit(input = input, output = out, maxBytes = MAX_URL_PDF_BYTES)
                }

                return@use runCatching {
                    PDDocument
                        .load(tmp, MemoryUsageSetting.setupTempFileOnly())
                        .use { doc -> PDFTextStripper().getText(doc) }
                }.getOrNull().orEmpty()
            } finally {
                runCatching { tmp.delete() }
            }
        }
    } finally {
        conn.disconnect()
    }
}

private fun createTempPdfFile(): File {
    val tmpDirPath = System.getProperty("java.io.tmpdir").orEmpty()
    val tmpDir = if (tmpDirPath.isNotBlank()) File(tmpDirPath) else File(".")
    if (!tmpDir.exists()) tmpDir.mkdirs()
    return File.createTempFile("rsvp_", ".pdf", tmpDir)
}

private fun readTextUpTo(input: java.io.InputStream, maxBytes: Int): String {
    val baos = ByteArrayOutputStream(minOf(maxBytes, 64 * 1024))
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var total = 0
    while (true) {
        val read = input.read(buffer)
        if (read <= 0) break
        val remaining = maxBytes - total
        if (remaining <= 0) break
        val toWrite = minOf(read, remaining)
        baos.write(buffer, 0, toWrite)
        total += toWrite
        if (total >= maxBytes) break
    }
    return baos.toByteArray().toString(Charsets.UTF_8)
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
        if (total > maxBytes) error("PDF too large ($total bytes).")
        output.write(buffer, 0, read)
    }
}
