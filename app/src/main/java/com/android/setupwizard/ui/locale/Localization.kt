package com.android.setupwizard.ui.locale

import androidx.compose.runtime.staticCompositionLocalOf
import java.util.Locale

/* 字符串定义 */
data class Strings(
    val welcomeTo: String,
    val accessibility: String,
    val start: String,
    val setLanguage: String,
    val change: String,
    val continueLabel: String,
    val selectLanguage: String,
    val selected: String,
)

/* English (US) */
private val English = Strings(
    welcomeTo = "Welcome to",
    accessibility = "Accessibility",
    start = "START",
    setLanguage = "Set Language",
    change = "Change",
    continueLabel = "CONTINUE",
    selectLanguage = "Select Language",
    selected = "Selected",
)

/* 日文 */
private val Japanese = Strings(
    welcomeTo = "ようこそ",
    accessibility = "ユーザー補助",
    start = "開始",
    setLanguage = "言語を設定",
    change = "変更",
    continueLabel = "続行",
    selectLanguage = "言語を選択",
    selected = "選択済み",
)

/* 中文全集 */
private val SimplifiedChinese = Strings(
    welcomeTo = "欢迎使用",
    accessibility = "无障碍",
    start = "开始",
    setLanguage = "设置语言",
    change = "更改",
    continueLabel = "继续",
    selectLanguage = "选择语言",
    selected = "已选择",
)

private val TraditionalChinese = Strings(
    welcomeTo = "歡迎使用",
    accessibility = "無障礙",
    start = "開始",
    setLanguage = "設定語言",
    change = "變更",
    continueLabel = "繼續",
    selectLanguage = "選擇語言",
    selected = "已選擇",
)

/* 韩文 */
private val Korean = Strings(
    welcomeTo = "환영합니다",
    accessibility = "접근성",
    start = "시작",
    setLanguage = "언어 설정",
    change = "변경",
    continueLabel = "계속",
    selectLanguage = "언어 선택",
    selected = "선택됨",
)

/* 俄文 */
private val Russian = Strings(
    welcomeTo = "Добро пожаловать в",
    accessibility = "Специальные возможности",
    start = "НАЧАТЬ",
    setLanguage = "Выбор языка",
    change = "Изменить",
    continueLabel = "ПРОДОЛЖИТЬ",
    selectLanguage = "Выберите язык",
    selected = "Выбрано",
)

private val TraditionalChineseRegions = setOf("TW", "HK", "MO")

/** 将任意 [Locale] 解析为受支持的文案表；繁简中文按脚本 (Hant/Hans) 或地区 (港澳台) 区分。 */
fun stringsFor(locale: Locale): Strings = when (locale.language) {
    "zh" -> when {
        locale.script.equals("Hant", ignoreCase = true) -> TraditionalChinese
        locale.script.equals("Hans", ignoreCase = true) -> SimplifiedChinese
        locale.country.uppercase(Locale.ROOT) in TraditionalChineseRegions -> TraditionalChinese
        else -> SimplifiedChinese
    }
    "ja" -> Japanese
    "ko" -> Korean
    "ru" -> Russian
    else -> English
}

val LocalStrings = staticCompositionLocalOf { English }
