plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

android {
    namespace = "com.example.spotly.core.database"
    compileSdk = 37
    defaultConfig { minSdk = 26 }
    testOptions { unitTests.isIncludeAndroidResources = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
room { schemaDirectory("$projectDir/schemas") }

dependencies {
    implementation(project(":core:domain"))
    api("androidx.room:room-runtime:2.8.4")
    implementation(libs.kotlinx.coroutines.core)
    ksp("androidx.room:room-compiler:2.8.4")
    testImplementation(libs.junit)
    testImplementation("org.robolectric:robolectric:4.16.1")
}
