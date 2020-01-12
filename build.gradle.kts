import io.gitlab.arturbosch.detekt.Detekt

plugins {
    id("io.gitlab.arturbosch.detekt") version "1.3.1"
}

tasks.withType<Detekt> {
    jvmTarget = Versions.jvm
}

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    detekt {
        failFast = true
        autoCorrect = true
        config = rootProject.files("config/detekt.yml")

        reports {
            html.enabled = true
            xml.enabled = false
            txt.enabled = false
        }
    }

    version = Apps.versionName

    repositories {
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        jcenter()
        google()
    }
}
