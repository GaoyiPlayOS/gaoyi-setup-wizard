package com.android.setupwizard.ui.lock

import android.app.KeyguardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Dialpad
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material.icons.outlined.Pattern
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.setupwizard.ui.locale.LocalStrings

/**
 * 第四屏：锁屏安全设置（完全居左 / Android 12+ 现代布局）。
 */

@Composable
fun LockScreen(
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val strings = LocalStrings.current

    val wizardTheme = if (isSystemInDarkTheme()) WIZARD_THEME_DARK else WIZARD_THEME_LIGHT

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { _ ->
        // ChooseLockGeneric 在向导流常返回 RESULT_FIRST_USER（== RESULT_FINISHED），偶尔为
        // RESULT_OK，结果码并不稳定；故以 KeyguardManager.isDeviceSecure() 为权威判据：
        // 仅当设备确已设锁才前进，用户中途返回（未设锁）则停留本屏。
        if (isDeviceSecured(context)) {
            onNext()
        }
    }

    val launchLockSetup: (LockType) -> Unit = { type ->
        runCatching { launcher.launch(buildChooseLockIntent(type, wizardTheme)) }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colorScheme.surface,
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            // 区域 A: 顶部安全图标与标题（居左）
            Spacer(modifier = Modifier.height(8.dp))

            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = null,
                tint = colorScheme.primary,
                modifier = Modifier.size(48.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = strings.lockTitle,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.5).sp,
                ),
                color = colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = strings.lockSubtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 区域 B: 三个凭证类型选项（居左，垂直排列）
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                LockOptionItem(
                    icon = Icons.Outlined.Dialpad,
                    title = strings.lockPin,
                    description = strings.lockPinDesc,
                    onClick = { launchLockSetup(LockType.Pin) },
                )
                LockOptionItem(
                    icon = Icons.Outlined.Password,
                    title = strings.lockPassword,
                    description = strings.lockPasswordDesc,
                    onClick = { launchLockSetup(LockType.Password) },
                )
                LockOptionItem(
                    icon = Icons.Outlined.Pattern,
                    title = strings.lockPattern,
                    description = strings.lockPatternDesc,
                    onClick = { launchLockSetup(LockType.Pattern) },
                )
            }

            // 区域 C: 底部导航栏 —— 仅左侧「不设置」，设锁成功后自动流转故无需「下一步」
            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onNext) {
                    Text(
                        text = strings.lockSkip,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
private fun LockOptionItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colorScheme.surfaceContainerLow,
        contentColor = colorScheme.onSurface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )

            Spacer(modifier = Modifier.width(20.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = colorScheme.onSurfaceVariant,
            )
        }
    }
}

/* 
 * ---------- 系统锁屏配置组件的特权唤起 ---------- 
 * 好像可以直接跳 Activity，就用了哦
*/

private const val SETTINGS_PACKAGE = "com.android.settings"
private const val CHOOSE_LOCK_PATTERN = "com.android.settings.password.ChooseLockPattern"
private const val CHOOSE_LOCK_PASSWORD = "com.android.settings.password.ChooseLockPassword"
private const val WIZARD_THEME_LIGHT = "glif_v3_light"
private const val WIZARD_THEME_DARK = "glif_v3"

private enum class LockType(val passwordQuality: Int) {
    Pattern(0x00010000),   // PASSWORD_QUALITY_SOMETHING
    Pin(0x00020000),       // PASSWORD_QUALITY_NUMERIC
    Password(0x00040000),  // PASSWORD_QUALITY_ALPHABETIC
}

private fun buildChooseLockIntent(type: LockType, wizardTheme: String): Intent = when (type) {
    LockType.Pattern -> Intent().apply {
        component = ComponentName(SETTINGS_PACKAGE, CHOOSE_LOCK_PATTERN)
        putExtra("is_setup_flow", true)
        putExtra("isSetupFlow", true)
        putExtra("theme", wizardTheme)
    }
    
    LockType.Pin, LockType.Password -> Intent().apply {
        component = ComponentName(SETTINGS_PACKAGE, CHOOSE_LOCK_PASSWORD)
        putExtra("is_setup_flow", true)
        putExtra("isSetupFlow", true)
        putExtra("theme", wizardTheme)
        putExtra("lockscreen.password_type", type.passwordQuality)
        putExtra("password_quality", type.passwordQuality)  // 兼容备用
    }
}

private fun isDeviceSecured(context: Context): Boolean = runCatching {
    val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
    keyguardManager?.isDeviceSecure == true
}.getOrDefault(false)
