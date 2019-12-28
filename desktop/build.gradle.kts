plugins {
    application
    kotlin("jvm")
    // use shadow/shadowJar task to create executable jar file of the game
    id("com.github.johnrengelman.shadow") version Versions.shadowJar
}

application {
    mainClassName = "com.game.quillyjumper.DesktopLauncherKt"
}

dependencies {
    implementation(project(":core"))
    api("com.badlogicgames.gdx:gdx-backend-lwjgl:${Versions.gdx}")
    api("com.badlogicgames.gdx:gdx-platform:${Versions.gdx}:natives-desktop")
    api("com.badlogicgames.gdx:gdx-box2d-platform:${Versions.gdx}:natives-desktop")
    api("com.badlogicgames.gdx:gdx-freetype-platform:${Versions.gdx}:natives-desktop")
    api("org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}")
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

tasks {
    shadowJar {
        archiveBaseName.set(Apps.name)
        archiveVersion.set(Apps.versionName)
        archiveClassifier.set("")
    }
}
