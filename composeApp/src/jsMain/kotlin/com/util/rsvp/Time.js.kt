package com.util.rsvp

internal actual fun nowEpochMs(): Long = kotlin.js.Date().getTime().toLong()

