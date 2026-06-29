package com.android.setupwizard.ui.navigation

import android.content.Context
import android.provider.Settings
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Gesture
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.setupwizard.ui.locale.LocalStrings
import kotlin.math.min

/**
 * 第六屏（倒数第二屏）：系统导航模式（完全居左 / Android 12+ 现代布局）。
 *
 * 区域 B 的演示动画是从 AOSP 官方 Lottie（lottie_system_nav_*）中提取矢量路径 / 关键帧后，
 * 用纯 Compose `Canvas` 数学手搓 1:1 复刻 —— 杜绝任何 Lottie / 第三方动画依赖。三种模式各一段，
 * 由单个 `Animatable<Float>` 时间轴（rememberInfiniteTransition）驱动，t 仅在 `Canvas` 的 onDraw
 * 内被读取，做到 120Hz 下零重组（Zero Recomposition）。模式切换借 `Crossfade` 平滑过渡。
 *
 * 选中模式时执行 [applyNavigationMode] 特权写入：独占启用对应 navbar RRO Overlay，物理级瞬时换栏，
 * 全程 runCatching 静默降级（非特权安装仅更新自身 UI）。
 */
@Composable
fun NavigationScreen(
    onBack: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val strings = LocalStrings.current

    // 双键（2-button）为 Android 9 旧模式，部分 ROM 已裁掉其 overlay；探测一次决定该卡是否可选。
    val twoButtonAvailable = remember { isOverlayAvailable(NavMode.TwoButton.overlayPackage) }

    // 初始选中态读自只读镜像 Settings.Secure.navigation_mode（SystemUI 始终保持其同步）。
    var selectedMode by rememberSaveable { mutableStateOf(currentNavMode(context)) }

    // 选择即乐观更新自身 UI，再借 android.uid.system 特权独占翻转对应 navbar overlay。
    val onSelectMode: (NavMode) -> Unit = { mode ->
        selectedMode = mode
        applyNavigationMode(context, mode)
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
            // 可滚动主体（区域 A + B），底部双按钮（区域 C）常驻不随滚动
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start,
            ) {
                // 区域 A: 顶部导航图标与标题（居左）
                Spacer(modifier = Modifier.height(8.dp))

                Icon(
                    imageVector = Icons.Outlined.Gesture,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(48.dp),
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = strings.navTitle,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.5).sp,
                    ),
                    color = colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = strings.navSubtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(28.dp))

                // 区域 B 上半部分: 动态 Canvas 演示区（按当前选中模式渲染，Crossfade 平滑切换）
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .clip(RoundedCornerShape(28.dp)),
                    color = colorScheme.surfaceContainerHighest,
                ) {
                    Crossfade(
                        targetState = selectedMode,
                        animationSpec = tween(durationMillis = 360),
                        label = "nav-demo",
                    ) { mode ->
                        when (mode) {
                            NavMode.Gestural -> GestureNavAnimation(modifier = Modifier.fillMaxSize())
                            NavMode.TwoButton -> TwoButtonNavAnimation(modifier = Modifier.fillMaxSize())
                            NavMode.ThreeButton -> ThreeButtonNavAnimation(modifier = Modifier.fillMaxSize())
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 区域 B 下半部分: 三张靠左单选卡片（role=RadioButton）
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    NavModeCard(
                        title = strings.navGesturalTitle,
                        description = strings.navGesturalDesc,
                        selected = selectedMode == NavMode.Gestural,
                        enabled = true,
                        onSelect = { onSelectMode(NavMode.Gestural) },
                    )
                    NavModeCard(
                        title = strings.navTwoButtonTitle,
                        // overlay 不存在时禁用本卡并改述为「此设备不支持」
                        description = if (twoButtonAvailable) {
                            strings.navTwoButtonDesc
                        } else {
                            strings.navUnavailable
                        },
                        selected = selectedMode == NavMode.TwoButton,
                        enabled = twoButtonAvailable,
                        onSelect = { onSelectMode(NavMode.TwoButton) },
                    )
                    NavModeCard(
                        title = strings.navThreeButtonTitle,
                        description = strings.navThreeButtonDesc,
                        selected = selectedMode == NavMode.ThreeButton,
                        enabled = true,
                        onSelect = { onSelectMode(NavMode.ThreeButton) },
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // 区域 C: 底部双按钮导航栏
            Spacer(modifier = Modifier.height(16.dp))

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

/** 导航模式单选卡片：选中时高亮描边 + secondaryContainer 底色；不可用时降透明度并禁用点选。 */
@Composable
private fun NavModeCard(
    title: String,
    description: String,
    selected: Boolean,
    enabled: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme

    val containerColor by animateColorAsState(
        targetValue = if (selected) colorScheme.secondaryContainer else colorScheme.surfaceContainerLow,
        animationSpec = tween(durationMillis = 220),
        label = "nav-card-bg",
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) colorScheme.primary else Color.Transparent,
        animationSpec = tween(durationMillis = 220),
        label = "nav-card-border",
    )
    val contentColor = if (selected) colorScheme.onSecondaryContainer else colorScheme.onSurface

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.45f)
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onSelect,
            ),
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        contentColor = contentColor,
        border = BorderStroke(2.dp, borderColor),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
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

            Spacer(modifier = Modifier.width(12.dp))

            RadioButton(
                selected = selected,
                onClick = null, // 点选语义由外层 selectable 承载
                enabled = enabled,
            )
        }
    }
}

