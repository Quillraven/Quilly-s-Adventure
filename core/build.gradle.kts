plugins {
    kotlin("jvm")
}

dependencies {
    api("com.badlogicgames.gdx:gdx:${rootProject.extra["gdxVersion"]}")
    api("com.badlogicgames.gdx:gdx-box2d:${rootProject.extra["gdxVersion"]}")
    api("com.badlogicgames.gdx:gdx-freetype:${rootProject.extra["gdxVersion"]}")
    api("com.badlogicgames.box2dlights:box2dlights:${rootProject.extra["box2DLightsVersion"]}")
    api("com.badlogicgames.ashley:ashley:${rootProject.extra["ashleyVersion"]}")
    api("com.badlogicgames.gdx:gdx-ai:${rootProject.extra["aiVersion"]}")
    api("org.jetbrains.kotlin:kotlin-stdlib:${rootProject.extra["kotlinVersion"]}")
    api("io.github.libktx:ktx-actors:${rootProject.extra["ktxVersion"]}")
    api("io.github.libktx:ktx-app:${rootProject.extra["ktxVersion"]}")
    api("io.github.libktx:ktx-ashley:${rootProject.extra["ktxVersion"]}")
    api("io.github.libktx:ktx-box2d:${rootProject.extra["ktxVersion"]}")
    api("io.github.libktx:ktx-collections:${rootProject.extra["ktxVersion"]}")
    api("io.github.libktx:ktx-freetype:${rootProject.extra["ktxVersion"]}")
    api("io.github.libktx:ktx-graphics:${rootProject.extra["ktxVersion"]}")
    api("io.github.libktx:ktx-inject:${rootProject.extra["ktxVersion"]}")
    api("io.github.libktx:ktx-log:${rootProject.extra["ktxVersion"]}")
    api("io.github.libktx:ktx-math:${rootProject.extra["ktxVersion"]}")
    api("io.github.libktx:ktx-scene2d:${rootProject.extra["ktxVersion"]}")
    api("io.github.libktx:ktx-style:${rootProject.extra["ktxVersion"]}")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_7
}

sourceSets {
    main {
        java.srcDirs("src/main/kotlin")
    }
}
