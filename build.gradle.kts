group = Versions.packageName
version = Versions.version

buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}")
        classpath("com.android.tools.build:gradle:${Versions.androidGradlePlugin}")
    }

    repositories {
        google()
        mavenCentral()
        jcenter()
    }
}

allprojects {
    group = project.group
    version = project.version

    repositories {
        repositories {
            google()
            mavenCentral()
            jcenter()
        }
    }
}
