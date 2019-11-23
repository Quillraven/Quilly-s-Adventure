import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val assetsDir = rootProject.file("assets")
val gdxVersion = project.ext["gdxVersion"] as String

plugins {
    id("com.github.johnrengelman.shadow") version "5.1.0"
    application
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":core"))
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl:$gdxVersion")
    implementation("com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-desktop")
    implementation("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop")
    implementation("com.badlogicgames.gdx:gdx-box2d-platform:$gdxVersion:natives-desktop")
}


tasks {
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

application {
    mainClassName = "com.game.quillyjumper.desktop.DesktopLauncherKt"
}
