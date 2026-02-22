package com.util.rsvp.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.util.rsvp.model.PdfHistoryItem

@Composable
expect fun InputPDF(
    modifier: Modifier = Modifier,
    onPicked: (PdfHistoryItem) -> Unit,
    onResult : (String) -> Unit
)