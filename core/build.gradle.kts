import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

val versions = rootProject.ext
dependencies {
    implementation(kotlin("stdlib-jdk8"))
    api("com.badlogicgames.gdx:gdx:${versions["gdx"]}")
    api("com.badlogicgames.gdx:gdx-box2d:${versions["gdx"]}")
    api("com.badlogicgames.gdx:gdx-ai:${versions["gdxAI"]}")
    api("com.badlogicgames.gdx:gdx-freetype:${versions["gdx"]}")
    api("com.badlogicgames.ashley:ashley:${versions["ashley"]}")
    api("com.badlogicgames.box2dlights:box2dlights:${versions["box2DLight"]}")
    api("io.github.libktx:ktx-actors:${versions["ktx"]}")
    api("io.github.libktx:ktx-app:${versions["ktx"]}")
    api("io.github.libktx:ktx-ashley:${versions["ktx"]}")
    api("io.github.libktx:ktx-box2d:${versions["ktx"]}")
    api("io.github.libktx:ktx-collections:${versions["ktx"]}")
    api("io.github.libktx:ktx-freetype:${versions["ktx"]}")
    api("io.github.libktx:ktx-graphics:${versions["ktx"]}")
    api("io.github.libktx:ktx-inject:${versions["ktx"]}")
    api("io.github.libktx:ktx-log:${versions["ktx"]}")
    api("io.github.libktx:ktx-math:${versions["ktx"]}")
    api("io.github.libktx:ktx-scene2d:${versions["ktx"]}")
    api("io.github.libktx:ktx-style:${versions["ktx"]}")
}

sourceSets {
    main {
        resources.srcDir(rootProject.files("assets"))
    }
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_7
    targetCompatibility = JavaVersion.VERSION_1_7
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = versions["jvmTarget"] as String
}
