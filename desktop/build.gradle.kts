import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm")
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

val versions = rootProject.ext

application {
    mainClassName = "${versions["packageName"]}.DesktopLauncherKt"
}

dependencies {
    implementation(project(":core"))
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl:${versions["gdx"]}")
    implementation("com.badlogicgames.gdx:gdx-freetype-platform:${versions["gdx"]}:natives-desktop")
    implementation("com.badlogicgames.gdx:gdx-platform:${versions["gdx"]}:natives-desktop")
    implementation("com.badlogicgames.gdx:gdx-box2d-platform:${versions["gdx"]}:natives-desktop")
}

tasks {
    jar {
        dependsOn("shadowJar") // also build fat jar when building jar
        archiveClassifier.set("original")
    }
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = versions["jvmTarget"] as String
}
