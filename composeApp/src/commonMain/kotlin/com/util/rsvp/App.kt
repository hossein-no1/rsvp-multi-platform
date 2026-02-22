package com.util.rsvp

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.util.rsvp.datastore.DEFAULT_THEME_PRIMARY_ARGB
import com.util.rsvp.datastore.rememberAppDataStore
import com.util.rsvp.history.rememberPdfHistoryOpener
import com.util.rsvp.model.PdfHistoryItem
import com.util.rsvp.theme.LocalTheme
import com.util.rsvp.theme.darkAppColorScheme
import com.util.rsvp.theme.lightAppColorScheme
import kotlinx.coroutines.launch
import kotlin.math.hypot

@Composable
@Preview
fun App() {
    val appDataStore = rememberAppDataStore()
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { appDataStore.load() }
    val config by appDataStore.config.collectAsState()

    val systemDark = isSystemInDarkTheme()
    var isDark by remember(systemDark) { mutableStateOf(value = systemDark) }

    var route: AppRoute by remember { mutableStateOf(AppRoute.Gate) }

    var rootSize by remember { mutableStateOf(IntSize.Zero) }
    var fabCenterInRootPx by remember { mutableStateOf(Offset.Unspecified) }

    var revealFromDark by remember { mutableStateOf<Boolean?>(null) }
    var revealTargetDark by remember { mutableStateOf<Boolean?>(null) }
    val revealRadiusPx = remember { Animatable(initialValue = 0f) }
    val revealActive = revealTargetDark != null

    var themePickerOpen by remember { mutableStateOf(false) }
    var pdfHistoryOpen by remember { mutableStateOf(false) }

    val primaryArgb = config.themePrimaryArgb.takeIf { it != 0L } ?: DEFAULT_THEME_PRIMARY_ARGB
    val primaryColor = remember(primaryArgb) { argbLongToColor(primaryArgb) }

    androidx.compose.runtime.LaunchedEffect(revealTargetDark) {
        val target = revealTargetDark ?: return@LaunchedEffect

        if (rootSize == IntSize.Zero || fabCenterInRootPx == Offset.Unspecified) {
            revealFromDark = null
            revealTargetDark = null
            return@LaunchedEffect
        }

        val maxRadius = maxRevealRadiusPx(size = rootSize, center = fabCenterInRootPx)

        revealRadiusPx.snapTo(0f)
        revealRadiusPx.animateTo(
            targetValue = maxRadius,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        )

        revealFromDark = null
        revealTargetDark = null
    }

    LocalTheme(darkTheme = isDark, primary = primaryColor) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { rootSize = it },
        ) {
            when (val r = route) {
                AppRoute.Gate -> GateScreen(
                    modifier = Modifier.fillMaxSize(),
                    onContinue = { text -> route = AppRoute.Home(text = text) },
                    onPdfPicked = { item -> scope.launch { appDataStore.addPdfHistory(item) } },
                )

                is AppRoute.Home -> {
                    val homeState = rememberHomeState(
                        lorem = r.text,
                        initialReadingMode = config.lastReadingMode,
                    )
                    HomeScreen(
                        modifier = Modifier.fillMaxSize(),
                        state = homeState,
                        onReadingModeChange = { mode ->
                            homeState.updateReadingMode(mode)
                            scope.launch { appDataStore.setLastReadingMode(mode) }
                        },
                    )
                }
            }

            if (revealActive) {
                val fromDark = revealFromDark ?: !isDark
                val fromScheme = if (fromDark) {
                    darkAppColorScheme(primary = primaryColor)
                } else {
                    lightAppColorScheme(primary = primaryColor)
                }

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val r = revealRadiusPx.value.coerceAtLeast(0f)
                    val hole = Rect(
                        left = fabCenterInRootPx.x - r,
                        top = fabCenterInRootPx.y - r,
                        right = fabCenterInRootPx.x + r,
                        bottom = fabCenterInRootPx.y + r,
                    )

                    val path = Path().apply {
                        fillType = PathFillType.EvenOdd
                        addRect(Rect(0f, 0f, size.width, size.height))
                        addOval(hole)
                    }

                    drawPath(
                        path = path,
                        color = fromScheme.background,
                    )
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .offset(y = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = { themePickerOpen = true },
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Palette,
                        tint = MaterialTheme.colorScheme.onBackground,
                        contentDescription = "Theme color",
                    )
                }

                IconButton(
                    modifier = Modifier.onGloballyPositioned { coordinates ->
                        val pos = coordinates.positionInRoot()
                        val size = coordinates.size
                        fabCenterInRootPx = pos + Offset(
                            x = size.width / 2f,
                            y = size.height / 2f,
                        )
                    },
                    onClick = {
                        if (revealActive) return@IconButton
                        revealFromDark = isDark
                        val target = !isDark
                        isDark = target
                        revealTargetDark = target
                    },
                ) {
                    Icon(
                        imageVector = if (isDark) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                        tint = MaterialTheme.colorScheme.onBackground,
                        contentDescription = "Toggle theme",
                    )
                }
            }

            if (route is AppRoute.Home) {
                IconButton(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .offset(y = 16.dp),
                    onClick = { route = AppRoute.Gate },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        tint = MaterialTheme.colorScheme.onBackground,
                        contentDescription = "Back",
                    )
                }
            }

            if (route is AppRoute.Gate) {
                IconButton(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .offset(y = 16.dp),
                    onClick = { pdfHistoryOpen = true },
                ) {
                    Icon(
                        imageVector = Icons.Rounded.History,
                        tint = MaterialTheme.colorScheme.onBackground,
                        contentDescription = "PDF history",
                    )
                }
            }

            if (revealActive) {
                val interactionSource = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = {},
                        )
                )
            }

            if (themePickerOpen) {
                ThemeColorDialog(
                    onDismiss = { themePickerOpen = false },
                    onPick = { argb ->
                        scope.launch { appDataStore.setThemePrimaryArgb(argb) }
                        themePickerOpen = false
                    },
                )
            }

            if (pdfHistoryOpen) {
                PdfHistoryDialog(
                    history = config.pdfHistory,
                    onDismiss = { pdfHistoryOpen = false },
                    onClear = {
                        scope.launch { appDataStore.clearPdfHistory() }
                        pdfHistoryOpen = false
                    },
                    onRemove = { item ->
                        scope.launch { appDataStore.removePdfHistory { it == item } }
                    },
                    onOpenText = { text ->
                        route = AppRoute.Home(text = text)
                        pdfHistoryOpen = false
                    },
                )
            }
        }
    }
}

