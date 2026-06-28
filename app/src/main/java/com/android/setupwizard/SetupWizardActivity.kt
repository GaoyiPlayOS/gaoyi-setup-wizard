package com.android.setupwizard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.android.setupwizard.navigation.SetupWizardHost
import com.android.setupwizard.ui.theme.SetupWizardTheme

class SetupWizardActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            SetupWizardTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SetupWizardHost(
                        onSetupFinished = ::finish,
                    )
                }
            }
        }
    }
}
