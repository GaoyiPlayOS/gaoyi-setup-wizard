package com.android.setupwizard.ui.preferences

import android.app.UiModeManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.setupwizard.ui.locale.LocalStrings

/**
 * 第五屏：系统偏好与动态取色（完全居左 / Android 12+ 现代布局）。
 * 外观状态完全 hoist 至 Activity：本屏只读 [isDarkTheme]、上抛 [onDarkThemeChange]，保持无状态。
 */
@Composable
fun PreferencesScreen(
    isDarkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val strings = LocalStrings.current

    // 选择外观：先即时重绘向导自身，再借 android.uid.system 特权全局热切换系统夜间模式
    val onSelectMode: (Boolean) -> Unit = { wantDark ->
        onDarkThemeChange(wantDark)
        applySystemNightMode(context, wantDark)
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
            // 区域 A: 顶部调色盘图标与标题（居左）
            Spacer(modifier = Modifier.height(8.dp))

            Icon(
                imageVector = Icons.Outlined.Palette,
                contentDescription = null,
                tint = colorScheme.primary,
                modifier = Modifier.size(48.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = strings.preferencesTitle,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.5).sp,
                ),
                color = colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 区域 B: 偏好配置核心区（居左）
            // 子区域 1: Monet 调色盘预览
            Text(
                text = strings.monetPalette,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MonetSwatch(color = colorScheme.primary)
                MonetSwatch(color = colorScheme.secondary)
                MonetSwatch(color = colorScheme.tertiary)
                MonetSwatch(color = colorScheme.primaryContainer)
                MonetSwatch(color = colorScheme.error)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { launchMonetManager(context, strings.noMonetManager) },
                shape = RoundedCornerShape(24.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Palette,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.customizeColors,
                    fontWeight = FontWeight.Medium,
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // 子区域 2: 外观模式切换（深 / 浅色单选卡片）
            Text(
                text = strings.appearanceTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AppearanceCard(
                    icon = Icons.Outlined.LightMode,
                    title = strings.lightMode,
                    description = strings.lightModeDesc,
                    selected = !isDarkTheme,
                    onSelect = { onSelectMode(false) },
                    modifier = Modifier.weight(1f),
                )
                AppearanceCard(
                    icon = Icons.Outlined.DarkMode,
                    title = strings.darkMode,
                    description = strings.darkModeDesc,
                    selected = isDarkTheme,
                    onSelect = { onSelectMode(true) },
                    modifier = Modifier.weight(1f),
                )
            }

            // 区域 C: 底部双按钮导航栏
            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(
                    onClick = onBack,
                    shape = RoundedCornerShape(24.dp),
                ) {
                    Text(
                        text = strings.back,
                        fontWeight = FontWeight.Medium,
                    )
                }

                FilledTonalButton(
                    onClick = onNext,
                    shape = RoundedCornerShape(24.dp),
                ) {
                    Text(
                        text = strings.nextStep,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                    )
                }
            }
        }
    }
}

/** Monet 动态取色预览：单个圆形色块，描边取自当前 outline 以适配深浅背景。 */
@Composable
private fun MonetSwatch(color: Color) {
    val outline = MaterialTheme.colorScheme.outlineVariant
    Surface(
        modifier = Modifier.size(44.dp),
        shape = CircleShape,
        color = color,
        border = BorderStroke(1.dp, outline),
    ) {}
}

/** 外观模式单选卡片：选中时高亮描边 + 容器底色，role=RadioButton 以正确播报无障碍语义。 */
@Composable
private fun AppearanceCard(
    icon: ImageVector,
    title: String,
    description: String,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme

    val containerColor by animateColorAsState(
        targetValue = if (selected) colorScheme.secondaryContainer else colorScheme.surfaceContainerLow,
        animationSpec = tween(durationMillis = 220),
        label = "appearance-card-bg",
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) colorScheme.primary else Color.Transparent,
        animationSpec = tween(durationMillis = 220),
        label = "appearance-card-border",
    )
    val contentColor = if (selected) colorScheme.onSecondaryContainer else colorScheme.onSurface

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onSelect,
            ),
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        contentColor = contentColor,
        border = BorderStroke(2.dp, borderColor),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) colorScheme.primary else colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = if (selected) {
                    colorScheme.onSecondaryContainer
                } else {
                    colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

private const val SECURE_UI_NIGHT_MODE = "ui_night_mode"

/**
 * 借助 android.uid.system 特权全局热切换系统夜间模式：
 * 主路径走公开的 [UiModeManager.setNightMode]
 * 失败再回退写入 Settings.Secure 的 `ui_night_mode`。非特权运行时静默降级（兜底容错）
 */
private fun applySystemNightMode(context: Context, dark: Boolean): Boolean {
    val mode = if (dark) UiModeManager.MODE_NIGHT_YES else UiModeManager.MODE_NIGHT_NO
    // 主路径：公开的 UiModeManager.setNightMode
    if (setNightModeViaManager(context, mode)) return true
    // 兜底
    return runCatching {
        Settings.Secure.putInt(context.contentResolver, SECURE_UI_NIGHT_MODE, mode)
    }.getOrDefault(false)
}

private fun setNightModeViaManager(context: Context, mode: Int): Boolean = runCatching {
    val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager
        ?: return@runCatching false
    uiModeManager.setNightMode(mode)
    true
}.getOrDefault(false)

/* ---------- Monet 管理器的三级防御性跳转 ---------- */
private const val COLORBLENDR_PACKAGE = "com.drdisagree.colorblendr"
private const val COLORBLENDR_ACTIVITY = "com.drdisagree.colorblendr.ui.activities.MainActivity"
private const val WALLPAPER_PACKAGE = "com.android.wallpaper"
private const val WALLPAPER_PICKER_ACTIVITY = "com.android.wallpaper.picker.CustomizationPickerActivity"

/* 分级唤起 Monet 管理器，全程守卫以杜绝 ActivityNotFoundException */
private fun launchMonetManager(context: Context, fallbackToast: String) {
    // 一级
    if (isPackageInstalled(context, COLORBLENDR_PACKAGE)) {
        val intent = resolvableExplicitIntent(context, COLORBLENDR_PACKAGE, COLORBLENDR_ACTIVITY)
        if (intent != null && startActivitySafely(context, intent)) return
    }

    // 二级
    val wallpaper = resolvableExplicitIntent(context, WALLPAPER_PACKAGE, WALLPAPER_PICKER_ACTIVITY)
    if (wallpaper != null && startActivitySafely(context, wallpaper)) return

    // 三级：Toast 拦截
    Toast.makeText(context, fallbackToast, Toast.LENGTH_SHORT).show()
}

private fun isPackageInstalled(context: Context, pkg: String): Boolean = runCatching {
    context.packageManager.getPackageInfo(pkg, 0)
    true
}.getOrDefault(false)

/** 构建显式 Intent 并以 resolveActivity 校验组件确实可达，不可达返回 null。 */
private fun resolvableExplicitIntent(context: Context, pkg: String, cls: String): Intent? = runCatching {
    val intent = Intent(Intent.ACTION_MAIN).apply {
        component = ComponentName(pkg, cls)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    if (intent.resolveActivity(context.packageManager) != null) intent else null
}.getOrNull()

private fun startActivitySafely(context: Context, intent: Intent): Boolean = runCatching {
    context.startActivity(intent)
    true
}.getOrDefault(false)
