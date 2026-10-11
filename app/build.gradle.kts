plugins { id("com.android.application") }
android {
  namespace = "com.neet.focus"; compileSdk = 34
  defaultConfig { applicationId = "com.neet.focus"; minSdk = 26; targetSdk = 34; versionCode = 2; versionName = "2.0" }
  compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}
dependencies {
  implementation("androidx.webkit:webkit:1.11.0")
  implementation("androidx.core:core:1.13.1")
}
