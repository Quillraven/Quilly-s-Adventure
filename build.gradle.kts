buildscript {
    val kotlinVersion by extra("1.3.61")
    extra.set("gdxVersion", "1.9.10")
    extra.set("roboVMVersion", "2.3.7")
    extra.set("box2DLightsVersion", "1.4")
    extra.set("ashleyVersion", "1.7.3")
    extra.set("aiVersion", "1.8.2")
    extra.set("ktxVersion", "1.9.10-b2")
    val agpVersion by extra("3.5.3")

    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://plugins.gradle.org/m2/")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        jcenter()
        google()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:$agpVersion")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    }
}

allprojects {
    version = "1.0"

    ext {
        set("appName", "Quilly Jumper")
    }

    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
        google()
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://oss.sonatype.org/content/repositories/releases/")
    }
}
