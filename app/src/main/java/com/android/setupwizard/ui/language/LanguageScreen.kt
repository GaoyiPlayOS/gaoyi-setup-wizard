package com.android.setupwizard.ui.language

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.LocaleList
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageScreen(
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme

    val availableLocales = remember { systemAvailableLocales() }
    var selectedTag by rememberSaveable { mutableStateOf(Locale.getDefault().toLanguageTag()) }
    var sheetVisible by rememberSaveable { mutableStateOf(false) }

    val selectedLocale = remember(selectedTag) { Locale.forLanguageTag(selectedTag) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        colorScheme.surfaceBright,
                        colorScheme.surface,
                        colorScheme.surfaceContainerLowest,
                    ),
                ),
            ),
        containerColor = Color.Transparent,
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
            // 区域 A: 顶部图标与标题（居左）
            Spacer(modifier = Modifier.height(8.dp))

            Icon(
                imageVector = Icons.Outlined.Language,
                contentDescription = null,
                tint = colorScheme.primary,
                modifier = Modifier.size(48.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Set Language",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.5).sp,
                ),
                color = colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(40.dp))

            // 区域 B: 中部选项卡（居左）
            LanguageSelectorCard(
                title = selectedLocale.autonym(),
                subtitle = "Change / 更改",
                onClick = { sheetVisible = true },
            )

            Spacer(modifier = Modifier.weight(1f))

            // 区域 C: 底部导航栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilledTonalButton(
                    onClick = onNext,
                    shape = RoundedCornerShape(24.dp),
                ) {
                    Text(
                        text = "CONTINUE",
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

    if (sheetVisible) {
        LanguageBottomSheet(
            locales = availableLocales,
            selectedTag = selectedTag,
            onSelect = { locale ->
                selectedTag = locale.toLanguageTag()
                applySystemLocale(context, locale)
                sheetVisible = false
            },
            onDismiss = { sheetVisible = false },
        )
    }
}

@Composable
private fun LanguageSelectorCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = colorScheme.surfaceContainerHigh,
        contentColor = colorScheme.onSurface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageBottomSheet(
    locales: List<Locale>,
    selectedTag: String,
    onSelect: (Locale) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            item {
                Text(
                    text = "Select Language / 选择语言",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                )
            }
            items(
                items = locales,
                key = { locale -> locale.toLanguageTag() },
            ) { locale ->
                LanguageRow(
                    locale = locale,
                    selected = locale.toLanguageTag() == selectedTag,
                    onClick = { onSelect(locale) },
                )
            }
        }
    }
}

@Composable
private fun LanguageRow(
    locale: Locale,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val autonym = locale.autonym()
    val englishName = locale.getDisplayName(Locale.ENGLISH)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = autonym,
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.onSurface,
            )
            if (!englishName.equals(autonym, ignoreCase = true)) {
                Text(
                    text = englishName,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                )
            }
        }

        if (selected) {
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = "Selected",
                tint = colorScheme.primary,
            )
        }
    }
}

private fun Locale.autonym(): String = getDisplayName(this).replaceFirstChar { char ->
    if (char.isLowerCase()) char.titlecase(this) else char.toString()
}

private val FallbackLocales = listOf(
    Locale.US,
    Locale.UK,
    Locale.SIMPLIFIED_CHINESE,
    Locale.TRADITIONAL_CHINESE,
    Locale.JAPANESE,
    Locale.KOREAN,
    Locale.GERMANY,
    Locale.FRANCE,
)

private fun systemAvailableLocales(): List<Locale> {
    val tags = runCatching { Resources.getSystem().assets.locales }.getOrNull()?.toList().orEmpty()
    val resolved = tags.asSequence()
        .filter { tag -> tag.isNotBlank() }
        .map { tag -> Locale.forLanguageTag(tag.replace('_', '-')) }
        .filter { locale -> locale.language.isNotBlank() }
        .distinctBy { locale -> locale.toLanguageTag() }
        .sortedBy { locale -> locale.autonym() }
        .toList()
    return resolved.ifEmpty { FallbackLocales }
}

/**
 * 借助 android.uid.system 特权，对 IActivityManager 持久化配置写入新的 LocaleList，
 * 实现真正的系统级语言切换。非特权运行时静默降级（兜底容错），由调用方负责同步本地状态。
 */
private fun applySystemLocale(context: Context, locale: Locale): Boolean = runCatching {
    val localeList = LocaleList(locale)
    LocaleList.setDefault(localeList)
    Locale.setDefault(locale)

    val activityManagerService = Class.forName("android.app.ActivityManager")
        .getMethod("getService")
        .invoke(null) ?: return@runCatching false

    val serviceInterface = Class.forName("android.app.IActivityManager")
    val configuration = serviceInterface
        .getMethod("getConfiguration")
        .invoke(activityManagerService) as Configuration

    configuration.setLocales(localeList)
    runCatching {
        Configuration::class.java.getField("userSetLocale").setBoolean(configuration, true)
    }

    val updater = serviceInterface.methods.firstOrNull { method ->
        (method.name == "updatePersistentConfiguration" ||
            method.name == "updatePersistentConfigurationWithAttribution") &&
            method.parameterTypes.firstOrNull() == Configuration::class.java
    } ?: return@runCatching false

    when (updater.parameterTypes.size) {
        1 -> updater.invoke(activityManagerService, configuration)
        3 -> updater.invoke(activityManagerService, configuration, context.packageName, null)
        else -> return@runCatching false
    }
    true
}.getOrDefault(false)
