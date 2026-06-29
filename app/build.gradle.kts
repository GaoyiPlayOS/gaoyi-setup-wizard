import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.File
import javax.inject.Inject

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

val platformCertificate = rootProject.layout.projectDirectory.file("sign/platform.x509.pem")
val platformPrivateKey = rootProject.layout.projectDirectory.file("sign/platform.pk8")
val generatedPlatformKeystore = rootProject.layout.buildDirectory.file("signing/platform.jks")

/* Gradle 9 已移除 `Project.exec`，故重构为带 [ExecOperations] 注入的类型化任务 */
abstract class PreparePlatformKeystore @Inject constructor(
    private val execOps: ExecOperations,
) : DefaultTask() {
    @get:InputFile
    abstract val certificate: RegularFileProperty

    @get:InputFile
    abstract val privateKey: RegularFileProperty

    @get:OutputFile
    abstract val keystore: RegularFileProperty

    @TaskAction
    fun prepare() {
        val keystoreFile = keystore.get().asFile
        val signingDir = keystoreFile.parentFile.apply { mkdirs() }
        val pemKey = File(signingDir, "platform.pem")
        val pkcs12File = File(signingDir, "platform.p12")

        pemKey.delete()
        pkcs12File.delete()
        keystoreFile.delete()

        execOps.exec {
            commandLine(
                "openssl", "pkcs8", "-inform", "DER",
                "-in", privateKey.get().asFile.absolutePath,
                "-out", pemKey.absolutePath,
                "-outform", "PEM", "-nocrypt",
            )
        }
        execOps.exec {
            commandLine(
                "openssl", "pkcs12", "-export",
                "-in", certificate.get().asFile.absolutePath,
                "-inkey", pemKey.absolutePath,
                "-name", "platform",
                "-out", pkcs12File.absolutePath,
                "-passout", "pass:android",
            )
        }
        execOps.exec {
            commandLine(
                "keytool", "-importkeystore", "-noprompt",
                "-alias", "platform",
                "-srckeystore", pkcs12File.absolutePath,
                "-srcstoretype", "PKCS12",
                "-srcstorepass", "android",
                "-destkeystore", keystoreFile.absolutePath,
                "-deststoretype", "JKS",
                "-deststorepass", "android",
                "-destkeypass", "android",
            )
        }
    }
}

val preparePlatformKeystore by tasks.registering(PreparePlatformKeystore::class) {
    certificate.set(platformCertificate)
    privateKey.set(platformPrivateKey)
    keystore.set(generatedPlatformKeystore)
}

android {
    namespace = "com.android.setupwizard"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.android.setupwizard"
        minSdk = 31
        targetSdk = 37
        versionCode = 31
        versionName = "12"
        vectorDrawables {
            useSupportLibrary = true
        }
        /*
         * 仅保留 aarch64
         * 剔除全部 32 位与 x86 冗余 ABI
        */
        ndk {
            abiFilters += "arm64-v8a"
        }
    }

    signingConfigs {
        create("platform") {
            storeFile = generatedPlatformKeystore.get().asFile
            storePassword = "android"
            keyAlias = "platform"
            keyPassword = "android"
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = true
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("platform")
            isDebuggable = true
        }
        release {
            signingConfig = signingConfigs.getByName("platform")
            /* 
             * 极限瘦身说是
             * R8 全量混淆 + 无用资源剔除
             * Anti QZX
            */
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// Kotlin 2.x：jvmTarget 迁移至 compilerOptions DSL（android.kotlinOptions 已废弃）
kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

tasks.named("preBuild").configure {
    dependsOn(preparePlatformKeystore)
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2026.06.00")

    implementation("androidx.core:core-ktx:1.19.0")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.11.0")
    implementation("androidx.navigation:navigation-compose:2.9.8")

    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
