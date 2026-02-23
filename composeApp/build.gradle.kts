import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.compose.desktop.application.tasks.AbstractJPackageTask
import org.gradle.api.DefaultTask
import com.util.rsvp.gradle.ExportDesktopInstallerTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.File
import javax.inject.Inject

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

val desktopPackageName = "RapidReader"
val desktopPackageVersion = "1.0.0"

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    jvm("desktop") {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            linkerOpts("-framework", "PDFKit")
        }
    }
    
    js {
        browser()
        binaries.executable()
    }
    
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.datastore.preferences)
            implementation(libs.pdfbox.android)
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.pdfbox.jvm)
            }
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.material.icons.extended)
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

abstract class GenerateMacIcns @Inject constructor(
    private val execOps: ExecOperations,
) : DefaultTask() {
    @get:InputFile
    abstract val inputPng: RegularFileProperty

    @get:OutputDirectory
    abstract val iconsetDir: DirectoryProperty

    @get:OutputFile
    abstract val outputIcns: RegularFileProperty

    @TaskAction
    fun run() {
        val isMac = System.getProperty("os.name").contains("Mac", ignoreCase = true)
        if (!isMac) return

        val input = inputPng.get().asFile
        if (!input.exists()) error("Missing icon source png: ${input.absolutePath}")

        val iconset = iconsetDir.get().asFile
        iconset.deleteRecursively()
        iconset.mkdirs()

        fun sips(size: Int, outName: String) {
            execOps.exec {
                commandLine(
                    "sips",
                    "-z", size.toString(), size.toString(),
                    input.absolutePath,
                    "--out", File(iconset, outName).absolutePath,
                )
            }
        }

        // Required macOS iconset sizes.
        sips(16, "icon_16x16.png")
        sips(32, "icon_16x16@2x.png")
        sips(32, "icon_32x32.png")
        sips(64, "icon_32x32@2x.png")
        sips(128, "icon_128x128.png")
        sips(256, "icon_128x128@2x.png")
        sips(256, "icon_256x256.png")
        sips(512, "icon_256x256@2x.png")
        sips(512, "icon_512x512.png")
        sips(1024, "icon_512x512@2x.png")

        execOps.exec {
            commandLine(
                "iconutil",
                "-c", "icns",
                iconset.absolutePath,
                "-o", outputIcns.get().asFile.absolutePath,
            )
        }
    }
}

val generateMacIcns by tasks.registering(GenerateMacIcns::class) {
    inputPng.set(layout.projectDirectory.file("src/commonMain/composeResources/drawable/rapid_reader_logo.png"))
    iconsetDir.set(layout.buildDirectory.dir("generated/macos/RapidReader.iconset"))
    outputIcns.set(layout.buildDirectory.file("generated/macos/RapidReader.icns"))
}

compose.desktop {
    application {
        mainClass = "com.util.rsvp.MainKt"

        buildTypes.release.proguard {
            configurationFiles.from(project.file("compose-desktop.pro"))
        }

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = desktopPackageName
            packageVersion = desktopPackageVersion

            macOS {
                // Fix DMG/.app name + icon on macOS (jpackage expects .icns).
                iconFile.set(generateMacIcns.flatMap { it.outputIcns })
                bundleID = "com.util.rsvp"
            }
        }
    }
}

// Ensure jpackage tasks have the generated .icns available.
tasks.withType<AbstractJPackageTask>().configureEach {
    dependsOn(generateMacIcns)
}

val dmgPackagingTaskCandidates = listOf("packageDmg", "packageReleaseDmg")
val msiPackagingTaskCandidates = listOf("packageMsi", "packageReleaseMsi")
val debPackagingTaskCandidates = listOf("packageDeb", "packageReleaseDeb")

val desktopBinariesDir = layout.buildDirectory.dir("compose/binaries")
val desktopDistDir = rootProject.layout.projectDirectory.dir("dist/desktop")

tasks.register<ExportDesktopInstallerTask>("exportDesktopMac") {
    group = "distribution"
    description = "Build DMG and copy to dist/desktop"

    requiredOsSubstring.set("Mac")
    installerExtension.set("dmg")
    outputFileName.set("${desktopPackageName}-${desktopPackageVersion}-mac.dmg")
    packagingTaskCandidates.set(dmgPackagingTaskCandidates)
    binariesDir.set(desktopBinariesDir)
    distDir.set(desktopDistDir)

    dmgPackagingTaskCandidates.firstOrNull { tasks.findByName(it) != null }?.let { dependsOn(it) }
}

tasks.register<ExportDesktopInstallerTask>("exportDesktopWindows") {
    group = "distribution"
    description = "Build MSI and copy to dist/desktop"

    requiredOsSubstring.set("Windows")
    installerExtension.set("msi")
    outputFileName.set("${desktopPackageName}-${desktopPackageVersion}-windows.msi")
    packagingTaskCandidates.set(msiPackagingTaskCandidates)
    binariesDir.set(desktopBinariesDir)
    distDir.set(desktopDistDir)

    msiPackagingTaskCandidates.firstOrNull { tasks.findByName(it) != null }?.let { dependsOn(it) }
}

tasks.register<ExportDesktopInstallerTask>("exportDesktopLinux") {
    group = "distribution"
    description = "Build DEB and copy to dist/desktop"

    requiredOsSubstring.set("Linux")
    installerExtension.set("deb")
    outputFileName.set("${desktopPackageName}-${desktopPackageVersion}-linux.deb")
    packagingTaskCandidates.set(debPackagingTaskCandidates)
    binariesDir.set(desktopBinariesDir)
    distDir.set(desktopDistDir)

    debPackagingTaskCandidates.firstOrNull { tasks.findByName(it) != null }?.let { dependsOn(it) }
}

tasks.register("exportDesktopCurrentOs") {
    group = "distribution"
    description = "Build and export the installer for the current OS"

    val osName = System.getProperty("os.name").lowercase()
    when {
        osName.contains("mac") -> dependsOn("exportDesktopMac")
        osName.contains("win") -> dependsOn("exportDesktopWindows")
        osName.contains("linux") -> dependsOn("exportDesktopLinux")
        else -> error("Unsupported OS for desktop packaging: ${System.getProperty("os.name")}")
    }
}

android {
    namespace = "com.util.rsvp"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.util.rsvp"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
}

