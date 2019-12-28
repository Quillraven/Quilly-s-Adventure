buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://plugins.gradle.org/m2/")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        jcenter()
        google()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:${Versions.androidGradlePlugin}")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}")
    }
}

allprojects {
    version = Apps.versionName

    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
        google()
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://oss.sonatype.org/content/repositories/releases/")
    }
}
