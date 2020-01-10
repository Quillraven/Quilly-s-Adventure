include(":core")
project(":core").projectDir = file("../core")

pluginManagement {
    repositories {
        gradlePluginPortal()
        jcenter()
        google()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id.startsWith("com.android")) {
                useModule("com.android.tools.build:gradle:3.5.3")
            }
        }
    }
}

buildscript {
    dependencies {
        classpath(files("../buildSrc/build/classes/kotlin/main"))
    }
}