/* ============================================================================
 *  纯 Canvas 手搓动画 —— AOSP Lottie 几何 / 关键帧的数学复刻
 *  画布原始基准 412 x 300 @60fps；所有坐标归一化到 0..1 of DrawScope size，自适配任意尺寸。
 * ========================================================================== */

// 取自真实关键帧手柄：标准缓动 + 手势对称缓动
private val StandardEasing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
private val SymmetricEasing = CubicBezierEasing(0.5f, 0f, 0.5f, 1f)

// 手指圆点半径（直径 26px / 412 ≈ 0.0316·W）
private const val FINGER_RADIUS = 0.0316f

/** 手指 / 涟漪的不透明度三角包络：快入(0..0.22)→保持(..0.70)→淡出(..1.0)。 */
private fun flashAlpha(local: Float): Float {
    val v = when {
        local < 0.22f -> local / 0.22f
        local < 0.70f -> 1f
        else -> 1f - (local - 0.70f) / 0.30f
    }
    return v.coerceIn(0f, 1f)
}

private fun lerpF(a: Float, b: Float, t: Float): Float = a + (b - a) * t

/** 共享设备机身 + 屏幕（三动画复用，杜绝切换跳变）。机身→surfaceVariant，屏幕→surface。 */
private fun DrawScope.drawDeviceMock(frameColor: Color, screenColor: Color) {
    val w = size.width
    val h = size.height
    val rmin = min(w, h)
    // 机身圆角矩形 topLeft(0.344,0.085) size(0.312,0.830)
    drawRoundRect(
        color = frameColor,
        topLeft = Offset(0.344f * w, 0.085f * h),
        size = Size(0.312f * w, 0.830f * h),
        cornerRadius = CornerRadius(rmin * 0.05f),
    )
    // 内屏视口 topLeft(0.356,0.100) size(0.288,0.800)
    drawRoundRect(
        color = screenColor,
        topLeft = Offset(0.356f * w, 0.100f * h),
        size = Size(0.288f * w, 0.800f * h),
        cornerRadius = CornerRadius(rmin * 0.04f),
    )
}

/** 返回箭头雪佛龙「‹」。 */
private fun DrawScope.drawChevron(center: Offset, color: Color, rmin: Float) {
    val hw = rmin * 0.011f
    val hh = rmin * 0.015f
    val path = Path().apply {
        moveTo(center.x + hw, center.y - hh)
        lineTo(center.x - hw, center.y)
        lineTo(center.x + hw, center.y + hh)
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = rmin * 0.008f, cap = StrokeCap.Round, join = StrokeJoin.Round),
    )
}

/** 最近任务空心圆角方块。 */
private fun DrawScope.drawSquareOutline(center: Offset, color: Color, rmin: Float) {
    val s = rmin * 0.020f
    drawRoundRect(
        color = color,
        topLeft = Offset(center.x - s / 2f, center.y - s / 2f),
        size = Size(s, s),
        cornerRadius = CornerRadius(rmin * 0.004f),
        style = Stroke(width = rmin * 0.006f, join = StrokeJoin.Round),
    )
}

/** 手指拖动：淡出拖尾 + 实心圆点。 */
private fun DrawScope.drawFingerDrag(start: Offset, current: Offset, color: Color, rmin: Float, alpha: Float) {
    drawLine(
        color = color.copy(alpha = alpha * 0.30f),
        start = start,
        end = current,
        strokeWidth = size.width * 0.014f,
        cap = StrokeCap.Round,
    )
    drawCircle(color = color.copy(alpha = alpha), radius = rmin * FINGER_RADIUS, center = current)
}

/** 按压涟漪：随 local 扩张的半透明圆。 */
private fun DrawScope.drawTapRipple(center: Offset, color: Color, rmin: Float, local: Float) {
    drawCircle(
        color = color.copy(alpha = flashAlpha(local) * 0.28f),
        radius = rmin * FINGER_RADIUS * (1f + 0.5f * local),
        center = center,
    )
}

/**
 * ① 全屏手势：底部细手柄 + 两段循环（上滑回主屏 → 边缘内滑返回）。
 * 复刻 lottie_system_nav_fully_gestural：home swipe-up (0.470 | y0.889→0.432)、
 * back edge-swipe (y0.583 | x0.419→0.615)。
 */
