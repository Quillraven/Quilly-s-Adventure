import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm")
}

application {
    mainClassName = "${Versions.packageName}.DesktopLauncherKt"
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}")
    implementation(project(":core"))
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl:${Versions.gdx}")
    implementation("com.badlogicgames.gdx:gdx-freetype-platform:${Versions.gdx}:natives-desktop")
    implementation("com.badlogicgames.gdx:gdx-platform:${Versions.gdx}:natives-desktop")
    implementation("com.badlogicgames.gdx:gdx-box2d-platform:${Versions.gdx}:natives-desktop")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = Versions.jvmTarget
}
