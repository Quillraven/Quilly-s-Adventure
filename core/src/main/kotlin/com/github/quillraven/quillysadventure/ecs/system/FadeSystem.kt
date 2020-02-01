package com.github.quillraven.quillysadventure.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.MathUtils
import com.github.quillraven.quillysadventure.ecs.component.FadeInComponent
import com.github.quillraven.quillysadventure.ecs.component.RemoveComponent
import com.github.quillraven.quillysadventure.ecs.component.RenderComponent
import com.github.quillraven.quillysadventure.ecs.component.fadeinCmp
import com.github.quillraven.quillysadventure.ecs.component.renderCmp
import ktx.ashley.allOf
import ktx.ashley.exclude
import kotlin.math.min

class FadeSystem :
    IteratingSystem(allOf(FadeInComponent::class, RenderComponent::class).exclude(RemoveComponent::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        with(entity.fadeinCmp) {
            fadeTime = min(1f, fadeTime + (deltaTime / maxFadeTime))
            entity.renderCmp.sprite.setAlpha(
                min(targetAlpha, MathUtils.lerp(startAlpha, targetAlpha, fadeTime))
            )

            if (fadeTime >= 1f) {
                // fade finished -> remove component to stop fading
                entity.remove(FadeInComponent::class.java)
            }
        }
    }
}
