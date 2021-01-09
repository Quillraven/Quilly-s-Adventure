plugins {
    kotlin("jvm")
}

dependencies {
    api("com.badlogicgames.gdx:gdx:${project.property("gdx")}")
    api("com.badlogicgames.gdx:gdx-box2d:${project.property("gdx")}")
    api("com.badlogicgames.gdx:gdx-freetype:${project.property("gdx")}")
    api("com.badlogicgames.box2dlights:box2dlights:${project.property("box2DLight")}")
    api("com.badlogicgames.ashley:ashley:${project.property("ashley")}")
    api("com.badlogicgames.gdx:gdx-ai:${project.property("gdxAI")}")
    api("org.jetbrains.kotlin:kotlin-stdlib")
    api("io.github.libktx:ktx-actors:${project.property("ktx")}")
    api("io.github.libktx:ktx-app:${project.property("ktx")}")
    api("io.github.libktx:ktx-ashley:${project.property("ktx")}")
    // async assets requires coroutines
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${project.property("coroutines")}")
    api("io.github.libktx:ktx-assets-async:${project.property("ktx")}")
    api("io.github.libktx:ktx-box2d:${project.property("ktx")}")
    api("io.github.libktx:ktx-collections:${project.property("ktx")}")
    api("io.github.libktx:ktx-freetype:${project.property("ktx")}")
    api("io.github.libktx:ktx-graphics:${project.property("ktx")}")
    api("io.github.libktx:ktx-inject:${project.property("ktx")}")
    api("io.github.libktx:ktx-i18n:${project.property("ktx")}")
    api("io.github.libktx:ktx-log:${project.property("ktx")}")
    api("io.github.libktx:ktx-math:${project.property("ktx")}")
    api("io.github.libktx:ktx-preferences:${project.property("ktx")}")
    api("io.github.libktx:ktx-scene2d:${project.property("ktx")}")
    api("io.github.libktx:ktx-style:${project.property("ktx")}")
    api("io.github.libktx:ktx-tiled:${project.property("ktx")}")

    testImplementation("org.junit.jupiter:junit-jupiter:5.6.2")
    testImplementation("io.mockk:mockk:1.10.0")
}

java.sourceCompatibility = JavaVersion.valueOf("${project.property("java")}")

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "${project.property("jvmTarget")}"
    }
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}
