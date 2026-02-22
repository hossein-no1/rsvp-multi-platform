package com.util.rsvp.datastore

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSUserDefaults

@Composable
actual fun rememberPlatformKeyValueStore(): PlatformKeyValueStore =
    remember { IosUserDefaultsKeyValueStore() }

private class IosUserDefaultsKeyValueStore : PlatformKeyValueStore {
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults()

    override suspend fun getString(key: String): String? =
        defaults.stringForKey(key)

    override suspend fun putString(key: String, value: String) {
        defaults.setObject(value, forKey = key)
    }

    override suspend fun remove(key: String) {
        defaults.removeObjectForKey(key)
    }
}

