package com.util.rsvp.datastore

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.browser.window

@Composable
actual fun rememberPlatformKeyValueStore(): PlatformKeyValueStore =
    remember { WasmLocalStorageKeyValueStore() }

private class WasmLocalStorageKeyValueStore : PlatformKeyValueStore {
    private val storage = window.localStorage

    override suspend fun getString(key: String): String? =
        storage.getItem(key)

    override suspend fun putString(key: String, value: String) {
        storage.setItem(key, value)
    }

    override suspend fun remove(key: String) {
        storage.removeItem(key)
    }
}

