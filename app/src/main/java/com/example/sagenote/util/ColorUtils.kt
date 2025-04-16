package com.example.sagenote.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb

/**
 * Determines if a color is light or dark
 * @param color The color to check
 * @return true if the color is light, false if it's dark
 */
fun isLightColor(color: Int): Boolean {
    return Color(color).luminance() > 0.5f
}

/**
 * Returns appropriate text color (black or white) based on background color
 * @param backgroundColor The background color
 * @return White for dark backgrounds, Black for light backgrounds
 */
fun getTextColorForBackground(backgroundColor: Int): Int {
    return if (isLightColor(backgroundColor)) {
        Color.Black.toArgb()
    } else {
        Color.White.toArgb()
    }
}
