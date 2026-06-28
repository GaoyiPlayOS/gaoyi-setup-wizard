![gaoyi-setup-wizard](https://socialify.git.ci/GaoyiPlayOS/gaoyi-setup-wizard/image?custom_language=Kotlin&description=1&font=Source+Code+Pro&issues=1&language=1&logo=https%3A%2F%2Fgaoyiplayos.pages.dev%2Fassets%2Fandroid-chrome.png&name=1&owner=1&pattern=Transparent&pulls=1&stargazers=1&theme=Auto)

# SUW for GaoyiPlayOS
A simple, minimalism SUW (Setup Wizard) for GaoyiPlayOS Release version.

## Features
- Elegant UI (Material You, Dark Mode, Monet)
- Full accessibility support (TalkBack)
- Multi-language support (translations are welcome!)
- Full system integration *(see below)*

## Key Points
- Shared `system.uid` for privileged APIs
- 0 Java, 0 XML
- 100% Kotlin with Jetpack Compose
- 100% Tranditional Style Human Coding. 0 AI, 0 Vibe.
<!-- btw README wrote by myself -->
- Fuck GeometryOS
<!-- And also fuck QZX -->

## Requirements
- Only support **`aarch64` (armv8)**
- `platform` Signing Keys from AOSP is **required for `shared.system.uid`**
- Builds must be placed in `/system/priv-app/SetupWizard` with name `SetupWizard.apk`

## License
GPL Version 3. See the [License](/LICENSE) file.
