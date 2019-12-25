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

// set resource directory to android assets folder
sourceSets {
    main {
        resources.srcDirs("../android/assets")
        java.srcDirs("src/main/kotlin")
    }
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_7
    targetCompatibility = JavaVersion.VERSION_1_7
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = Versions.jvmTarget
}
