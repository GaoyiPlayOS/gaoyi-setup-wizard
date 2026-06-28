package com.android.setupwizard.ui.theme

import android.graphics.Typeface
import androidx.compose.ui.text.font.DeviceFontFamilyName
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

/**
 * 通过 [DeviceFontFamilyName] 直接命中设备已安装字体
 * 框架静默回退到系统默认字体
 */

private val BrandFamilyCandidates = listOf(
    "google-sans-flex",
    "google-sans",
    "googlesans",
    "Google Sans Flex",
    "Google Sans",
)

val BrandFontFamily: FontFamily by lazy {
    val resolved = BrandFamilyCandidates.firstOrNull { name ->
        runCatching { Typeface.create(name, Typeface.NORMAL) != Typeface.DEFAULT }
            .getOrDefault(false)
    }

    resolved?.let { name ->
        FontFamily(
            Font(DeviceFontFamilyName(name), FontWeight.Normal),
            Font(DeviceFontFamilyName(name), FontWeight.Medium),
            Font(DeviceFontFamilyName(name), FontWeight.SemiBold),
        )
    } ?: FontFamily.Default
}
