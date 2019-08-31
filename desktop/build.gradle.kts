import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.github.johnrengelman.shadow") version "5.1.0"
    application
    kotlin("jvm")
}

val assetsDir = rootProject.file("assets")
val gdxVersion = project.ext["gdxVersion"] as String

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":core"))
    api("com.badlogicgames.gdx:gdx-backend-lwjgl:$gdxVersion")
    api("com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-desktop")
    api("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop")
    api("com.badlogicgames.gdx:gdx-box2d-platform:$gdxVersion:natives-desktop")
}

application {
    mainClassName = "com.game.quillyjumper.desktop.DesktopLauncherKt"
}

sourceSets {
    main {
        resources.srcDir(rootProject.file("assets"))
    }
}

tasks {
    "run"(JavaExec::class) {
        dependsOn(classes)
        main = project.application.mainClassName
        classpath = sourceSets.main.get().runtimeClasspath
        standardInput = System.`in`
        workingDir = assetsDir
        isIgnoreExitValue = true
    }

    task("debug", JavaExec::class) {
        dependsOn(classes)
        main = project.application.mainClassName
        classpath = sourceSets.main.get().runtimeClasspath
        standardInput = System.`in`
        workingDir = assetsDir
        isIgnoreExitValue = true
        debug = true
    }

    jar {
        archiveClassifier.set("original")
    }

    "shadowJar"(ShadowJar::class) {
        archiveFileName.set("${archiveBaseName.orNull}-${archiveVersion.orNull}.${archiveExtension.orNull}")
        archiveVersion.set(project.version as String)
    }
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
