plugins {
    kotlin("jvm") version Versions.kotlin apply false
}

allprojects {
    version = Apps.versionName

    repositories {
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        jcenter()
        google()
    }
}
