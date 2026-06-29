package com.android.setupwizard.ui.finish

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.graphics.RectF
import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.PathParser
import com.android.setupwizard.SetupWizardActivity
import com.android.setupwizard.navigation.NiriCloseDurationMillis
import com.android.setupwizard.navigation.NiriCloseEasing
import com.android.setupwizard.ui.locale.LocalStrings
import kotlinx.coroutines.launch
import kotlin.math.max

/**
 * 第七屏（末屏）：设置完成页 —— 视觉上的绝对空灵与高信噪比。
 *
 * 区域 A：屏幕绝对中心，仅渲染 GaoyiPlayOS Logo（Google「G」几何），无任何文字标题。
 * Logo 的两段路径在 [rememberLogoMark] 中由 `PathParser` 解析 tmp/GyPOS.svg 的矢量 `d`，
 * **彻底丢弃 SVG 内写死的 #4285F4 / #FBBC05**，改由 `colorScheme.primary` 与
 * `colorScheme.tertiary` 动态填充 —— 随 Monet 主题色实时变化。
 *
 * 区域 B：底部 Row，右下角放置进入系统的 Action 按钮（[LetsGoButton]）。
 * 点击执行特权移交（[finalizeProvisioning] → [disableSelfComponent] → [launchHomeLauncher]
 * → onSetupFinished），整页以 250ms Niri 关闭曲线「吸入式」淡出缩小；长按则把按钮文案
 * **永久**替换为字符画彩蛋 `٩(ˊᗜˋ*)و`（不随系统语言变化、不回退）。
 */
@Composable
fun FinishScreen(
    onSetupFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()

    val logo = rememberLogoMark()

    // 入场：整页轻微放大 + 淡入（一次性，graphicsLayer 延迟读取，零重组）。
    val entrance = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        entrance.animateTo(1f, animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing))
    }

    // 退场：250ms Niri 关闭曲线，整页淡出 + 缩小，驱动 graphicsLayer。
    val exit = remember { Animatable(0f) }

    // 彩蛋：长按一次即永久翻面，绝不回退（rememberSaveable 跨配置变更亦保持）。
    var eggRevealed by rememberSaveable { mutableStateOf(false) }

    // 防重入：移交流程仅允许触发一次。
    var handoverStarted by remember { mutableStateOf(false) }

    val onEnterSystem: () -> Unit = {
        if (!handoverStarted) {
            handoverStarted = true
            scope.launch {
                // 1) 先行宣告 OOBE 彻底完成（瞬时写入，无视觉副作用）。
                finalizeProvisioning(context)
                // 2) 整页吸入式淡出（Niri：250ms / cubic-bezier(0.3,0,0.8,0.15)）。
                exit.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = NiriCloseDurationMillis, easing = NiriCloseEasing),
                )
                // 3) 退役向导自身组件 → 平滑拉起默认 Launcher → 关闭向导 Activity。
                disableSelfComponent(context)
                launchHomeLauncher(context)
                onSetupFinished()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colorScheme.surface)
            .graphicsLayer {
                val intro = entrance.value
                val e = exit.value
                alpha = intro * (1f - e)
                val s = (0.94f + 0.06f * intro) * (1f - 0.08f * e)
                scaleX = s
                scaleY = s
            },
    ) {
        // 区域 A：绝对中心 Logo，无文字。
        LogoMark(
            primaryPath = logo.primary,
            tertiaryPath = logo.tertiary,
            primaryColor = colorScheme.primary,
            tertiaryColor = colorScheme.tertiary,
            modifier = Modifier
                .align(Alignment.Center)
                .size(176.dp),
        )

        // 区域 B：底部 Row，右下角对齐的进入系统按钮。
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LetsGoButton(
                // 长按后永久显示彩蛋字符画；否则走 Localization 多语言路由。
                label = if (eggRevealed) EGG_KAOMOJI else strings.letsGo,
                onClick = onEnterSystem,
                onLongClick = { eggRevealed = true },
            )
        }
    }
}

/** 右下角进入系统按钮：自定义 Surface 样式 + combinedClickable（承载点击 / 长按彩蛋）。 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LetsGoButton(
    label: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = modifier.combinedClickable(
            interactionSource = interactionSource,
            indication = ripple(),
            role = Role.Button,
            onClick = onClick,
            onLongClick = onLongClick,
        ),
        shape = RoundedCornerShape(24.dp),
        color = colorScheme.primary,
        contentColor = colorScheme.onPrimary,
    ) {
        Row(
            modifier = Modifier.padding(start = 28.dp, end = 24.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.width(10.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
            )
        }
    }
}

/** 居中绘制 Logo：单位空间 [0,1] 的两段路径，缩放贴合到画布最小边的居中正方形，分别动态上色。 */
@Composable
private fun LogoMark(
    primaryPath: Path,
    tertiaryPath: Path,
    primaryColor: Color,
    tertiaryColor: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val side = size.minDimension
        val left = (size.width - side) / 2f
        val top = (size.height - side) / 2f
        withTransform({
            translate(left, top)
            scale(side, side, pivot = Offset.Zero)
        }) {
            drawPath(path = primaryPath, color = primaryColor)
            drawPath(path = tertiaryPath, color = tertiaryColor)
        }
    }
}

/** 两段已归一化（单位空间 [0,1]）的 Compose 路径：primary 组 + tertiary 组。 */
private class LogoMark(val primary: Path, val tertiary: Path)

/**
 * 解析 GyPOS.svg 的四条矢量路径并合并为两组（svg_1+svg_2 → primary，svg_3+svg_4 → tertiary），
 * 计算两组并集包围盒后，用 [Matrix] 等比缩放居中归一化到单位正方形 [0,1]×[0,1]。
 * 仅在首次组合时计算一次（remember 无 key），此后随主题切换仅重新填色、不重新解析。
 */
