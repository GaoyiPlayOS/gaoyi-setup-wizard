package com.android.setupwizard.ui.agreement

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.android.setupwizard.ui.locale.LocalStrings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

/** 协议加载的三态状态机：加载中 / 网络时效版成功 / 失败回退内置版。 */
private sealed interface TosState {
    data object Loading : TosState
    data class Success(val text: String) : TosState
    data class Error(val text: String) : TosState
}

@Composable
fun AgreementScreen(
    selectedLocale: Locale,
    onNext: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()

    var tosState by remember { mutableStateOf<TosState>(TosState.Loading) }
    var showDisagreeDialog by remember { mutableStateOf(false) }
    var moeUnlocked by remember { mutableStateOf(false) }
    var titleTapCount by remember { mutableIntStateOf(0) }

    val scrollState = rememberScrollState()

    // 仅在协议正文已就绪且滚动到底部时，方可激活「我同意」。内容短于视口时 maxValue==0，视为已读毕。
    val reachedBottom by remember {
        derivedStateOf {
            scrollState.maxValue == 0 || scrollState.value >= scrollState.maxValue
        }
    }
    val canAgree = tosState !is TosState.Loading && reachedBottom

    // 进入页面：联网则拉取时效版，否则/失败回退内置版（空内容回退英文）。
    LaunchedEffect(selectedLocale) {
        tosState = TosState.Loading
        val assetKey = tosAssetKey(selectedLocale)
        val fresh = if (isOnline(context)) {
            withContext(Dispatchers.IO) { fetchTos(tosUrl(assetKey)) }
        } else {
            null
        }
        tosState = if (!fresh.isNullOrBlank()) {
            TosState.Success(fresh)
        } else {
            TosState.Error(withContext(Dispatchers.IO) { loadBundledTos(context, assetKey) })
        }
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
            // 区域 A: 顶部盾牌图标与标题（居左）
            Spacer(modifier = Modifier.height(8.dp))

            Icon(
                imageVector = Icons.Outlined.Shield,
                contentDescription = null,
                tint = colorScheme.primary,
                modifier = Modifier.size(48.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = strings.userAgreement,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.5).sp,
                ),
                color = colorScheme.onSurface,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    // 萌化彩蛋：仅简体中文下，连点标题十次切换萌化版协议。
                    if (!moeUnlocked && selectedLocale.isSimplifiedChinese()) {
                        titleTapCount++
                        if (titleTapCount >= MOE_TAP_THRESHOLD) {
                            moeUnlocked = true
                            scope.launch {
                                val moe = withContext(Dispatchers.IO) {
                                    loadBundledTos(context, TOS_KEY_MOE)
                                }
                                tosState = TosState.Success(moe)
                                Toast.makeText(context, strings.moeToast, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 区域 B: 协议正文容器（weight 占满中部）+ 加载态机
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(28.dp),
                color = colorScheme.surfaceContainerLow,
                contentColor = colorScheme.onSurface,
            ) {
                Crossfade(targetState = tosState, label = "tos-content") { state ->
                    when (state) {
                        TosState.Loading -> AgreementLoading()
                        is TosState.Success -> AgreementBody(state.text, scrollState)
                        is TosState.Error -> AgreementBody(state.text, scrollState)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 区域 C: 底部双按钮导航栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(
                    onClick = { showDisagreeDialog = true },
                    shape = RoundedCornerShape(24.dp),
                ) {
                    Text(
                        text = strings.disagree,
                        fontWeight = FontWeight.Medium,
                    )
                }

                FilledTonalButton(
                    onClick = onNext,
                    enabled = canAgree,
                    shape = RoundedCornerShape(24.dp),
                ) {
                    Text(
                        text = strings.agree,
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

    if (showDisagreeDialog) {
        DisagreeDialog(
            onBack = {
                showDisagreeDialog = false
                onBack()
            },
            onKeepReading = { showDisagreeDialog = false },
            onPowerOff = { shutdownDevice(context) },
        )
    }
}

@Composable
private fun AgreementLoading() {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color = colorScheme.primary,
            trackColor = colorScheme.primary.copy(alpha = 0.2f),
            strokeCap = StrokeCap.Round,
        )
    }
}

@Composable
private fun AgreementBody(text: String, scrollState: ScrollState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 20.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DisagreeDialog(
    onBack: () -> Unit,
    onKeepReading: () -> Unit,
    onPowerOff: () -> Unit,
) {
    val strings = LocalStrings.current
    val colorScheme = MaterialTheme.colorScheme

    AlertDialog(
        // 不可点击空白处 / 返回键关闭。
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
        icon = {
            Icon(
                imageVector = Icons.Outlined.WarningAmber,
                contentDescription = null,
                tint = colorScheme.primary,
            )
        },
        title = { Text(text = strings.disagreeTitle) },
        text = { Text(text = strings.disagreeMessage) },
        confirmButton = {
            Column(horizontalAlignment = Alignment.End) {
                TextButton(onClick = onKeepReading) {
                    Text(text = strings.disagreeKeepReading)
                }
                TextButton(onClick = onBack) {
                    Text(text = strings.disagreeBack)
                }
                TextButton(onClick = onPowerOff) {
                    Text(
                        text = strings.disagreePowerOff,
                        color = colorScheme.error,
                    )
                }
            }
        },
    )
}

/* ---------- 状态便捷封装 ---------- */

@Composable
private fun rememberStateBoolean() = remember { mutableStateOf(false) }

/* ---------- 协议语种解析 ---------- */

private const val TOS_BASE_URL = "https://gaoyi.cn-nb1.rains3.com/internal/docs/release/tos"
private const val TOS_KEY_FALLBACK = "en_US"
private const val TOS_KEY_MOE = "zh_MOE"
private const val MOE_TAP_THRESHOLD = 10

private val TraditionalChineseRegions = setOf("TW", "HK", "MO")

private fun Locale.isTraditionalChinese(): Boolean {
    if (language != "zh") return false
    if (script.equals("Hant", ignoreCase = true)) return true
    if (script.equals("Hans", ignoreCase = true)) return false
    return country.uppercase(Locale.ROOT) in TraditionalChineseRegions
}

private fun Locale.isSimplifiedChinese(): Boolean =
    language == "zh" && !isTraditionalChinese()

/** Locale → 协议资源键（与 assets/tos/<key>.txt 及远端 <key>.txt 对应）。 */
private fun tosAssetKey(locale: Locale): String = when (locale.language) {
    "zh" -> if (locale.isTraditionalChinese()) "zh_TW" else "zh_CN"
    "ja" -> "ja_JP"
    "ko" -> "ko_KR"
    "ru" -> "ru_RU"
    else -> TOS_KEY_FALLBACK
}

private fun tosUrl(assetKey: String): String = "$TOS_BASE_URL/$assetKey.txt"

/* ---------- 网络与资源读取（零三方依赖） ---------- */

/** 通过 public ConnectivityManager 判定是否具备已校验的互联网连接。 */
private fun isOnline(context: Context): Boolean = runCatching {
    val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = manager.activeNetwork ?: return@runCatching false
    val capabilities = manager.getNetworkCapabilities(network) ?: return@runCatching false
    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}.getOrDefault(false)

/** 用 HttpURLConnection 拉取时效版协议；任何异常或非 200 返回 null。须在 IO 线程调用。 */
private fun fetchTos(url: String): String? = runCatching {
    val connection = (URL(url).openConnection() as HttpURLConnection).apply {
        connectTimeout = 5_000
        readTimeout = 7_000
        requestMethod = "GET"
        setRequestProperty("Accept-Charset", "UTF-8")
    }
    try {
        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } else {
            null
        }
    } finally {
        connection.disconnect()
    }
}.getOrNull()

/** 读取打包的内置协议；目标语种为空时回退英文。须在 IO 线程调用。 */
private fun loadBundledTos(context: Context, assetKey: String): String {
    val primary = readAsset(context, "tos/$assetKey.txt")
    return primary.ifBlank { readAsset(context, "tos/$TOS_KEY_FALLBACK.txt") }
}

private fun readAsset(context: Context, path: String): String = runCatching {
    context.assets.open(path).bufferedReader(Charsets.UTF_8).use { it.readText() }
}.getOrDefault("")

/* ---------- 特权关机 ---------- */

/**
 * 借助 android.uid.system 特权，反射调用隐藏的 PowerManager#shutdown 实现真正关机。
 * 非特权运行时静默降级（兜底容错），与 LanguageScreen#applySystemLocale 同理。
 */
private fun shutdownDevice(context: Context): Boolean = runCatching {
    val powerManager = context.getSystemService(Context.POWER_SERVICE)
        ?: return@runCatching false
    val shutdown = powerManager.javaClass.getMethod(
        "shutdown",
        Boolean::class.javaPrimitiveType,
        String::class.java,
        Boolean::class.javaPrimitiveType,
    )
    shutdown.invoke(powerManager, false, "GaoyiPlayOS: user declined agreement", false)
    true
}.getOrDefault(false)