@Composable
private fun GestureNavAnimation(modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    val transition = rememberInfiniteTransition(label = "nav-gestural")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "gestural-t",
    )

    Canvas(modifier = modifier) {
        drawDeviceMock(colorScheme.surfaceVariant, colorScheme.surface)
        val w = size.width
        val h = size.height
        val rmin = min(w, h)

        // 手柄药丸 center(0.501,0.886) size(0.053,~0.012 放大以可见)
        val pillW = 0.053f * w
        val pillH = 0.012f * h
        drawRoundRect(
            color = colorScheme.outline,
            topLeft = Offset(0.501f * w - pillW / 2f, 0.886f * h - pillH / 2f),
            size = Size(pillW, pillH),
            cornerRadius = CornerRadius(pillH / 2f),
        )

        if (t < 0.5f) {
            // 段 A：home 上滑
            val local = t / 0.5f
            val a = SymmetricEasing.transform(local)
            val start = Offset(0.470f * w, 0.889f * h)
            val current = Offset(0.470f * w, lerpF(0.889f, 0.432f, a) * h)
            drawFingerDrag(start, current, colorScheme.primary, rmin, flashAlpha(local))
        } else {
            // 段 B：边缘内滑返回
            val local = (t - 0.5f) / 0.5f
            val b = SymmetricEasing.transform(local)
            val start = Offset(0.419f * w, 0.583f * h)
            val current = Offset(lerpF(0.419f, 0.615f, b) * w, 0.583f * h)
            drawFingerDrag(start, current, colorScheme.primary, rmin, flashAlpha(local))
        }
    }
}

/**
 * ② 双按钮：返回雪佛龙 + 主屏药丸；轻触返回 → 招牌横向快切（滑动主屏药丸切换应用）。
 * 复刻 lottie_system_nav_2_button：back(0.420,0.883)、home pill(0.500,0.883)、
 * quick-switch swipe (y0.583 | x0.419→0.615)。
 */
@Composable
private fun TwoButtonNavAnimation(modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    val transition = rememberInfiniteTransition(label = "nav-twobutton")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "twobutton-t",
    )

    Canvas(modifier = modifier) {
        drawDeviceMock(colorScheme.surfaceVariant, colorScheme.surface)
        val w = size.width
        val h = size.height
        val rmin = min(w, h)
        val y = 0.883f * h

        val backCenter = Offset(0.420f * w, y)
        drawChevron(backCenter, colorScheme.primary, rmin)

        // 主屏药丸 center(0.500,0.883)
        val pw = 0.031f * w
        val ph = 0.013f * h
        drawRoundRect(
            color = colorScheme.primary,
            topLeft = Offset(0.500f * w - pw / 2f, y - ph / 2f),
            size = Size(pw, ph),
            cornerRadius = CornerRadius(ph / 2f),
        )

        if (t < 0.5f) {
            // 段 A：轻触返回键
            drawTapRipple(backCenter, colorScheme.primary, rmin, t / 0.5f)
        } else {
            // 段 B：横向快切（最具辨识度，重点打磨）
            val local = (t - 0.5f) / 0.5f
            val b = SymmetricEasing.transform(local)
            val start = Offset(0.419f * w, 0.583f * h)
            val current = Offset(lerpF(0.419f, 0.615f, b) * w, 0.583f * h)
            drawFingerDrag(start, current, colorScheme.primary, rmin, flashAlpha(local))
        }
    }
}

/**
 * ③ 三按钮：经典返回 / 主屏 / 最近任务三键；按压涟漪依次轮播三键。
 * 复刻 lottie_system_nav_3_button：navbar y0.878，back(0.422)、home(0.499)、recents(0.577)。
 */
@Composable
private fun ThreeButtonNavAnimation(modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    val transition = rememberInfiniteTransition(label = "nav-threebutton")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "threebutton-t",
    )

    Canvas(modifier = modifier) {
        drawDeviceMock(colorScheme.surfaceVariant, colorScheme.surface)
        val w = size.width
        val h = size.height
        val rmin = min(w, h)
        val y = 0.878f * h

        val backCenter = Offset(0.422f * w, y)
        val homeCenter = Offset(0.499f * w, y)
        val recentsCenter = Offset(0.577f * w, y)

        // 三键常驻
        drawChevron(backCenter, colorScheme.primary, rmin)
        drawCircle(color = colorScheme.primary, radius = rmin * 0.012f, center = homeCenter)
        drawSquareOutline(recentsCenter, colorScheme.primary, rmin)

        // 三拍轮播按压涟漪：back → home → recents
        val beat = (t * 3f).toInt().coerceIn(0, 2)
        val local = (t * 3f) - beat
        val active = when (beat) {
            0 -> backCenter
            1 -> homeCenter
            else -> recentsCenter
        }
        drawTapRipple(active, colorScheme.primary, rmin, local)
    }
}

