// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {

    repositories {
        google()
        jcenter()
//        maven{url "http://maven.aliyun.com/nexus/content/groups/public/"}
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.5.0")

        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.50")
        classpath("com.github.dcendents:android-maven-gradle-plugin:2.1")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        jcenter()
//        maven{url "http://maven.aliyun.com/nexus/content/groups/public/"}
        maven { setUrl("https://jitpack.io")  }
    }
}

val clean by tasks.registering(Delete::class){
    delete(buildDir)
}
