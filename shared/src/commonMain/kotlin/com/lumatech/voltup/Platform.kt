package com.lumatech.voltup

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform