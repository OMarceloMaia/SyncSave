import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.ir.backend.js.compile

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    kotlin("plugin.serialization") version "1.9.0"
}

kotlin {
    jvm("desktop")
    
    sourceSets {
        val desktopMain by getting
        val ktor_version: String by project
        val logback_version: String by project
        
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)

            implementation("io.ktor:ktor-server-core:$ktor_version")
            implementation("io.ktor:ktor-server-netty:$ktor_version")
            implementation("io.ktor:ktor-server-auth:$ktor_version")
            implementation("io.ktor:ktor-server-sessions:$ktor_version")
            implementation("io.ktor:ktor-server-html-builder:$ktor_version")
            implementation("io.ktor:ktor-client-core:$ktor_version")
            implementation("io.ktor:ktor-client-cio:$ktor_version")
            implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
            implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")

            implementation("com.google.apis:google-api-services-drive:v3-rev20220815-2.0.0")
            implementation("com.google.api-client:google-api-client:2.0.0")
            implementation("com.google.auth:google-auth-library-oauth2-http:1.11.0")
            implementation("com.google.code.gson:gson:2.9.1")

            implementation("ch.qos.logback:logback-classic:$logback_version")

            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
        }
    }
}


compose.desktop {
    application {
        mainClass = "org.mgam.syncsave.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "org.mgam.syncsave"
            packageVersion = "1.0.0"
        }
    }
}


