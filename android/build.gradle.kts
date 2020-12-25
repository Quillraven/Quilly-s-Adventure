plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    compileSdkVersion(Apps.compileSdk)
    sourceSets {
        named("main") {
            java.srcDirs("src/main/kotlin")
            assets.srcDirs(project.file("../assets"))
            jniLibs.srcDirs("libs")
        }
    }

    defaultConfig {
        applicationId = Apps.packageName
        minSdkVersion(Apps.minSdk)
        targetSdkVersion(Apps.targetSdk)
        versionCode = Apps.versionCode
        versionName = Apps.versionName
    }

    buildTypes {
        named("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = Versions.java
        targetCompatibility = Versions.java
    }

    kotlinOptions {
        jvmTarget = Versions.jvm
    }
}

val natives: Configuration by configurations.creating

dependencies {
    implementation(project(":core"))
    api("com.badlogicgames.gdx:gdx-backend-android:${Versions.gdx}")
    natives("com.badlogicgames.gdx:gdx-platform:${Versions.gdx}:natives-armeabi")
    natives("com.badlogicgames.gdx:gdx-platform:${Versions.gdx}:natives-armeabi-v7a")
    natives("com.badlogicgames.gdx:gdx-platform:${Versions.gdx}:natives-arm64-v8a")
    natives("com.badlogicgames.gdx:gdx-platform:${Versions.gdx}:natives-x86")
    natives("com.badlogicgames.gdx:gdx-platform:${Versions.gdx}:natives-x86_64")
    api("com.badlogicgames.gdx:gdx-box2d:${Versions.gdx}")
    natives("com.badlogicgames.gdx:gdx-box2d-platform:${Versions.gdx}:natives-armeabi")
    natives("com.badlogicgames.gdx:gdx-box2d-platform:${Versions.gdx}:natives-armeabi-v7a")
    natives("com.badlogicgames.gdx:gdx-box2d-platform:${Versions.gdx}:natives-arm64-v8a")
    natives("com.badlogicgames.gdx:gdx-box2d-platform:${Versions.gdx}:natives-x86")
    natives("com.badlogicgames.gdx:gdx-box2d-platform:${Versions.gdx}:natives-x86_64")
    api("com.badlogicgames.gdx:gdx-freetype:${Versions.gdx}")
    natives("com.badlogicgames.gdx:gdx-freetype-platform:${Versions.gdx}:natives-armeabi")
    natives("com.badlogicgames.gdx:gdx-freetype-platform:${Versions.gdx}:natives-armeabi-v7a")
    natives("com.badlogicgames.gdx:gdx-freetype-platform:${Versions.gdx}:natives-arm64-v8a")
    natives("com.badlogicgames.gdx:gdx-freetype-platform:${Versions.gdx}:natives-x86")
    natives("com.badlogicgames.gdx:gdx-freetype-platform:${Versions.gdx}:natives-x86_64")
    api("com.badlogicgames.box2dlights:box2dlights:${Versions.box2DLight}")
    api("com.badlogicgames.ashley:ashley:${Versions.ashley}")
    api("com.badlogicgames.gdx:gdx-ai:${Versions.gdxAI}")
    api("org.jetbrains.kotlin:kotlin-stdlib")
}

// Called every time gradle gets executed, takes the native dependencies of
// the natives configuration, and extracts them to the proper libs/ folders
// so they get packed with the APK.
tasks.register("copyAndroidNatives") {
    doFirst {
        natives.files.forEach { jar ->
            val outputDir = file("libs/" + jar.nameWithoutExtension.substringAfterLast("natives-"))
            outputDir.mkdirs()
            copy {
                from(zipTree(jar))
                into(outputDir)
                include("*.so")
            }
        }
    }
}

tasks.whenTaskAdded {
    if ("package" in name) {
        dependsOn("copyAndroidNatives")
    }
}
