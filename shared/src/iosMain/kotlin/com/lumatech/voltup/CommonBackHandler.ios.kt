package com.lumatech.voltup

import androidx.compose.runtime.Composable

@Composable
actual fun CommonBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS uses swipe-to-back navigation natively
}
