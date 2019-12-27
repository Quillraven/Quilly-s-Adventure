import org.jetbrains.kotlin.konan.properties.loadProperties

plugins {
    kotlin("jvm") version "1.3.61" apply false // root project does not need Kotlin plugin since it does not contain any source
}

loadProperties(project.file("versions.properties").absolutePath).forEach { key, value ->
    ext.set(key as String, value)
}

allprojects {
    repositories {
        jcenter()
    }
}
