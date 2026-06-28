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
    // 用户协议屏 (AgreementScreen)
    val userAgreement: String,
    val agree: String,
    val disagree: String,
    val disagreeTitle: String,
    val disagreeMessage: String,
    val disagreeBack: String,
    val disagreeKeepReading: String,
    val disagreePowerOff: String,
    val moeToast: String,
    // 锁屏安全屏 (LockScreen)
    val lockTitle: String,
    val lockSubtitle: String,
    val lockPin: String,
    val lockPinDesc: String,
    val lockPassword: String,
    val lockPasswordDesc: String,
    val lockPattern: String,
    val lockPatternDesc: String,
    val lockSkip: String,
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
    userAgreement = "User Agreement",
    agree = "I AGREE",
    disagree = "I DISAGREE",
    disagreeTitle = "Cannot continue setup",
    disagreeMessage = "You must accept the agreement to finish setting up GaoyiPlayOS. " +
        "Without your consent, device initialization cannot continue.",
    disagreeBack = "Previous",
    disagreeKeepReading = "Keep reading",
    disagreePowerOff = "Power off",
    moeToast = "(｡･ω･｡)ﾉ Moe mode activated!",
    lockTitle = "Protect your phone",
    lockSubtitle = "Set a screen lock so only you can unlock this device and access your data.",
    lockPin = "PIN",
    lockPinDesc = "Enter 4 or more digits",
    lockPassword = "Password",
    lockPasswordDesc = "Mix letters, numbers and symbols",
    lockPattern = "Pattern",
    lockPatternDesc = "Connect 4 or more dots",
    lockSkip = "SKIP",
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
    userAgreement = "利用規約",
    agree = "同意する",
    disagree = "同意しない",
    disagreeTitle = "セットアップを続行できません",
    disagreeMessage = "GaoyiPlayOS のセットアップを完了するには、規約に同意する必要があります。" +
        "同意いただけない場合、デバイスの初期化を続行できません。",
    disagreeBack = "前へ",
    disagreeKeepReading = "読み続ける",
    disagreePowerOff = "電源を切る",
    moeToast = "(｡･ω･｡)ﾉ 萌えモード！",
    lockTitle = "デバイスを保護",
    lockSubtitle = "画面ロックを設定すると、あなただけがこのデバイスのロックを解除し、データにアクセスできます。",
    lockPin = "PIN",
    lockPinDesc = "4 桁以上の数字を入力",
    lockPassword = "パスワード",
    lockPasswordDesc = "英字・数字・記号を組み合わせる",
    lockPattern = "パターン",
    lockPatternDesc = "4 つ以上の点をつなぐ",
    lockSkip = "スキップ",
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
    userAgreement = "用户协议与条款",
    agree = "我同意",
    disagree = "我不同意",
    disagreeTitle = "无法继续设置",
    disagreeMessage = "您需要接受本协议才能完成 GaoyiPlayOS 的初始化设置。" +
        "若不同意，设备将无法继续初始化。",
    disagreeBack = "上一步",
    disagreeKeepReading = "继续阅读",
    disagreePowerOff = "关机",
    moeToast = "(｡･ω･｡)ﾉ 萌化模式已开启～",
    lockTitle = "保护你的设备",
    lockSubtitle = "设置屏幕锁定后，只有你才能解锁本设备并访问其中的数据。",
    lockPin = "数字密码 PIN",
    lockPinDesc = "输入至少 4 位数字",
    lockPassword = "混合密码",
    lockPasswordDesc = "字母、数字与符号混合",
    lockPattern = "手势密码",
    lockPatternDesc = "连接至少 4 个点",
    lockSkip = "不设置",
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
    userAgreement = "使用者協議與條款",
    agree = "我同意",
    disagree = "我不同意",
    disagreeTitle = "無法繼續設定",
    disagreeMessage = "您需要接受本協議才能完成 GaoyiPlayOS 的初始化設定。" +
        "若不同意，裝置將無法繼續初始化。",
    disagreeBack = "上一步",
    disagreeKeepReading = "繼續閱讀",
    disagreePowerOff = "關機",
    moeToast = "(｡･ω･｡)ﾉ 萌化模式已開啟～",
    lockTitle = "保護你的裝置",
    lockSubtitle = "設定螢幕鎖定後，只有你才能解鎖本裝置並存取其中的資料。",
    lockPin = "數字密碼 PIN",
    lockPinDesc = "輸入至少 4 位數字",
    lockPassword = "混合密碼",
    lockPasswordDesc = "字母、數字與符號混合",
    lockPattern = "圖形密碼",
    lockPatternDesc = "連接至少 4 個點",
    lockSkip = "不設定",
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
    userAgreement = "사용자 약관",
    agree = "동의함",
    disagree = "동의 안 함",
    disagreeTitle = "설정을 계속할 수 없음",
    disagreeMessage = "GaoyiPlayOS 설정을 완료하려면 약관에 동의해야 합니다. " +
        "동의하지 않으면 기기 초기화를 계속할 수 없습니다.",
    disagreeBack = "이전",
    disagreeKeepReading = "계속 읽기",
    disagreePowerOff = "종료",
    moeToast = "(｡･ω･｡)ﾉ 모에 모드 활성화!",
    lockTitle = "기기 보호",
    lockSubtitle = "화면 잠금을 설정하면 나만 이 기기를 잠금 해제하고 데이터에 접근할 수 있습니다.",
    lockPin = "PIN",
    lockPinDesc = "숫자 4자리 이상 입력",
    lockPassword = "비밀번호",
    lockPasswordDesc = "문자, 숫자, 기호 조합",
    lockPattern = "패턴",
    lockPatternDesc = "4개 이상의 점 연결",
    lockSkip = "건너뛰기",
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
    userAgreement = "Пользовательское соглашение",
    agree = "ПРИНИМАЮ",
    disagree = "НЕ ПРИНИМАЮ",
    disagreeTitle = "Не удаётся продолжить настройку",
    disagreeMessage = "Чтобы завершить настройку GaoyiPlayOS, необходимо принять соглашение. " +
        "Без вашего согласия инициализация устройства не может быть продолжена.",
    disagreeBack = "Назад",
    disagreeKeepReading = "Продолжить чтение",
    disagreePowerOff = "Выключить",
    moeToast = "(｡･ω･｡)ﾉ Moe-режим включён!",
    lockTitle = "Защитите устройство",
    lockSubtitle = "Установите блокировку экрана, чтобы только вы могли разблокировать устройство и получить доступ к данным.",
    lockPin = "PIN-код",
    lockPinDesc = "Введите не менее 4 цифр",
    lockPassword = "Пароль",
    lockPasswordDesc = "Буквы, цифры и символы",
    lockPattern = "Графический ключ",
    lockPatternDesc = "Соедините не менее 4 точек",
    lockSkip = "ПРОПУСТИТЬ",
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
