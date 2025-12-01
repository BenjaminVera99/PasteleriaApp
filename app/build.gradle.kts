plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.kapt")

    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"
}

android {
    namespace = "com.example.pasteleriaapp"
    compileSdk = 36 // Se usa la versión directa

    defaultConfig {
        applicationId = "com.example.pasteleriaapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("com.google.code.gson:gson:2.10.1")

    //  CONVERSOR GSON (JSON to Object)
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    // Coil
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.ui.test.junit4)

    // Room
    val roomVersion = "2.7.0-beta01"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")


    // Konfetti
    implementation("nl.dionsegijn:konfetti-compose:2.0.5")

    // ==========================================================
    // ⭐ DEPENDENCIAS DE TEST UNITARIO (Kotest, Mockk, JUnit 5) ⭐
    // ==========================================================
    testImplementation("io.kotest:kotest-runner-junit5:5.8.0")
    testImplementation("io.kotest:kotest-assertions-core:5.8.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0") // JUnit 5 runner
    testImplementation("io.mockk:mockk:1.13.10")

    // Nota: Mantenemos la exclusión de hamcrest-core y la dependencia de JUnit
    testImplementation(libs.junit) {
        exclude(group = "org.hamcrest", module = "hamcrest-core")
    }

    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("javax.inject:javax.inject:1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Asegurarse que la dependencia de AndroidX Test no cause conflicto
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))

    // Test UI específicas (Versiones de la guía)
    androidTestImplementation("androidx.test.ext:junit:1.2.1") // <--- Actualizar
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1") // <--- Actualizar

    // Las dependencias de compose-test-junit4 y mockk son necesarias para tu ProfileScreenTest
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.2")
    androidTestImplementation("io.mockk:mockk-android:1.13.10")

    // Mantenemos esta línea para el preview tooling
    debugImplementation(libs.androidx.compose.ui.tooling)
}

// ==========================================================
// ⭐ CONFIGURACIÓN PARA JUNIT 5 (FUERA DEL BLOQUE dependencies) ⭐
// ==========================================================
tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}