@Composable
private fun ThemeColorDialog(
    onDismiss: () -> Unit,
    onPick: (Long) -> Unit,
) {
    val presets: List<Pair<String, Long>> = listOf(
        "Brand" to DEFAULT_THEME_PRIMARY_ARGB,
        "Blue" to 0xFF1565C0L,
        "Green" to 0xFF2E7D32L,
        "Purple" to 0xFF6A1B9AL,
        "Orange" to 0xFFEF6C00L,
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Theme color",
                style = MaterialTheme.typography.headlineSmall,
                fontSize = 16.sp
            )
        },
        text = {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                presets.forEach { (name, argb) ->
                    IconButton(onClick = { onPick(argb) }) {
                        Canvas(modifier = Modifier.size(28.dp)) {
                            drawCircle(color = argbLongToColor(argb))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Close",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 14.sp
                )
            }
        },
    )
}

@Composable
private fun PdfHistoryDialog(
    history: List<PdfHistoryItem>,
    onDismiss: () -> Unit,
    onClear: () -> Unit,
    onRemove: (PdfHistoryItem) -> Unit,
    onOpenText: (String) -> Unit,
) {
    val opener = rememberPdfHistoryOpener()
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "History",
                style = MaterialTheme.typography.headlineSmall,
                fontSize = 16.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (history.isEmpty()) {
                    Text(
                        text = "No history yet.",
                        style = MaterialTheme.typography.labelMedium,
                        fontSize = 14.sp
                    )
                } else {
                    history.forEach { item ->
                        val hasSavedText = item.text.isNotBlank()
                        val isHttp =
                            item.uri?.startsWith("http://") == true || item.uri?.startsWith("https://") == true
                        val detailLine = item.preview
                            ?: item.uri
                            ?: item.text.singleLinePreview()

                        val existsState by produceState<Boolean?>(initialValue = null, item) {
                            value = when {
                                hasSavedText -> true
                                item.uri == null -> true
                                isHttp -> true
                                else -> runCatching { opener.exists(item) }.getOrDefault(false)
                            }
                        }
                        val canOpen = hasSavedText || (existsState == true)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.03f))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(enabled = canOpen) {
                                        if (item.text.isNotBlank()) {
                                            onOpenText(item.text)
                                        } else {
                                            scope.launch {
                                                val text =
                                                    runCatching { opener.openText(item) }.getOrNull()
                                                        .orEmpty()
                                                if (text.isNotBlank()) onOpenText(text)
                                            }
                                        }
                                    },
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                Text(
                                    text = item.name.ifBlank { "History item" },
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (canOpen) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = detailLine,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }

                            TextButton(
                                onClick = { onRemove(item) },
                                enabled = true,
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                            ) {
                                Text(
                                    text = "Remove",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (history.isNotEmpty()) {
                    TextButton(onClick = onClear) {
                        Text(
                            text = "Clear", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 14.sp
                        )
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text(
                        text = "Close",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 14.sp
                    )
                }
            }
        },
    )
}

private fun String.singleLinePreview(
    maxChars: Int = 80,
): String {
    val single = lineSequence().joinToString(" ") { it.trim() }.trim().replace(Regex("\\s+"), " ")
    if (single.length <= maxChars) return single
    return single.take(maxChars).trimEnd() + "…"
}

private fun maxRevealRadiusPx(
    size: IntSize,
    center: Offset,
): Float {
    val dx = maxOf(center.x, size.width - center.x)
    val dy = maxOf(center.y, size.height - center.y)
    return hypot(dx, dy)
}

private fun argbLongToColor(argb: Long): Color {
    val v = argb and 0xFFFF_FFFFL
    val a = ((v ushr 24) and 0xFF).toInt() / 255f
    val r = ((v ushr 16) and 0xFF).toInt() / 255f
    val g = ((v ushr 8) and 0xFF).toInt() / 255f
    val b = (v and 0xFF).toInt() / 255f
    return Color(red = r, green = g, blue = b, alpha = a)
}