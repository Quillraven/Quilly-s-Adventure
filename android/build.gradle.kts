plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    compileSdkVersion(29)
    sourceSets {
        named("main") {
            java.srcDirs("src/main/kotlin")
            assets.srcDirs(project.file("../assets"))
            jniLibs.srcDirs("libs")
        }
    }

    defaultConfig {
        applicationId = "${project.property("packageName")}"
        minSdkVersion(24)
        targetSdkVersion(29)
        versionCode = 2
        versionName = "1.1"
    }

    buildTypes {
        named("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.valueOf("${project.property("java")}")
        targetCompatibility = JavaVersion.valueOf("${project.property("java")}")
    }

    kotlinOptions {
        jvmTarget = "${project.property("jvmTarget")}"
    }
}

val natives: Configuration by configurations.creating

dependencies {
    implementation(project(":core"))
    api("com.badlogicgames.gdx:gdx-backend-android:${project.property("gdx")}")
    natives("com.badlogicgames.gdx:gdx-platform:${project.property("gdx")}:natives-armeabi")
    natives("com.badlogicgames.gdx:gdx-platform:${project.property("gdx")}:natives-armeabi-v7a")
    natives("com.badlogicgames.gdx:gdx-platform:${project.property("gdx")}:natives-arm64-v8a")
    natives("com.badlogicgames.gdx:gdx-platform:${project.property("gdx")}:natives-x86")
    natives("com.badlogicgames.gdx:gdx-platform:${project.property("gdx")}:natives-x86_64")
    api("com.badlogicgames.gdx:gdx-box2d:${project.property("gdx")}")
    natives("com.badlogicgames.gdx:gdx-box2d-platform:${project.property("gdx")}:natives-armeabi")
    natives("com.badlogicgames.gdx:gdx-box2d-platform:${project.property("gdx")}:natives-armeabi-v7a")
    natives("com.badlogicgames.gdx:gdx-box2d-platform:${project.property("gdx")}:natives-arm64-v8a")
    natives("com.badlogicgames.gdx:gdx-box2d-platform:${project.property("gdx")}:natives-x86")
    natives("com.badlogicgames.gdx:gdx-box2d-platform:${project.property("gdx")}:natives-x86_64")
    api("com.badlogicgames.gdx:gdx-freetype:${project.property("gdx")}")
    natives("com.badlogicgames.gdx:gdx-freetype-platform:${project.property("gdx")}:natives-armeabi")
    natives("com.badlogicgames.gdx:gdx-freetype-platform:${project.property("gdx")}:natives-armeabi-v7a")
    natives("com.badlogicgames.gdx:gdx-freetype-platform:${project.property("gdx")}:natives-arm64-v8a")
    natives("com.badlogicgames.gdx:gdx-freetype-platform:${project.property("gdx")}:natives-x86")
    natives("com.badlogicgames.gdx:gdx-freetype-platform:${project.property("gdx")}:natives-x86_64")
    api("com.badlogicgames.box2dlights:box2dlights:${project.property("box2DLight")}")
    api("com.badlogicgames.ashley:ashley:${project.property("ashley")}")
    api("com.badlogicgames.gdx:gdx-ai:${project.property("gdxAI")}")
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
