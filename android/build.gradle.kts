import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    compileSdkVersion(29)

    defaultConfig {
        applicationId = Versions.packageName
        // min version is 26 to support isGame and appCategory in AndroidManifest.xml
        minSdkVersion(26)
        targetSdkVersion(29)
        versionCode = Versions.androidVersionCode
        versionName = Versions.version
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_7
        targetCompatibility = JavaVersion.VERSION_1_7
    }

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
}

val natives by configurations.register("natives")

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}")
    implementation(project(":core"))
    // libgdx specific stuff
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
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = Versions.jvmTarget
}
