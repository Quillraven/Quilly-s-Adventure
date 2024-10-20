package com.github.quillraven.quillysadventure.teavm

import com.github.quillraven.quillysadventure.VIRTUAL_H
import com.github.quillraven.quillysadventure.VIRTUAL_W
import com.github.quillraven.quillysadventure.ability.Ability
import com.github.quillraven.quillysadventure.ability.FireballEffect
import com.github.quillraven.quillysadventure.ecs.system.SaveState
import com.github.quillraven.quillysadventure.trigger.Trigger
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionCreateCharacter
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionDamageCharacter
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionDeactivateTrigger
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionDelay
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionEnablePortal
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionMoveCharacter
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionMoveOrderCharacter
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionPlayAnimationCharacter
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionPlayMusic
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionResetState
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionSelectActivatingCharacter
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionSelectCharacterByType
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionSetPlayerInput
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionShowDialog
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionWaitCreatedCharacterDeath
import com.github.quillraven.quillysadventure.trigger.condition.TriggerConditionIsEntityAlive
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
    @JvmStatic
    fun main(arguments: Array<String>) {
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
        TeaReflectionSupplier.addReflectionClass(TriggerActionPlayMusic::class.java)
        TeaReflectionSupplier.addReflectionClass(TriggerActionCreateCharacter::class.java)
        TeaReflectionSupplier.addReflectionClass(TriggerActionSetPlayerInput::class.java)
        TeaReflectionSupplier.addReflectionClass(TriggerActionDelay::class.java)
        TeaReflectionSupplier.addReflectionClass(TriggerActionResetState::class.java)
        TeaReflectionSupplier.addReflectionClass(TriggerActionEnablePortal::class.java)
        TeaReflectionSupplier.addReflectionClass(TriggerActionWaitCreatedCharacterDeath::class.java)
        TeaReflectionSupplier.addReflectionClass(TriggerActionSelectCharacterByType::class.java)
        TeaReflectionSupplier.addReflectionClass(TriggerActionSelectActivatingCharacter::class.java)
        TeaReflectionSupplier.addReflectionClass(TriggerActionMoveCharacter::class.java)
        TeaReflectionSupplier.addReflectionClass(TriggerActionDamageCharacter::class.java)
        TeaReflectionSupplier.addReflectionClass(TriggerActionDeactivateTrigger::class.java)
        TeaReflectionSupplier.addReflectionClass(TriggerActionShowDialog::class.java)
        TeaReflectionSupplier.addReflectionClass(TriggerActionMoveOrderCharacter::class.java)
        TeaReflectionSupplier.addReflectionClass(TriggerActionPlayAnimationCharacter::class.java)
        TeaReflectionSupplier.addReflectionClass(SaveState::class.java)
        TeaReflectionSupplier.addReflectionClass(FireballEffect::class.java)
        TeaReflectionSupplier.addReflectionClass(TriggerConditionIsEntityAlive::class.java)

        val tool = TeaBuilder.config(teaBuildConfiguration)
        tool.mainClass = "com.github.quillraven.quillysadventure.teavm.TeaVMLauncher"
        tool.optimizationLevel = TeaVMOptimizationLevel.FULL
        tool.setObfuscated(true)
        TeaBuilder.build(tool)
    }
}
