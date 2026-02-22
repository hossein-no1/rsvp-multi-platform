package com.util.rsvp.model

import kotlinx.serialization.Serializable

@Serializable
data class PdfHistoryItem(
    val name: String,
    val uri: String? = null,
    /**
     * Extracted/entered text for restoring across platforms.
     *
     * - File: extracted text from the PDF at pick time.
     * - Link: downloaded + extracted text at download time.
     * - Paste: the raw pasted text.
     */
    val text: String = "",
    /**
     * Optional UI-only detail line (e.g. paste preview). If null, UI can fall back to [uri].
     */
    val preview: String? = null,
    val addedAtEpochMs: Long,
)

