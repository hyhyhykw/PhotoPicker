import org.codehaus.groovy.runtime.DefaultGroovyMethods
import org.jetbrains.kotlin.gradle.internal.CacheImplementation
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("com.github.dcendents.android-maven") // 添加这个
}

group = "com.github.hyhyhykw"    // 指定group，com.github.<用户名>
android {
    compileSdkVersion(28)
    resourcePrefix("picker_")
    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(27)
        versionCode = 42
        versionName = "4.2"
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

    androidExtensions {
        isExperimental = true
        defaultCacheImplementation = CacheImplementation.SPARSE_ARRAY
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.50")
    implementation("org.jetbrains.anko:anko-common:0.10.8")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")
    implementation("androidx.appcompat:appcompat:1.1.0")
    implementation("androidx.viewpager2:viewpager2:1.0.0-beta04")
    implementation("androidx.recyclerview:recyclerview:1.0.0")

    api("com.facebook.fresco:fresco:2.0.0")
    api("com.facebook.fresco:animated-gif:2.0.0")
    api("me.relex:photodraweeview:2.0.0")
    api("androidx.exifinterface:exifinterface:1.0.0")
    implementation("com.google.code.gson:gson:2.8.5")
    api("pub.devrel:easypermissions:3.0.0")
    implementation("com.afollestad.material-dialogs:core:3.1.1")
}

//---------------------------------------------

// 指定编码
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

// 打包源码
val sourcesJar by tasks.creating(Jar::class) {
    from(android.sourceSets["main"].java.srcDirs)
    archiveClassifier.set("sources")
}

val javadoc by tasks.creating(Javadoc::class) {
    isFailOnError = false
    source = android.sourceSets["main"].java.sourceFiles
    val join = DefaultGroovyMethods.join(android.bootClasspath as Iterable<File>, File.pathSeparator)
    classpath.plus(project.files(join))
    classpath.plus(configurations["compile"])
}

// 制作文档(Javadoc)
val javadocJar by tasks.creating(Jar::class) {
    dependsOn(javadoc)
    archiveClassifier.set("javadoc")
    from(javadoc.destinationDir)
}

artifacts {
    archives(sourcesJar)
    archives(javadocJar)
}