package com.util.rsvp.datastore

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.util.rsvp.ReadingMode
import com.util.rsvp.model.PdfHistoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val CONFIG_KEY = "app_config_v1"
private const val PDF_HISTORY_LIMIT = 25

class AppDataStore internal constructor(
    private val kv: PlatformKeyValueStore,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val _config = MutableStateFlow(AppConfig())
    val config: StateFlow<AppConfig> = _config.asStateFlow()

    suspend fun load() {
        val raw = kv.getString(CONFIG_KEY) ?: return
        val decoded = runCatching { json.decodeFromString(AppConfig.serializer(), raw) }.getOrNull() ?: return
        _config.value = decoded
    }

    suspend fun setThemePrimaryArgb(argb: Long) = persist(_config.value.copy(themePrimaryArgb = argb))

    suspend fun setLastReadingMode(mode: ReadingMode) = persist(_config.value.copy(lastReadingMode = mode))

    suspend fun addPdfHistory(item: PdfHistoryItem) {
        val current = _config.value
        val newList = buildList {
            add(item)
            current.pdfHistory
                .asSequence()
                .filterNot { it.uri != null && item.uri != null && it.uri == item.uri }
                .filterNot { it.uri == null && item.uri == null && it.name == item.name }
                .take(PDF_HISTORY_LIMIT - 1)
                .forEach { add(it) }
        }
        persist(current.copy(pdfHistory = newList))
    }

    suspend fun clearPdfHistory() = persist(_config.value.copy(pdfHistory = emptyList()))

    suspend fun removePdfHistory(predicate: (PdfHistoryItem) -> Boolean) {
        val current = _config.value
        persist(current.copy(pdfHistory = current.pdfHistory.filterNot(predicate)))
    }

    private suspend fun persist(newConfig: AppConfig) {
        _config.value = newConfig
        kv.putString(CONFIG_KEY, json.encodeToString(newConfig))
    }
}

@Composable
fun rememberAppDataStore(): AppDataStore {
    val kv = rememberPlatformKeyValueStore()
    return remember(kv) { AppDataStore(kv) }
}

