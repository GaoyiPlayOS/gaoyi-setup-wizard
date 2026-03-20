import java.io.File

plugins {
    id("com.android.application")
    kotlin("android")
}

val platformCertificate = rootProject.layout.projectDirectory.file("sign/platform.x509.pem")
val platformPrivateKey = rootProject.layout.projectDirectory.file("sign/platform.pk8")
val generatedSigningDir = rootProject.layout.buildDirectory.dir("signing")
val generatedPlatformKeystore = rootProject.layout.buildDirectory.file("signing/platform.jks")

val preparePlatformKeystore by tasks.registering {
    inputs.files(platformCertificate, platformPrivateKey)
    outputs.file(generatedPlatformKeystore)

    doLast {
        val signingDir = generatedSigningDir.get().asFile.apply { mkdirs() }
        val pemKey = File(signingDir, "platform.pem")
        val pkcs12File = File(signingDir, "platform.p12")
        val keystoreFile = generatedPlatformKeystore.get().asFile

        pemKey.delete()
        pkcs12File.delete()
        keystoreFile.delete()

        exec {
            commandLine(
                "openssl",
                "pkcs8",
                "-inform",
                "DER",
                "-in",
                platformPrivateKey.asFile.absolutePath,
                "-out",
                pemKey.absolutePath,
                "-outform",
                "PEM",
                "-nocrypt",
            )
        }

        exec {
            commandLine(
                "openssl",
                "pkcs12",
                "-export",
                "-in",
                platformCertificate.asFile.absolutePath,
                "-inkey",
                pemKey.absolutePath,
                "-name",
                "platform",
                "-out",
                pkcs12File.absolutePath,
                "-passout",
                "pass:android",
            )
        }

        exec {
            commandLine(
                "keytool",
                "-importkeystore",
                "-noprompt",
                "-alias",
                "platform",
                "-srckeystore",
                pkcs12File.absolutePath,
                "-srcstoretype",
                "PKCS12",
                "-srcstorepass",
                "android",
                "-destkeystore",
                keystoreFile.absolutePath,
                "-deststoretype",
                "JKS",
                "-deststorepass",
                "android",
                "-destkeypass",
                "android",
            )
        }
    }
}

android {
    namespace = "com.android.setupwizard"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.android.setupwizard"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
        vectorDrawables {
            useSupportLibrary = true
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
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

tasks.named("preBuild").configure {
    dependsOn(preparePlatformKeystore)
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.09.00")

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")
    implementation("androidx.navigation:navigation-compose:2.8.0")

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

