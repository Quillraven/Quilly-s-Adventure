import java.util.*

plugins {
    id("com.android.application")
    kotlin("android")
}

val natives by configurations.register("natives")

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
    buildToolsVersion("29.0.2")
    compileSdkVersion(29)
    sourceSets {
        getByName("main").apply {
            manifest.srcFile("AndroidManifest.xml")
            java.srcDirs("src")
            aidl.srcDirs("src")
            renderscript.srcDirs("src")
            res.srcDirs("res")
            assets.srcDirs("assets")
            jniLibs.srcDirs("libs")
        }

    }
    packagingOptions {
        exclude("META-INF/robovm/ios/robovm.xml")
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


// called every time gradle gets executed, takes the native dependencies of
// the natives configuration, and extracts them to the proper libs/ folders
// so they get packed with the APK.
task("copyAndroidNatives") {
    doFirst {
        file("libs/armeabi/").mkdirs()
        file("libs/armeabi-v7a/").mkdirs()
        file("libs/arm64-v8a/").mkdirs()
        file("libs/x86_64/").mkdirs()
        file("libs/x86/").mkdirs()

        natives.files.forEach { jar ->
            var outputDir: File? = null
            if (jar.name.endsWith("natives-arm64-v8a.jar")) outputDir = file("libs/arm64-v8a")
            if (jar.name.endsWith("natives-armeabi-v7a.jar")) outputDir = file("libs/armeabi-v7a")
            if (jar.name.endsWith("natives-armeabi.jar")) outputDir = file("libs/armeabi")
            if (jar.name.endsWith("natives-x86_64.jar")) outputDir = file("libs/x86_64")
            if (jar.name.endsWith("natives-x86.jar")) outputDir = file("libs/x86")
            if (outputDir != null) {
                copy {
                    from(zipTree(jar))
                    into(outputDir) {
                        include("*.so")
                    }
                }
            }
        }
    }
}

tasks.whenTaskAdded {
    if (name.contains("package")) {
        dependsOn("copyAndroidNatives")
    }
}

task("run") {
    val localProperties = project.file("../local.properties")
    val properties = Properties()
    val path = if (localProperties.exists()) {
        properties.load(localProperties.inputStream())
        val sdkDir = properties.getProperty("sdk.dir")
        if (!sdkDir.isBlank()) {
            sdkDir
        } else {
            "\$System.env.ANDROID_HOME"
        }
    } else {
        "\$System.env.ANDROID_HOME"
    }

    val adb = "$path/platform-tools/adb"
    Runtime.getRuntime().exec(
        arrayOf(
            adb,
            "shell",
            "am",
            "start",
            "-n",
            "com.game.quillyjumper/com.game.quillyjumper.AndroidLauncher"
        )
    )
}
