plugins {
    id("com.android.application") version Versions.androidGradlePlugin
    kotlin("android") version Versions.kotlin
    kotlin("android.extensions") version Versions.kotlin
}

group = "com.quillraven.quillyjumper"
version = "1.0-SNAPSHOT"

allprojects {
    repositories {
        google()
        jcenter()
    }
}

val natives by configurations.register("natives")
val copyAndroidNatives by tasks.registering(Task::class)

dependencies {
    implementation(project("core"))
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
    //api(kotlin("stdlib"))
}

android {
    compileSdkVersion(29)

    sourceSets {
        getByName("main").apply {
            java.srcDir("src/main/kotlin")
            assets.srcDir("../assets")
            jniLibs.srcDir("libs")
        }
    }

    defaultConfig {
        applicationId = "com.game.quillyjumper.android"
        minSdkVersion(23)
        targetSdkVersion(29)
        version = Versions.version
        versionCode = Versions.androidVersionCode
    }

    buildTypes {
        getByName("release").apply {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
}
tasks {
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

