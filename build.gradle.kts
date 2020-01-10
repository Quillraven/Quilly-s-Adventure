plugins {
    kotlin("jvm") version "1.3.61" apply false
}

allprojects {
    version = Apps.versionName

    repositories {
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        jcenter()
        google()
    }
}
