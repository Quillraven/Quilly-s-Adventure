plugins {
    kotlin("jvm") version Versions.kotlin apply false // root project does not need Kotlin plugin since it does not contain any source
}

allprojects {
    repositories {
        jcenter()
    }
}

group = Versions.packageName
version = Versions.version
