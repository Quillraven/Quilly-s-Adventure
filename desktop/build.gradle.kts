plugins {
    application
    kotlin("jvm")
}

application {
    mainClass.set("${Apps.packageName}.DesktopLauncherKt")
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

val assetsDir = rootProject.files("assets")
sourceSets {
    main {
        resources.srcDir(assetsDir)
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = Versions.jvm
}

tasks {
    named<Jar>("jar") {
        from(files(sourceSets.main.get().output.classesDirs))
        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })

        archiveBaseName.set(Apps.name)

        manifest {
            attributes["Main-Class"] = application.mainClass.get()
        }
    }
}
