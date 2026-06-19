package com.scanner

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform