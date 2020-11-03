import io.gitlab.arturbosch.detekt.Detekt

plugins {
    kotlin("multiplatform") version Versions.kotlin apply false
    id("com.android.application") version Versions.androidGradlePlugin apply (false)
    id("io.gitlab.arturbosch.detekt").version(Versions.detekt)
}

allprojects {
    version = Apps.versionName

    apply(plugin = "io.gitlab.arturbosch.detekt")

    repositories {
        jcenter()
        google()
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
    }

    detekt {
        failFast = true // fail build on any finding
        buildUponDefaultConfig = true // preconfigure defaults
        config = rootProject.files("config/detekt.yml")

        reports {
            html.enabled = true // observe findings in your browser with structure and code snippets
            xml.enabled = false // checkstyle like format mainly for integrations like Jenkins
            txt.enabled = false // similar to the console output
        }
    }
}

tasks {
    withType<Detekt> {
        // Target version of the generated JVM bytecode. It is used for type resolution.
        this.jvmTarget = Versions.jvm
    }
}
