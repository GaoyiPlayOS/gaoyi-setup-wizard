package com.android.setupwizard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.android.setupwizard.navigation.SetupWizardHost
import com.android.setupwizard.ui.locale.LocalStrings
import com.android.setupwizard.ui.locale.stringsFor
import com.android.setupwizard.ui.theme.SetupWizardTheme
import com.android.setupwizard.ui.theme.ThemeMode
import java.util.Locale

class SetupWizardActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            // LocalStrings 重组，实现所见即所得的实时语言切换
            var selectedLocale by remember { mutableStateOf(Locale.getDefault()) }

            // 向导自身的外观覆写：用户在偏好屏切换深/浅色时即时重绘
            var themeMode by remember { mutableStateOf(ThemeMode.System) }
            val isDark = when (themeMode) {
                ThemeMode.Dark -> true
                ThemeMode.Light -> false
                ThemeMode.System -> isSystemInDarkTheme()
            }

            CompositionLocalProvider(LocalStrings provides stringsFor(selectedLocale)) {
                SetupWizardTheme(darkTheme = isDark) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        SetupWizardHost(
                            selectedLocale = selectedLocale,
                            onLocaleSelected = { selectedLocale = it },
                            isDarkTheme = isDark,
                            onDarkThemeChange = { wantDark ->
                                themeMode = if (wantDark) ThemeMode.Dark else ThemeMode.Light
                            },
                            onSetupFinished = ::finish,
                        )
                    }
                }
            }
        }
    }
}
