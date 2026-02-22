package com.util.rsvp.datastore

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.rsvpDataStore: DataStore<Preferences> by preferencesDataStore(name = "rsvp_datastore")

@Composable
actual fun rememberPlatformKeyValueStore(): PlatformKeyValueStore {
    val context = LocalContext.current.applicationContext
    return remember(context) { AndroidPlatformKeyValueStore(context) }
}

private class AndroidPlatformKeyValueStore(
    private val context: Context,
) : PlatformKeyValueStore {
    private val ds: DataStore<Preferences> get() = context.rsvpDataStore

    override suspend fun getString(key: String): String? =
        ds.data.first()[stringPreferencesKey(key)]

    override suspend fun putString(key: String, value: String) {
        val prefKey = stringPreferencesKey(key)
        ds.edit { prefs -> prefs[prefKey] = value }
    }

    override suspend fun remove(key: String) {
        val prefKey = stringPreferencesKey(key)
        ds.edit { prefs -> prefs.remove(prefKey) }
    }
}

