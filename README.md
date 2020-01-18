[![Kotlin](https://img.shields.io/badge/kotlin-1.3.61-red.svg)](http://kotlinlang.org/)
[![LibGDX](https://img.shields.io/badge/libgdx-1.9.10-blue.svg)](https://libgdx.badlogicgames.com/)
[![LibKTX](https://img.shields.io/badge/libktx-1.9.10--SNAPSHOT-orange.svg)](https://libgdx.badlogicgames.com/)

[![JetBrains](https://upload.wikimedia.org/wikipedia/commons/thumb/1/1a/JetBrains_Logo_2016.svg/100px-JetBrains_Logo_2016.svg.png)](https://www.jetbrains.com/?from=QuillyJumper)

# Quilly's Adventure

This is a streaming project on twitch.tv/quillraven

We are going to develop a simple adventure using LibGDX, Kotlin and LibKTX extensions.
In addition we will use Tiled as a mapeditor, box2d as a physic engine and ashley as our entity component system.

If you want to use the framework, feel free to use it. I will try to update the [wiki](https://github.com/Quillraven/QuillyJumper/wiki) as best as possible so that you know, how to use it.

More information will follow once the project is growing a little bit bigger ;)

---

### Import to Intellij

Due to the reason that Intellij does not like it when using an android plugin in a subproject (it makes all other projects an android project in that case), the android project became its own main project.

Therefore, you need to import the main `build.gradle.kts` first to get the `desktop` and `core` module running.
Afterwards open your module page `File -> Project Structure -> Modules` and add the android folder as a new module using the `+` icon (import as gradle). 
After that you should be ready to go! 😊
