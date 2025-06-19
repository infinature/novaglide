// ...existing code...

dependencies {
    // ...existing dependencies...
    
    // Room数据库相关
    val roomVersion = "2.5.2"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")  // Kotlin扩展和协程支持
    kapt("androidx.room:room-compiler:$roomVersion")  // 或使用ksp("androidx.room:room-compiler:$roomVersion")
    
    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.compose.animation)

    // Accompanist
    implementation(libs.accompanist.swiperefresh)

    // ...existing dependencies...
}

// ...existing code...