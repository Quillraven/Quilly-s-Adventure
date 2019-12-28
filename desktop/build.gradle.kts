plugins {
    application
    kotlin("jvm")
    // use shadow/shadowJar task to create executable jar file of the game
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

application {
    mainClassName = "com.game.quillyjumper.DesktopLauncherKt"
}

dependencies {
    implementation(project(":core"))
    api("com.badlogicgames.gdx:gdx-backend-lwjgl:${rootProject.extra["gdxVersion"]}")
    api("com.badlogicgames.gdx:gdx-platform:${rootProject.extra["gdxVersion"]}:natives-desktop")
    api("com.badlogicgames.gdx:gdx-box2d-platform:${rootProject.extra["gdxVersion"]}:natives-desktop")
    api("com.badlogicgames.gdx:gdx-freetype-platform:${rootProject.extra["gdxVersion"]}:natives-desktop")
    api("org.jetbrains.kotlin:kotlin-stdlib:${rootProject.extra["kotlinVersion"]}")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_7
}

sourceSets {
    main {
        java.srcDirs("src/main/kotlin")
        resources.srcDirs("../android/assets")
    }
}
