package com.util.rsvp.datastore

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Composable
actual fun rememberPlatformKeyValueStore(): PlatformKeyValueStore =
    remember { DesktopFileKeyValueStore() }

private class DesktopFileKeyValueStore : PlatformKeyValueStore {
    private val baseDir: Path = Paths.get(System.getProperty("user.home"), ".rsvp")

    private fun fileForKey(key: String): Path = baseDir.resolve("$key.json")

    override suspend fun getString(key: String): String? = withContext(Dispatchers.IO) {
        runCatching {
            val path = fileForKey(key)
            if (!Files.exists(path)) return@runCatching null
            Files.readString(path, StandardCharsets.UTF_8)
        }.getOrNull()
    }

    override suspend fun putString(key: String, value: String) = withContext(Dispatchers.IO) {
        runCatching {
            Files.createDirectories(baseDir)
            Files.writeString(fileForKey(key), value, StandardCharsets.UTF_8)
        }.getOrNull()
        Unit
    }

    override suspend fun remove(key: String) = withContext(Dispatchers.IO) {
        runCatching { Files.deleteIfExists(fileForKey(key)) }.getOrNull()
        Unit
    }
}

