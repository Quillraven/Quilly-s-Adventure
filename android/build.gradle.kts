import java.util.*

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
}

val gdxVersion = project.ext["gdxVersion"] as String
val aiVersion = rootProject.ext["aiVersion"] as String
val ashleyVersion = rootProject.ext["ashleyVersion"] as String
val ktxVersion = rootProject.ext["ktxVersion"] as String
val box2DLightsVersion = rootProject.ext["box2DLightsVersion"] as String

repositories {
    google()
    jcenter()
}

val natives: Configuration by configurations.register("natives")

dependencies {
    implementation(project(":core"))
    api("com.badlogicgames.gdx:gdx-backend-android:$gdxVersion")
    api("com.badlogicgames.gdx:gdx-freetype:$gdxVersion")
    natives("com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-armeabi")
    natives("com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-armeabi-v7a")
    natives("com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-arm64-v8a")
    natives("com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-x86")
    natives("com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-x86_64")
    natives("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-armeabi")
    natives("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-armeabi-v7a")
    natives("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-arm64-v8a")
    natives("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-x86")
    natives("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-x86_64")
    api("com.badlogicgames.gdx:gdx-box2d:$gdxVersion")
    natives("com.badlogicgames.gdx:gdx-box2d-platform:$gdxVersion:natives-armeabi")
    natives("com.badlogicgames.gdx:gdx-box2d-platform:$gdxVersion:natives-armeabi-v7a")
    natives("com.badlogicgames.gdx:gdx-box2d-platform:$gdxVersion:natives-arm64-v8a")
    natives("com.badlogicgames.gdx:gdx-box2d-platform:$gdxVersion:natives-x86")
    natives("com.badlogicgames.gdx:gdx-box2d-platform:$gdxVersion:natives-x86_64")
    api("com.badlogicgames.gdx:gdx-ai:$aiVersion")
    api("com.badlogicgames.ashley:ashley:$ashleyVersion")
    api("com.badlogicgames.box2dlights:box2dlights:$box2DLightsVersion")
}

android {
    buildToolsVersion = "29.0.2"
    compileSdkVersion(29)

    defaultConfig {
        applicationId = "com.game.quillyjumper"
        minSdkVersion(24)
        targetSdkVersion(29)
        versionCode = 1
        versionName = "1.0"
    }

    sourceSets {
        getByName("main") {
            manifest.srcFile("AndroidManifest.xml")
            java.srcDir(file("src/java"))
            res.srcDir(file("res"))
            assets.srcDir(rootProject.file("assets"))
            jniLibs.srcDir(file("libs"))
        }
    }

    packagingOptions {
        exclude("META-INF/robovm/ios/robovm.xml")
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
tasks {
    task("copyAndroidNatives") {
        doFirst {
            println(configurations["natives"].files)
            configurations["natives"].files.forEach { jar ->
                var outputDir: File? = null
                if (jar.name.endsWith("natives-arm64-v8a.jar")) outputDir = file("libs/arm64-v8a")
                if (jar.name.endsWith("natives-armeabi-v7a.jar")) outputDir = file("libs/armeabi-v7a")
                if (jar.name.endsWith("natives-armeabi.jar")) outputDir = file("libs/armeabi")
                if (jar.name.endsWith("natives-x86_64.jar")) outputDir = file("libs/x86_64")
                if (jar.name.endsWith("natives-x86.jar")) outputDir = file("libs/x86")
                outputDir?.let {
                    copy {
                        mkdir(outputDir)
                        from(zipTree(jar))
                        into(outputDir)
                        include("*.so")
                    }
                }
            }
        }
    }

    task("run", Exec::class) {
        var path: String? = null
        val localProperties = rootProject.file("local.properties")
        path = if (localProperties.exists()) {
            val properties = Properties()
            localProperties.inputStream().also { instr ->
                properties.load(instr)
            }
            properties.getProperty("sdk.dir") ?: System.getenv("ANDROID_HOME")
        } else {
            System.getenv("ANDROID_HOME")
        }

        val adb = "$path/platform-tools/adb"
        commandLine(adb, "shell", "am", "start", "-n", "com.game.quillyjumper/com.game.quillyjumper.AndroidLauncher")
    }

    this.whenTaskAdded {
        if ("package" in name) {
            dependsOn("copyAndroidNatives")
        }
    }
}

