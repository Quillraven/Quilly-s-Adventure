plugins {
    id("com.android.application")
    kotlin("android")
}

val natives by configurations.register("natives")
val copyAndroidNatives by tasks.registering(Task::class)

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
    api("org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}")
}

android {
    compileSdkVersion(Apps.compileSdk)
    sourceSets {
        getByName("main") {
            java.srcDirs("src/main/kotlin")
            assets.srcDirs(rootProject.file("assets"))
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
        getByName("release") {
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

tasks {
    // called every time gradle gets executed, takes the native dependencies of
    // the natives configuration, and extracts them to the proper libs/ folders
    // so they get packed with the APK.
    copyAndroidNatives {
        doFirst {
            file("libs/armeabi/").mkdirs()
            file("libs/armeabi-v7a/").mkdirs()
            file("libs/arm64-v8a/").mkdirs()
            file("libs/x86_64/").mkdirs()
            file("libs/x86/").mkdirs()
            natives.files.forEach { jar ->
                val outputDir = when {
                    jar.name.endsWith("natives-arm64-v8a.jar") -> file("libs/arm64-v8a")
                    jar.name.endsWith("natives-armeabi-v7a.jar") -> file("libs/armeabi-v7a")
                    jar.name.endsWith("natives-armeabi.jar") -> file("libs/armeabi")
                    jar.name.endsWith("natives-x86_64.jar") -> file("libs/x86_64")
                    jar.name.endsWith("natives-x86.jar") -> file("libs/x86")
                    else -> null
                }
                if (outputDir != null) {
                    copy {
                        from(zipTree(jar))
                        into(outputDir)
                        include("*.so")
                    }
                }
            }
        }
    }

    whenTaskAdded {
        if ("package" in this.name) {
            dependsOn(copyAndroidNatives)
        }
    }
}
