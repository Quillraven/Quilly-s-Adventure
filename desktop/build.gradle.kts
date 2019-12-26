plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":core"))
    api("com.badlogicgames.gdx:gdx-backend-lwjgl:${rootProject.extra["gdxVersion"]}")
    api("com.badlogicgames.gdx:gdx-platform:${rootProject.extra["gdxVersion"]}:natives-desktop")
    api("com.badlogicgames.gdx:gdx-box2d-platform:${rootProject.extra["gdxVersion"]}:natives-desktop")
    api("com.badlogicgames.gdx:gdx-freetype-platform:${rootProject.extra["gdxVersion"]}:natives-desktop")
    api("org.jetbrains.kotlin:kotlin-stdlib:${rootProject.extra["kotlinVersion"]}")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_7
}

sourceSets {
    main {
        java.srcDirs("src/main/kotlin")
        resources.srcDirs("../android/assets")
    }
}

project.ext.set("mainClassName", "com.game.quillyjumper.DesktopLauncherKt")
project.ext.set("assetsDir", File("../android/assets"))

task("run", JavaExec::class) {
    dependsOn("classes")
    main = project.ext["mainClassName"] as String
    classpath = sourceSets.getByName("main").runtimeClasspath
    standardInput = System.`in`
    workingDir = project.ext["assetsDir"] as File
    isIgnoreExitValue = true
}

task("debug", JavaExec::class) {
    dependsOn("classes")
    main = project.ext["mainClassName"] as String
    classpath = sourceSets.getByName("main").runtimeClasspath
    standardInput = System.`in`
    workingDir = project.ext["assetsDir"] as File
    isIgnoreExitValue = true
    debug = true
}

task("dist", Jar::class) {
    dependsOn("classes")
    manifest {
        attributes["Main-Class"] = project.ext["mainClassName"] as String
    }
    from(configurations.compileClasspath.map { if ((it as File).isDirectory) it else zipTree(it) })
    // with(jar) <- TODO how to convert this to kotlin dsl
}
