plugins {
    application
    kotlin("jvm")
}

application {
    mainClass.set("${project.property("packageName")}.DesktopLauncherKt")
}

group = "${project.property("packageName")}"
version = "1.1"

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":core"))
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:${project.property("gdx")}")
    implementation("com.badlogicgames.gdx:gdx-platform:${project.property("gdx")}:natives-desktop")
    implementation("com.badlogicgames.gdx:gdx-box2d-platform:${project.property("gdx")}:natives-desktop")
    implementation("com.badlogicgames.gdx:gdx-freetype-platform:${project.property("gdx")}:natives-desktop")
}

java.sourceCompatibility = JavaVersion.valueOf("${project.property("java")}")

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "${project.property("jvmTarget")}"
    }
}

val assetsDir = rootProject.files("assets")
sourceSets {
    main {
        resources.srcDir(assetsDir)
    }
}

tasks {
    jar {
        from(files(sourceSets.main.get().output.classesDirs))
        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })

        archiveBaseName.set("${project.property("name")}")

        manifest {
            attributes["Main-Class"] = application.mainClass.get()
        }
    }
}
