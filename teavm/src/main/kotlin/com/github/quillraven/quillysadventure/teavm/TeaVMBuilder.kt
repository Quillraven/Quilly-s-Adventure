package com.github.quillraven.quillysadventure.teavm

import com.github.quillraven.quillysadventure.VIRTUAL_H
import com.github.quillraven.quillysadventure.VIRTUAL_W
import com.github.quillraven.quillysadventure.ability.Ability
import com.github.quillraven.quillysadventure.trigger.Trigger
import com.github.quillraven.quillysadventure.ui.action.ScaleToRegionWidth
import com.github.xpenatan.gdx.backends.teavm.config.AssetFileHandle
import com.github.xpenatan.gdx.backends.teavm.config.TeaBuildConfiguration
import com.github.xpenatan.gdx.backends.teavm.config.TeaBuilder
import com.github.xpenatan.gdx.backends.teavm.config.plugins.TeaReflectionSupplier
import com.github.xpenatan.gdx.backends.teavm.gen.SkipClass
import org.teavm.vm.TeaVMOptimizationLevel
import java.io.File

/** Builds the TeaVM/HTML application. */
@SkipClass
object TeaVMBuilder {
    @JvmStatic fun main(arguments: Array<String>) {
        val teaBuildConfiguration = TeaBuildConfiguration().apply {
            assetsPath.add(AssetFileHandle("../assets"))
            webappPath = File("build/dist").canonicalPath
            // Register any extra classpath assets here:
            // additionalAssetsClasspathFiles += "com/github/quillraven/quillysadventure/asset.extension"
            htmlTitle = "Quilly's Adventure"
            htmlWidth = VIRTUAL_W
            htmlHeight = VIRTUAL_H
        }

        // Register any classes or packages that require reflection here:
        TeaReflectionSupplier.addReflectionClass("com.github.quillraven.quillysadventure.ecs.component")
        TeaReflectionSupplier.addReflectionClass(Ability::class.java)
        TeaReflectionSupplier.addReflectionClass(Trigger::class.java)
        TeaReflectionSupplier.addReflectionClass(ScaleToRegionWidth::class.java)

        val tool = TeaBuilder.config(teaBuildConfiguration)
        tool.mainClass = "com.github.quillraven.quillysadventure.teavm.TeaVMLauncher"
        tool.optimizationLevel = TeaVMOptimizationLevel.FULL
        tool.setObfuscated(true)
        TeaBuilder.build(tool)
    }
}
