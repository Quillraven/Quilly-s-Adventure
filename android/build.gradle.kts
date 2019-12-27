import org.jetbrains.kotlin.konan.properties.loadProperties

plugins {
    id("com.android.application") version "3.5.1"
    kotlin("android") version "1.3.61"
    kotlin("android.extensions") version "1.3.61"
}

// Inherit properties from root project
loadProperties(project.file("../versions.properties").absolutePath).forEach { key, value ->
    ext.set(key as String, value)
}

val versions = ext

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
    api("com.badlogicgames.gdx:gdx-backend-android:${versions["gdx"]}")
    natives("com.badlogicgames.gdx:gdx-platform:${versions["gdx"]}:natives-armeabi")
    natives("com.badlogicgames.gdx:gdx-platform:${versions["gdx"]}:natives-armeabi-v7a")
    natives("com.badlogicgames.gdx:gdx-platform:${versions["gdx"]}:natives-arm64-v8a")
    natives("com.badlogicgames.gdx:gdx-platform:${versions["gdx"]}:natives-x86")
    natives("com.badlogicgames.gdx:gdx-platform:${versions["gdx"]}:natives-x86_64")
    api("com.badlogicgames.gdx:gdx-box2d:${versions["gdx"]}")
    natives("com.badlogicgames.gdx:gdx-box2d-platform:${versions["gdx"]}:natives-armeabi")
    natives("com.badlogicgames.gdx:gdx-box2d-platform:${versions["gdx"]}:natives-armeabi-v7a")
    natives("com.badlogicgames.gdx:gdx-box2d-platform:${versions["gdx"]}:natives-arm64-v8a")
    natives("com.badlogicgames.gdx:gdx-box2d-platform:${versions["gdx"]}:natives-x86")
    natives("com.badlogicgames.gdx:gdx-box2d-platform:${versions["gdx"]}:natives-x86_64")
    api("com.badlogicgames.gdx:gdx-freetype:${versions["gdx"]}")
    natives("com.badlogicgames.gdx:gdx-freetype-platform:${versions["gdx"]}:natives-armeabi")
    natives("com.badlogicgames.gdx:gdx-freetype-platform:${versions["gdx"]}:natives-armeabi-v7a")
    natives("com.badlogicgames.gdx:gdx-freetype-platform:${versions["gdx"]}:natives-arm64-v8a")
    natives("com.badlogicgames.gdx:gdx-freetype-platform:${versions["gdx"]}:natives-x86")
    natives("com.badlogicgames.gdx:gdx-freetype-platform:${versions["gdx"]}:natives-x86_64")
    api("com.badlogicgames.box2dlights:box2dlights:${versions["box2DLight"]}")
    api("com.badlogicgames.ashley:ashley:${versions["ashley"]}")
    api("com.badlogicgames.gdx:gdx-ai:${versions["gdxAI"]}")
    api(kotlin("stdlib"))
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
        version = versions["version"]!!
        versionCode = (versions["androidVersionCode"] as String).toInt()
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

