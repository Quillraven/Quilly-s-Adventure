import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}
val gdxVersion = project.ext["gdxVersion"] as String
val aiVersion = project.ext["aiVersion"] as String
val ashleyVersion = project.ext["ashleyVersion"] as String
val ktxVersion = project.ext["ktxVersion"] as String
val box2DLightsVersion = project.ext["box2DLightsVersion"] as String

dependencies {
    implementation(kotlin("stdlib"))
    api("com.badlogicgames.gdx:gdx:$gdxVersion")
    api("com.badlogicgames.gdx:gdx-box2d:$gdxVersion")
    api("com.badlogicgames.gdx:gdx-ai:$aiVersion")
    api("com.badlogicgames.gdx:gdx-freetype:$gdxVersion")
    api("com.badlogicgames.ashley:ashley:$ashleyVersion")
    api("com.badlogicgames.box2dlights:box2dlights:$box2DLightsVersion")
    api("io.github.libktx:ktx-actors:$ktxVersion")
    api("io.github.libktx:ktx-app:$ktxVersion")
    api("io.github.libktx:ktx-ashley:$ktxVersion")
    api("io.github.libktx:ktx-box2d:$ktxVersion")
    api("io.github.libktx:ktx-freetype:$ktxVersion")
    api("io.github.libktx:ktx-graphics:$ktxVersion")
    api("io.github.libktx:ktx-inject:$ktxVersion")
    api("io.github.libktx:ktx-log:$ktxVersion")
    api("io.github.libktx:ktx-math:$ktxVersion")
    api("io.github.libktx:ktx-scene2d:$ktxVersion")
    api("io.github.libktx:ktx-style:$ktxVersion")
}

sourceSets {
    main {
        resources.srcDir(rootProject.file("assets"))
    }
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}