import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}")
    api("com.badlogicgames.gdx:gdx:${Versions.gdx}")
    api("com.badlogicgames.gdx:gdx-box2d:${Versions.gdx}")
    api("com.badlogicgames.gdx:gdx-ai:${Versions.gdxAI}")
    api("com.badlogicgames.gdx:gdx-freetype:${Versions.gdx}")
    api("com.badlogicgames.ashley:ashley:${Versions.ashley}")
    api("com.badlogicgames.box2dlights:box2dlights:${Versions.box2DLight}")
    api("io.github.libktx:ktx-actors:${Versions.ktx}")
    api("io.github.libktx:ktx-app:${Versions.ktx}")
    api("io.github.libktx:ktx-ashley:${Versions.ktx}")
    api("io.github.libktx:ktx-box2d:${Versions.ktx}")
    api("io.github.libktx:ktx-collections:${Versions.ktx}")
    api("io.github.libktx:ktx-freetype:${Versions.ktx}")
    api("io.github.libktx:ktx-graphics:${Versions.ktx}")
    api("io.github.libktx:ktx-inject:${Versions.ktx}")
    api("io.github.libktx:ktx-log:${Versions.ktx}")
    api("io.github.libktx:ktx-math:${Versions.ktx}")
    api("io.github.libktx:ktx-scene2d:${Versions.ktx}")
    api("io.github.libktx:ktx-style:${Versions.ktx}")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_7
    targetCompatibility = JavaVersion.VERSION_1_7
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = Versions.jvmTarget
}
