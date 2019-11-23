plugins {
    kotlin("jvm") version "1.3.60" apply false //root project does not need kotlin it's just here to inherit version
}

group = "com.game"
version = "1.0"

allprojects {

    group = project.group
    version = project.version

    ext {
        set("appName", "Quilly Jumper")
        set("gdxVersion", "1.9.10")
        set("roboVMVersion", "2.3.7")
        set("box2DLightsVersion", "1.4")
        set("ashleyVersion", "1.7.3")
        set("aiVersion", "1.8.2")
        set("ktxVersion", "1.9.10-b2")
        set("kotlinVersion", "1.3.60")
    }

    repositories {
        repositories {
            maven("https://oss.sonatype.org/content/repositories/snapshots/")
            jcenter()
            google()
        }
    }
}
