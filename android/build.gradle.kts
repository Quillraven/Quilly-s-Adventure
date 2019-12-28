plugins {
    id("com.android.application")
    kotlin("android")
}

val natives by configurations.register("natives")
val copyAndroidNatives by tasks.registering(Task::class)

dependencies {
    implementation(project(":core"))
    api("com.badlogicgames.gdx:gdx-backend-android:${rootProject.extra["gdxVersion"]}")
    natives("com.badlogicgames.gdx:gdx-platform:${rootProject.extra["gdxVersion"]}:natives-armeabi")
    natives("com.badlogicgames.gdx:gdx-platform:${rootProject.extra["gdxVersion"]}:natives-armeabi-v7a")
    natives("com.badlogicgames.gdx:gdx-platform:${rootProject.extra["gdxVersion"]}:natives-arm64-v8a")
    natives("com.badlogicgames.gdx:gdx-platform:${rootProject.extra["gdxVersion"]}:natives-x86")
    natives("com.badlogicgames.gdx:gdx-platform:${rootProject.extra["gdxVersion"]}:natives-x86_64")
    api("com.badlogicgames.gdx:gdx-box2d:${rootProject.extra["gdxVersion"]}")
    natives("com.badlogicgames.gdx:gdx-box2d-platform:${rootProject.extra["gdxVersion"]}:natives-armeabi")
    natives("com.badlogicgames.gdx:gdx-box2d-platform:${rootProject.extra["gdxVersion"]}:natives-armeabi-v7a")
    natives("com.badlogicgames.gdx:gdx-box2d-platform:${rootProject.extra["gdxVersion"]}:natives-arm64-v8a")
    natives("com.badlogicgames.gdx:gdx-box2d-platform:${rootProject.extra["gdxVersion"]}:natives-x86")
    natives("com.badlogicgames.gdx:gdx-box2d-platform:${rootProject.extra["gdxVersion"]}:natives-x86_64")
    api("com.badlogicgames.gdx:gdx-freetype:${rootProject.extra["gdxVersion"]}")
    natives("com.badlogicgames.gdx:gdx-freetype-platform:${rootProject.extra["gdxVersion"]}:natives-armeabi")
    natives("com.badlogicgames.gdx:gdx-freetype-platform:${rootProject.extra["gdxVersion"]}:natives-armeabi-v7a")
    natives("com.badlogicgames.gdx:gdx-freetype-platform:${rootProject.extra["gdxVersion"]}:natives-arm64-v8a")
    natives("com.badlogicgames.gdx:gdx-freetype-platform:${rootProject.extra["gdxVersion"]}:natives-x86")
    natives("com.badlogicgames.gdx:gdx-freetype-platform:${rootProject.extra["gdxVersion"]}:natives-x86_64")
    api("com.badlogicgames.box2dlights:box2dlights:${rootProject.extra["box2DLightsVersion"]}")
    api("com.badlogicgames.ashley:ashley:${rootProject.extra["ashleyVersion"]}")
    api("com.badlogicgames.gdx:gdx-ai:${rootProject.extra["aiVersion"]}")
    api("org.jetbrains.kotlin:kotlin-stdlib:${rootProject.extra["kotlinVersion"]}")
}

android {
    compileSdkVersion(29)
    sourceSets {
        getByName("main") {
            manifest.srcFile("AndroidManifest.xml")
            java.srcDirs("src")
            aidl.srcDirs("src")
            renderscript.srcDirs("src")
            res.srcDirs("res")
            assets.srcDirs("assets")
            jniLibs.srcDirs("libs")
        }

    }
    defaultConfig {
        applicationId = "com.game.quillyjumper"
        minSdkVersion(14)
        targetSdkVersion(29)
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
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
