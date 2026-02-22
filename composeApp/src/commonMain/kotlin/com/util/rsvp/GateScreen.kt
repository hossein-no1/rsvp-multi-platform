package com.util.rsvp

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.util.rsvp.component.InputPDF
import com.util.rsvp.model.PdfHistoryItem

@Composable
fun GateScreen(
    modifier: Modifier = Modifier,
    onContinue: (String) -> Unit,
    onPdfPicked: (PdfHistoryItem) -> Unit,
) {

    var text by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var paste by remember { mutableStateOf("") }
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("File", "Link", "Paste")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
        content = {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = .5F)
                    .clip(shape = RoundedCornerShape(size = 8.dp))
                    .background(color = MaterialTheme.colorScheme.onBackground.copy(alpha = .05F)),
                contentAlignment = Alignment.Center,
                content = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(all = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(
                            space = 4.dp,
                            alignment = Alignment.Top
                        ),
                        horizontalAlignment = Alignment.Start,
                        content = {
                            Text(
                                text = "Start reading",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Drop a document or paste a link.\n" +
                                        "We'll present it word-by-word for focused reading.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Start
                            )
                            Spacer(modifier = Modifier.height(height = 12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(shape = RoundedCornerShape(8.dp))
                                    .background(
                                        color = MaterialTheme.colorScheme.onBackground.copy(
                                            alpha = 0.02f
                                        )
                                    )
                                    .padding(all = 6.dp),
                                content = {
                                    SecondaryTabRow(
                                        selectedTabIndex = selectedTabIndex,
                                        modifier = Modifier.fillMaxWidth(),
                                        containerColor = Color.Transparent,
                                        contentColor = TabRowDefaults.primaryContentColor,
                                        tabs = {
                                            tabs.forEachIndexed { index, title ->
                                                val selected = selectedTabIndex == index
                                                Tab(
                                                    modifier = Modifier
                                                        .width(80.dp)
                                                        .clip(shape = RoundedCornerShape(6.dp))
                                                        .background(
                                                            color = if (selected) MaterialTheme.colorScheme.background else Color.Transparent
                                                        ),
                                                    selected = selected,
                                                    onClick = { selectedTabIndex = index },
                                                    selectedContentColor = MaterialTheme.colorScheme.onBackground,
                                                    unselectedContentColor = MaterialTheme.colorScheme.onBackground.copy(
                                                        alpha = 0.7f
                                                    ),
                                                    text = {
                                                        Text(
                                                            text = title,
                                                            color = if (selected) MaterialTheme.colorScheme.onBackground else Color.Gray,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontSize = 12.sp
                                                        )
                                                    }
                                                )
                                            }
                                        },
                                        indicator = {},
                                        divider = {}
                                    )
                                }
                            )
                            Spacer(modifier = Modifier.height(height = 12.dp))

                            AnimatedContent(
                                targetState = selectedTabIndex,
                                transitionSpec = {
                                    val direction = if (targetState > initialState) 1 else -1
                                    ((
                                            slideInHorizontally(
                                                animationSpec = tween(220),
                                                initialOffsetX = { direction * (it / 10) }
                                            ) + fadeIn(animationSpec = tween(220))) togetherWith
                                            (slideOutHorizontally(
                                                animationSpec = tween(220),
                                                targetOffsetX = { -direction * (it / 10) }
                                            ) + fadeOut(animationSpec = tween(120)))
                                            ).using(SizeTransform(clip = false))
                                },
                                label = "GateTabsContent",
                                contentAlignment = Alignment.TopStart,
                                content = { tabIndex ->
                                    when (tabIndex) {
                                        0 -> InputPDF(
                                            modifier = Modifier.fillMaxWidth(),
                                            onPicked = onPdfPicked,
                                            onResult = { text = it },
                                        )

                                        1 -> InputUrl(
                                            modifier = Modifier.fillMaxWidth(),
                                            value = url,
                                            onValueChange = { url = it },
                                        )

                                        else -> InputPaste(
                                            modifier = Modifier.fillMaxWidth(),
                                            value = paste,
                                            onValueChange = { paste = it },
                                        )
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.height(height = 24.dp))
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                enabled = text.isNotBlank(),
                                onClick = { onContinue(text) },
                                shape = RoundedCornerShape(size = 8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.onBackground,
                                    contentColor = MaterialTheme.colorScheme.background
                                )
                            ) {
                                Text(
                                    text = "Let's go",
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            }
                        }
                    )
                }
            )
        }
    )

}

@Composable
private fun InputUrl(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = 8.dp),
        content = {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(size = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                    focusedPlaceholderColor = Color.Gray.copy(alpha = .2F),
                    unfocusedPlaceholderColor = Color.Gray.copy(alpha = .2F),
                    cursorColor = MaterialTheme.colorScheme.onBackground
                ),
                placeholder = {
                    Text(
                        text = "Paste article or PDF link...",
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 14.sp
                    )
                }
            )
        }
    )
}

@Composable
private fun InputPaste(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = 8.dp),
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            minLines = 6,
            maxLines = 6,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                focusedPlaceholderColor = Color.Gray.copy(alpha = .2F),
                unfocusedPlaceholderColor = Color.Gray.copy(alpha = .2F),
            ),
            placeholder = {
                Text(
                    text = "Paste text…",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 14.sp
                )
            }
        )
    }
}