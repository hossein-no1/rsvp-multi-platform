package com.util.rsvp

import kotlin.JsFun
import kotlin.js.ExperimentalWasmJsInterop

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => Date.now()")
private external fun jsDateNow(): Double

internal actual fun nowEpochMs(): Long = jsDateNow().toLong()

