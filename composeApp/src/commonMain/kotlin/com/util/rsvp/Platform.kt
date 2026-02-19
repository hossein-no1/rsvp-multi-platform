package com.util.rsvp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform