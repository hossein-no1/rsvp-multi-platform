package com.util.rsvp.datastore

import androidx.compose.runtime.Composable

interface PlatformKeyValueStore {
    suspend fun getString(key: String): String?
    suspend fun putString(key: String, value: String)
    suspend fun remove(key: String)
}

@Composable
expect fun rememberPlatformKeyValueStore(): PlatformKeyValueStore

