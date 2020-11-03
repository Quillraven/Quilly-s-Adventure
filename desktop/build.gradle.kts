plugins {
    application
    kotlin("jvm")
    // use shadow/shadowJar task to create executable jar file of the game
    id("com.github.johnrengelman.shadow") version Versions.shadowJar
}

application {
    mainClassName = "${Apps.packageName}.DesktopLauncherKt"
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":core"))
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:${Versions.gdx}")
    implementation("com.badlogicgames.gdx:gdx-platform:${Versions.gdx}:natives-desktop")
    implementation("com.badlogicgames.gdx:gdx-box2d-platform:${Versions.gdx}:natives-desktop")
    implementation("com.badlogicgames.gdx:gdx-freetype-platform:${Versions.gdx}:natives-desktop")
}

configure<JavaPluginConvention> {
    sourceCompatibility = Versions.java
    targetCompatibility = Versions.java
}

sourceSets {
    main {
        resources.srcDir(rootProject.files("assets"))
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = Versions.jvm
}

tasks {
    shadowJar {
        archiveBaseName.set(Apps.name)
        archiveVersion.set(Apps.versionName)
        archiveClassifier.set("")
    }
}
