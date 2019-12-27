rootProject.name = "android"
include(":core")
project(":core").projectDir = file("../core")

pluginManagement {
    repositories {
        @Suppress("UnstableApiUsage")
        gradlePluginPortal()
        jcenter()
        google()
    }

    resolutionStrategy {
        eachPlugin {
            val pluginId = requested.id
            if (pluginId.id == "com.android.application") {
                // Why are you not on the Gradle plugin portal Android!?!
                useModule("com.android.tools.build:gradle:${requested.version}")
            }
        }
    }
}