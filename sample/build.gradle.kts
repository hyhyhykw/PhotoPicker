plugins {
    id("com.android.application")
}

android {
    compileSdkVersion(28)
    defaultConfig {
        applicationId = "com.hy.photopicker"
        minSdkVersion(21)
        targetSdkVersion(27)
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true
        javaCompileOptions {
            annotationProcessorOptions {
                includeCompileClasspath = true
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildTypes {
        get("release").apply {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("androidx.appcompat:appcompat:1.1.0")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")
    implementation(project(":picker"))
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("androidx.recyclerview:recyclerview:1.0.0")
    api("com.github.hyhyhykw:Crash:1.8")
    implementation("com.github.bumptech.glide:okhttp3-integration:4.9.0")
    implementation("com.github.bumptech.glide:glide:4.9.0")
    implementation("com.github.markzhai:blockcanary-android:1.5.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.9.0")
    testImplementation("com.squareup.leakcanary:leakcanary-android-no-op:1.6.3")
    debugImplementation("com.squareup.leakcanary:leakcanary-android:1.6.3")
    releaseImplementation("com.squareup.leakcanary:leakcanary-android-no-op:1.6.3")
}
