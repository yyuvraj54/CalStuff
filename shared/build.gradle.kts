import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
}

kotlin {
    applyDefaultHierarchyTemplate()

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
        publishLibraryVariants("release", "debug")
    }

    iosArm64 {
        binaries.framework {
            baseName = "CalStuffShared"
            isStatic = true
            // Exposes MutableStateFlow/Flow builders to Swift — needed so Swift-side repository
            // implementations (UserProfileRepository, NutritionRepository, StreakRepository) can
            // construct the Flow their observe*() methods must return (e.g. wrapping a Firestore
            // addSnapshotListener). Without this, only the *interface* types (Flow/StateFlow as
            // protocols) are visible from Swift, not anything that can construct one.
            export(libs.kotlinx.coroutines.core)
        }
    }
    iosSimulatorArm64 {
        binaries.framework {
            baseName = "CalStuffShared"
            isStatic = true
            export(libs.kotlinx.coroutines.core)
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                api(libs.kotlinx.coroutines.core)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

android {
    namespace = "com.dusht.shared"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
