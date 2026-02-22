package com.util.rsvp.model

import kotlinx.serialization.Serializable

@Serializable
data class PdfHistoryItem(
    val name: String,
    val uri: String? = null,
    val addedAtEpochMs: Long,
)

