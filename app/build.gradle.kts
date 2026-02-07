plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.devtools.ksp)
    // Hilt 플러그인
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.chordzip"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.chordzip"
        minSdk = 30
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

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.foundation.layout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // 네비게이션 (NavController 사용을 위해 필수)
    implementation("androidx.navigation:navigation-compose:2.8.5")
    // 아이콘 확장 (혹시 모를 아이콘 누락 방지, Visibility는 기본에 있지만 추가해두면 좋습니다)
    implementation("androidx.compose.material:material-icons-extended:1.7.6")
    // Dagger Hilt 의존성
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-android-compiler:2.51.1")
    // ViewModel과 함께 쓰기 위한 Hilt Navigation Compose (선택 사항이지만 권장)
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    // Reorderable 라이브러리 추가
    implementation("sh.calvin.reorderable:reorderable:2.4.3")
    // [Room 데이터베이스] : 데이터를 폰에 영구 저장하는 도구
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    // [KSP] : 코틀린용 어노테이션 프로세서 (Room 코드를 자동 생성해줌)
    // plugins 블록에 id("com.google.devtools.ksp")가 추가되어 있어야 합니다.
    ksp("androidx.room:room-compiler:$room_version")
    // [Room KTX] : 코루틴과 Flow를 편하게 쓰기 위함
    implementation("androidx.room:room-ktx:$room_version")
    // [Gson] : 리스트(List) 데이터를 문자열(JSON)로 바꿔주는 도구
    implementation("com.google.code.gson:gson:2.10.1")
}