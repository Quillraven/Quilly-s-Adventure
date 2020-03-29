plugins {
    kotlin("jvm")
}

dependencies {
    api("com.badlogicgames.gdx:gdx:${Versions.gdx}")
    api("com.badlogicgames.gdx:gdx-box2d:${Versions.gdx}")
    api("com.badlogicgames.gdx:gdx-freetype:${Versions.gdx}")
    api("com.badlogicgames.box2dlights:box2dlights:${Versions.box2DLight}")
    api("com.badlogicgames.ashley:ashley:${Versions.ashley}")
    api("com.badlogicgames.gdx:gdx-ai:${Versions.gdxAI}")
    api("org.jetbrains.kotlin:kotlin-stdlib")
    api("io.github.libktx:ktx-actors:${Versions.ktx}")
    api("io.github.libktx:ktx-app:${Versions.ktx}")
    api("io.github.libktx:ktx-ashley:${Versions.ktx}")
    api("io.github.libktx:ktx-box2d:${Versions.ktx}")
    api("io.github.libktx:ktx-collections:${Versions.ktx}")
    api("io.github.libktx:ktx-freetype:${Versions.ktx}")
    api("io.github.libktx:ktx-graphics:${Versions.ktx}")
    api("io.github.libktx:ktx-inject:${Versions.ktx}")
    api("io.github.libktx:ktx-i18n:${Versions.ktx}")
    api("io.github.libktx:ktx-log:${Versions.ktx}")
    api("io.github.libktx:ktx-math:${Versions.ktx}")
    api("io.github.libktx:ktx-preferences:${Versions.ktx}")
    api("io.github.libktx:ktx-scene2d:${Versions.ktx}")
    api("io.github.libktx:ktx-style:${Versions.ktx}")
    api("io.github.libktx:ktx-tiled:${Versions.ktx}")

    testImplementation("org.junit.jupiter:junit-jupiter:${Versions.junit}")
    testImplementation("io.mockk:mockk:${Versions.mockk}")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

configure<JavaPluginConvention> {
    sourceCompatibility = Versions.java
    targetCompatibility = Versions.java
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = Versions.jvm
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}
