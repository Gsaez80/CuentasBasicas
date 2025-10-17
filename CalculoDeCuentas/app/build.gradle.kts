plugins {
  id("com.android.application")
  kotlin("android")
  id("org.jetbrains.kotlin.kapt")
}
android {
  namespace = "cl.tuapp.calculocuentas"
  compileSdk = 34
  defaultConfig {
    applicationId = "cl.tuapp.calculocuentas"
    minSdk = 24
    targetSdk = 34
    versionCode = 1
    versionName = "1.0"
  }
  signingConfigs {
    release {
      storeFile file(System.getenv("CC_KEYSTORE_PATH") ?: "keystore/calculo_de_cuentas.keystore")
      storePassword System.getenv("CC_KEYSTORE_PASSWORD")
      keyAlias System.getenv("CC_KEY_ALIAS")
      keyPassword System.getenv("CC_KEY_PASSWORD")
    }
  }
  buildTypes {
    release {
      signingConfig signingConfigs.release
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  buildFeatures { compose = true }
  composeOptions { kotlinCompilerExtensionVersion = "1.5.14" }
  packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}
dependencies {
  implementation(platform("androidx.compose:compose-bom:2024.09.03"))
  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.ui:ui-tooling-preview")
  implementation("androidx.compose.material3:material3")
  implementation("androidx.activity:activity-compose:1.9.2")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
  implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
  implementation("androidx.room:room-runtime:2.6.1")
  implementation("androidx.room:room-ktx:2.6.1")
  kapt("androidx.room:room-compiler:2.6.1")
  debugImplementation("androidx.compose.ui:ui-tooling")
  debugImplementation("androidx.compose.ui:ui-test-manifest")
}