@Composable
private fun rememberLogoMark(): LogoMark = remember {
    val primaryAndroid = android.graphics.Path().apply {
        addPath(PathParser.createPathFromPathData(LOGO_PATH_1))
        addPath(PathParser.createPathFromPathData(LOGO_PATH_2))
    }
    val tertiaryAndroid = android.graphics.Path().apply {
        addPath(PathParser.createPathFromPathData(LOGO_PATH_3))
        addPath(PathParser.createPathFromPathData(LOGO_PATH_4))
    }

    // 两组并集包围盒（原始 SVG 1000×1000 坐标系）。
    val bounds = RectF().also { r ->
        android.graphics.Path().apply {
            addPath(primaryAndroid)
            addPath(tertiaryAndroid)
        }.computeBounds(r, true)
    }

    // 等比缩放：较长边映射到 1.0；居中对齐到单位正方形。
    val span = max(bounds.width(), bounds.height())
    val s = if (span > 0f) 1f / span else 1f
    val offX = (1f - bounds.width() * s) / 2f
    val offY = (1f - bounds.height() * s) / 2f
    val matrix = Matrix().apply {
        postTranslate(-bounds.left, -bounds.top)
        postScale(s, s)
        postTranslate(offX, offY)
    }
    primaryAndroid.transform(matrix)
    tertiaryAndroid.transform(matrix)

    LogoMark(primaryAndroid.asComposePath(), tertiaryAndroid.asComposePath())
}

/* ============================================================================
 *  系统接管 —— OOBE 完成宣告与主权移交（android.uid.system / 平台签名）
 *  全程 runCatching 静默降级：非特权安装仅本地无副作用地走完动画并关闭自身。
 * ========================================================================== */

// Settings.Secure.USER_SETUP_COMPLETE 为 @hide 常量，按字面量引用其键名（与 NavigationScreen 同例）。
private const val SECURE_USER_SETUP_COMPLETE = "user_setup_complete"

/** 向系统宣告 OOBE 彻底完成：device_provisioned + user_setup_complete 均置 1。 */
private fun finalizeProvisioning(context: Context) {
    val resolver = context.contentResolver
    runCatching {
        Settings.Global.putInt(resolver, Settings.Global.DEVICE_PROVISIONED, 1)
    }
    runCatching {
        Settings.Secure.putInt(resolver, SECURE_USER_SETUP_COMPLETE, 1)
    }
}

/** 退役向导自身：禁用本 Activity 组件（DONT_KILL_APP，待移交完成后随任务自然回收）。 */
private fun disableSelfComponent(context: Context) {
    runCatching {
        context.packageManager.setComponentEnabledSetting(
            ComponentName(context, SetupWizardActivity::class.java),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP,
        )
    }
}

/** 平滑拉起默认 Launcher 桌面（ACTION_MAIN + CATEGORY_HOME，新任务栈）。 */
private fun launchHomeLauncher(context: Context) {
    runCatching {
        val home = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(home)
    }
}

/* ============================================================================
 *  彩蛋与素材常量
 * ========================================================================== */

// 字符画彩蛋：长按按钮永久显示，刻意脱离 Localization 多语言路由（任何语言下都一致）。
private const val EGG_KAOMOJI = "٩(ˊᗜˋ*)و"

// GaoyiPlayOS Logo 的四条矢量路径（取自 tmp/GyPOS.svg，颜色已剥离）。
// svg_1 + svg_2 → primary 组；svg_3 + svg_4 → tertiary 组。
private const val LOGO_PATH_1 =
    "m999.42207,511.01477c0,-41.0461 -3.4003,-70.98027 -10.69962,-102.04831l-477.85585,0l0," +
        "185.18366l280.45695,0c-5.62183,45.98977 -36.13388,115.29191 -104.00392,161.8713l-0.95208," +
        "6.16825l151.06411,114.74765l10.42759,0.99781c96.20589,-86.99051 151.56282,-215.07248 " +
        "151.56282,-366.92036z"
private const val LOGO_PATH_2 =
    "m510.91194,998.80569c137.37222,0 252.71048,-44.357 336.99265,-120.87055l-160.58495," +
        "-121.91371c-42.97982,29.38991 -100.64895,49.89028 -176.40769,49.89028a305.80054,305.91815 " +
        "0 0 1 -289.52442,-207.36216l-5.98453,0.4989l-157.09398,119.19242l-2.04018,5.624c83.69278," +
        "162.95981 255.61207,274.94082 454.64311,274.94082z"
private const val LOGO_PATH_3 =
    "m221.43286,598.54954a301.49349,301.60945 0 0 1 -17.09219,-98.6467c0,-34.37894 6.25656," +
        "-67.62401 16.36679,-98.6467l-0.27202,-6.66716l-159.04348,-121.09733l-5.2138,2.44916a491.04902," +
        "491.23788 0 0 0 -54.17815,223.96202c0,80.36871 19.76709,156.33801 54.26883,223.96202l165.11869," +
        "-125.31532l0.04534,0z"
private const val LOGO_PATH_4 =
    "m510.91194,193.89398c95.57117,0 159.99557,40.45649 196.76417,74.29117l143.58344,-137.47041c-88.18118," +
        "-80.36871 -202.97539,-129.71474 -340.34761,-129.71474c-199.07638,0 -370.95034,111.98101 " +
        "-454.64311,274.94082l164.57464,125.31532a306.93397,307.05202 0 0 1 290.06847,-207.36216z"
