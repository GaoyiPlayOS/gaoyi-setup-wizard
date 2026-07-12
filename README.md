> [!tip]
> **Why this branch exists?**
> Try fixing the *weird padding (or exactly, a weird white space)* on buttom when using 2/3-button navigation.

![gaoyi-setup-wizard](https://socialify.git.ci/GaoyiPlayOS/gaoyi-setup-wizard/image?custom_language=Kotlin&description=1&font=Source+Code+Pro&issues=1&language=1&logo=https%3A%2F%2Fgaoyiplayos.pages.dev%2Fassets%2Fandroid-chrome.png&name=1&owner=1&pattern=Transparent&pulls=1&stargazers=1&theme=Auto)

# SUW for GaoyiPlayOS
A simple, minimalism SUW (Setup Wizard) for GaoyiPlayOS Release version.

## Features
- Elegant UI (Material You, Dark Mode, Monet)
- ~~Full accessibility support (TalkBack)~~ <- **In progress, will be introduced with Accessibility panel.**
- Multi-language support (translations are welcome!)
- Full system integration *(see below)*

## Key Points
- Shared `system.uid` for privileged APIs
- 0 Java, 0 XML
- 100% Kotlin with Jetpack Compose
- 100% Tranditional Style Human Coding. 0 AI, 0 Vibe.
- Fuck GeometryOS
<!-- And also fuck QZX -->
<!-- btw README wrote by myself -->
<p align="left">
  <a href="https://notbyai.fyi/" target="_blank">
    <img src="assets/badges/svg/Written-By-a-Human-Not-By-AI-Badge-black.svg" alt="All documents are written by a human." />
  </a>
  <a href="https://notbyai.fyi/" target="_blank">
    <img src="assets/badges/svg/Developed-By-a-Human-Not-By-AI-Badge-black.svg" alt="All codes are written, tested and reviewed by a human." />
  </a>
</p>

## Requirements
- Only support **`aarch64` (armv8)**
- Minimal Android version is **12 (API 31)**
- `platform` Signing Keys from AOSP is **required for `shared.system.uid`**
- Builds must be placed in `/system/system_ext/priv-app/SetupWizard` with name `SetupWizard.apk`

## Toolchain
- Gradle 9.6+ *(Project uses 9.6.1)*
- Android NDK & `build-tools` 31+. Target is 37 (Cinnamon Bun)

## License
GPL Version 3. See the [License](/LICENSE) file.
