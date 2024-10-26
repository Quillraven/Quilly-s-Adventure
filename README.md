[![Master](https://img.shields.io/github/actions/workflow/status/quillraven/Quilly-s-Adventure/verify.yml?branch=master)](https://github.com/Quillraven/Quilly-s-Adventure/actions)
[![Kotlin](https://img.shields.io/badge/kotlin-2.0.21-red.svg)](http://kotlinlang.org/)
[![LibGDX](https://img.shields.io/badge/libgdx-1.13.0-blue.svg)](https://libgdx.badlogicgames.com/)
[![LibKTX](https://img.shields.io/badge/libktx-1.12.1--rc2-orange.svg)](https://libktx.github.io/)

![image](https://user-images.githubusercontent.com/93260/77850937-2a9b9200-71d6-11ea-9517-29f7c7d9a276.png)

# Quilly's Adventure

### Update 2024

I updated the project to the newest (to date October 2024) LibGDX, Kotlin, 
Gradle and LibKTX versions. I added a new TeaVM backend which now allows the game
to be run in the browser. [Here](https://quillraven.github.io/Quilly-s-Adventure/) is the link.
TeaVM required a few code changes (mainly reflection issues). The original code 
can be found in a separate branch called `original`.

### General

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

![image](https://user-images.githubusercontent.com/93260/77858359-55e8a600-7203-11ea-848c-39f90af4e4a6.png)
