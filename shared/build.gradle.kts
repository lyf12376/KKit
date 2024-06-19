import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.zipline)
    alias(libs.plugins.buildConfig)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            commonWebpackConfig {
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(project.projectDir.path)
                    }
                }
            }
        }
    }

    js(IR) {
        browser {}
        nodejs {}
        binaries.executable()
    }
    
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    
    jvm()

    applyDefaultHierarchyTemplate()
    
    sourceSets {
        commonMain.get().apply {
            dependencies {
                // put your Multiplatform dependencies here
            }
            buildConfig {
                useKotlinOutput { internalVisibility = false }
                val ziplineJSVersion: Int by rootProject.extra
                buildConfigField<Int>("ZIPLINE_JS_VERSION", ziplineJSVersion)
                val ziplineJSPort: Int by rootProject.extra
                buildConfigField<Int>("ZIPLINE_JS_PORT", ziplineJSPort)
            }
        }
        val ziplineMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                api(libs.zipline)
            }
        }
        jvmMain.get().apply {
            dependsOn(ziplineMain)
            dependencies {
                implementation(libs.zipline.loader)
                implementation(libs.okhttp3)
            }
        }
        jsMain.get().apply {
            dependsOn(ziplineMain)
        }
    }
}

android {
    namespace = "pub.telephone.kkit.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
