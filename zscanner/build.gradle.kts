import java.util.Base64
import org.jetbrains.compose.resources.ResourcesExtension.ResourceClassGeneration
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.androidLint)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    id("maven-publish")
    id("signing")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    android {
        namespace = "io.github.namantonk"
        minSdk = libs.versions.android.minSdk.get().toInt()
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        androidResources {
            enable = true
        }
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    val xcfName = "zscanner"

    iosArm64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    iosSimulatorArm64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)
                implementation(libs.components.resources)
                implementation(libs.material.icons.extended)
                implementation(libs.runtime)
                implementation(libs.foundation)
                implementation(libs.material3)
                implementation(libs.ui)
                implementation(libs.ui.util)
                implementation(libs.ui.backhandler)
                implementation(libs.androidx.lifecycle.runtimeCompose)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.compose.uiToolingPreview)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.androidx.camera.core)
                implementation(libs.androidx.camera.camera2)
                implementation(libs.androidx.camera.lifecycle)
                implementation(libs.androidx.camera.view)
                implementation(libs.google.mlkit.barcode)
                implementation(libs.androidx.activity.compose)
            }
        }

        iosMain.dependencies {
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.zscanner.generated.resources"
    generateResClass = ResourceClassGeneration.Always
}

group = "com.github.namantonk"
version = "1.0.0"

val emptyJavadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    destinationDirectory.set(layout.buildDirectory.dir("emptyJavadoc"))
}

publishing {
    publications.withType<MavenPublication> {
        artifact(emptyJavadocJar)
        pom {
            name.set("ZScanner")
            description.set("Kotlin Multiplatform Barcode Scanner Library")
            url.set("https://github.com/NamanTonk/zscanner-kmp")
            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            developers {
                developer {
                    id.set("namantonk")
                    name.set("Naman Tonk")
                    email.set("namantonk@gmail.com")
                }
            }
            scm {
                connection.set("scm:git:git://github.com/NamanTonk/zscanner-kmp.git")
                developerConnection.set("scm:git:ssh://github.com/NamanTonk/zscanner-kmp.git")
                url.set("https://github.com/NamanTonk/zscanner-kmp")
            }
        }
    }
    repositories {
        maven {
            name = "LocalDirectory"
            url = uri(layout.buildDirectory.dir("repo"))
        }
    }
}

signing {
    val signingKeyBase64 = System.getenv("GPG_SIGNING_KEY") ?: project.findProperty("signing.key") as String?
    val signingPassword = System.getenv("GPG_SIGNING_PASSWORD") ?: project.findProperty("signing.password") as String?
    
    if (signingKeyBase64 != null || signingPassword != null) {
        if (signingKeyBase64.isNullOrBlank()) {
            logger.error("GPG_SIGNING_KEY is null or blank!")
        }
        if (signingPassword.isNullOrBlank()) {
            logger.error("GPG_SIGNING_PASSWORD is null or blank!")
        }
        
        if (!signingKeyBase64.isNullOrBlank() && !signingPassword.isNullOrBlank()) {
            val signingKey = try {
                val cleanedKey = signingKeyBase64.trim().replace("\\s".toRegex(), "")
                String(Base64.getDecoder().decode(cleanedKey))
            } catch (e: Exception) {
                signingKeyBase64 // Fallback if already plain text
            }
            
            if (!signingKey.contains("-----BEGIN PGP PRIVATE KEY BLOCK-----")) {
                logger.error("Parsed GPG key does not contain private key block header! Key length: ${signingKey.length}")
            }
            
            useInMemoryPgpKeys(signingKey, signingPassword)
            sign(publishing.publications)
        }
    }
}
