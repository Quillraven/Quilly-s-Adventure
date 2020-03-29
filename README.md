![Master](https://github.com/Quillraven/Quilly-s-Adventure/workflows/Master/badge.svg)
[![Kotlin](https://img.shields.io/badge/kotlin-1.3.71-red.svg)](http://kotlinlang.org/)
[![LibGDX](https://img.shields.io/badge/libgdx-1.9.10-blue.svg)](https://libgdx.badlogicgames.com/)
[![LibKTX](https://img.shields.io/badge/libktx-1.9.10--SNAPSHOT-orange.svg)](https://libktx.github.io/)

[![JetBrains](https://upload.wikimedia.org/wikipedia/commons/thumb/1/1a/JetBrains_Logo_2016.svg/100px-JetBrains_Logo_2016.svg.png)](https://www.jetbrains.com/?from=QuillyJumper)

![image](https://user-images.githubusercontent.com/93260/77850937-2a9b9200-71d6-11ea-9517-29f7c7d9a276.png)

# Quilly's Adventure

This is a streaming project of my [twitch](twitch.tv/quillraven) channel.

We are going to develop a simple adventure using LibGDX, Kotlin and LibKTX extensions.
In addition we will use Tiled as a mapeditor, box2d as a physic engine and ashley as our entity component system.

If you want to use the framework, feel free to use it. I will try to update the [wiki](https://github.com/Quillraven/QuillyJumper/wiki) as best as possible so that you know, how to use it.

As of 2020-03-29 the first version of the game is finished including all important game mechanics and two
fully functional maps to discover including a boss fight.

![image](https://user-images.githubusercontent.com/93260/77850959-528af580-71d6-11ea-8b4e-20fadddb6053.png)

The story ends after the boss fight for now. The idea is that a new passage opens in the cave which allows you to
move forward to new areas to look for your girlfriend. But that part is not done yet and only god knows when this
part will be finished ;)

Until then feel free to check out the game or use the framework behind it for your own games!

![image](https://user-images.githubusercontent.com/93260/77850983-764e3b80-71d6-11ea-8f14-27e9c596cda4.png)

---

### Import to Intellij

Due to the reason that Intellij does not like it when using an android plugin in a subproject (it makes all other projects an android project in that case), the android project became its own main project.

Therefore, you need to import the main `build.gradle.kts` first to get the `desktop` and `core` module running.
Afterwards open your module page `File -> Project Structure -> Modules` and add the android folder as a new module using the `+` icon (import as gradle). 
After that you should be ready to go! 😊