/* ============================================================================
 *  系统导航模式特权切换 —— 独占启用 navbar RRO Overlay（android.uid.system 平台签名）
 *  机制与 AOSP Settings.SystemNavigationGestureSettings 完全一致：
 *  IOverlayManager.setEnabledExclusiveInCategory(pkg, userId)。SystemUI 收到
 *  ACTION_OVERLAY_CHANGED 后一两帧内实时换栏，无需重启。
 *  Settings.Secure.navigation_mode 为派生只读镜像：仅作初始态读取与兜底回写。
 * ========================================================================== */

/** 三种导航模式：携带各自的 navbar overlay 包名与只读镜像值。 */
private enum class NavMode(val overlayPackage: String, val secureValue: Int) {
    Gestural("com.android.internal.systemui.navbar.gestural", 2),
    TwoButton("com.android.internal.systemui.navbar.twobutton", 1),
    ThreeButton("com.android.internal.systemui.navbar.threebutton", 0),
}

// Settings.Secure.NAVIGATION_MODE 为 @hide 常量，按字面量引用其键名
private const val SECURE_NAVIGATION_MODE = "navigation_mode"

/** 读 Settings.Secure.navigation_mode 映射为初始选中模式；缺省 / 异常回退全屏手势。 */
private fun currentNavMode(context: Context): NavMode = runCatching {
    when (Settings.Secure.getInt(context.contentResolver, SECURE_NAVIGATION_MODE, 2)) {
        0 -> NavMode.ThreeButton
        1 -> NavMode.TwoButton
        else -> NavMode.Gestural
    }
}.getOrDefault(NavMode.Gestural)

/**
 * 应用导航模式：先独占翻转 overlay（权威开关），再尽力回写只读镜像（独立守卫，失败静默）。
 * 返回 overlay 翻转是否成功；非特权 / overlay 缺失时静默返回 false，UI 已乐观更新故不受影响。
 */
private fun applyNavigationMode(context: Context, mode: NavMode): Boolean {
    val flipped = setNavOverlayExclusive(mode.overlayPackage)
    // 兜底：保持读侧镜像即时一致（SystemUI 随后也会自行回写）
    runCatching {
        Settings.Secure.putInt(context.contentResolver, SECURE_NAVIGATION_MODE, mode.secureValue)
    }
    return flipped
}

/** 反射 IOverlayManager.setEnabledExclusiveInCategory(pkg, userId)，整体 runCatching 静默降级。 */
private fun setNavOverlayExclusive(overlayPackage: String): Boolean = runCatching {
    val userId = currentUserId()
    val binder = Class.forName("android.os.ServiceManager")
        .getMethod("getService", String::class.java)
        .invoke(null, "overlay") ?: return@runCatching false // Context.OVERLAY_SERVICE == "overlay"

    val iBinderClass = Class.forName("android.os.IBinder")
    val overlayManager = Class.forName("android.content.om.IOverlayManager\$Stub")
        .getMethod("asInterface", iBinderClass)
        .invoke(null, binder) ?: return@runCatching false

    Class.forName("android.content.om.IOverlayManager")
        .getMethod("setEnabledExclusiveInCategory", String::class.java, Int::class.javaPrimitiveType)
        .invoke(overlayManager, overlayPackage, userId)
    true
}.getOrDefault(false)

/** 探测某 overlay 是否存在（getOverlayInfo 非空即存在），用于决定双键卡是否可选。 */
private fun isOverlayAvailable(overlayPackage: String): Boolean = runCatching {
    val userId = currentUserId()
    val binder = Class.forName("android.os.ServiceManager")
        .getMethod("getService", String::class.java)
        .invoke(null, "overlay") ?: return@runCatching false

    val iBinderClass = Class.forName("android.os.IBinder")
    val overlayManager = Class.forName("android.content.om.IOverlayManager\$Stub")
        .getMethod("asInterface", iBinderClass)
        .invoke(null, binder) ?: return@runCatching false

    val info = Class.forName("android.content.om.IOverlayManager")
        .getMethod("getOverlayInfo", String::class.java, Int::class.javaPrimitiveType)
        .invoke(overlayManager, overlayPackage, userId)
    info != null
}.getOrDefault(false)

/** 当前用户：优先 USER_CURRENT(-2)（由服务解析为前台用户），回退 myUserId，再回退 0。 */
private fun currentUserId(): Int = runCatching {
    Class.forName("android.os.UserHandle").getField("USER_CURRENT").getInt(null)
}.getOrElse {
    runCatching {
        Class.forName("android.os.UserHandle").getMethod("myUserId").invoke(null) as Int
    }.getOrDefault(0)
}
