package com.util.rsvp.datastore

import com.util.rsvp.ReadingMode
import com.util.rsvp.model.PdfHistoryItem
import kotlinx.serialization.Serializable

@Serializable
data class AppConfig(
    /**
     * Stored as raw ARGB bits (0xAARRGGBB). This may be negative as a signed [Long].
     */
    val themePrimaryArgb: Long = DEFAULT_THEME_PRIMARY_ARGB,
    val lastReadingMode: ReadingMode = ReadingMode.Focus,
    val pdfHistory: List<PdfHistoryItem> = emptyList(),
)

internal const val DEFAULT_THEME_PRIMARY_ARGB: Long = 0xFFC71D25L

