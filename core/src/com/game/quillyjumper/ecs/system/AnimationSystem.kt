package com.game.quillyjumper.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityListener
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.utils.Array
import com.game.quillyjumper.UNIT_SCALE
import com.game.quillyjumper.assets.TextureAtlasAssets
import com.game.quillyjumper.assets.get
import com.game.quillyjumper.ecs.component.AnimationComponent
import com.game.quillyjumper.ecs.component.RenderComponent
import com.game.quillyjumper.graphics.Animation
import com.game.quillyjumper.graphics.AnimationType
import com.game.quillyjumper.graphics.ModelType
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.log.logger
import java.util.*

private val LOG = logger<AnimationSystem>()
private const val DEFAULT_REGION_KEY = "error"

/**
 * In order for the [AnimationSystem] to work you need to follow following conventions
 * when creating the texture atlas:
 * 1) name of region keys must be MODELTYPE/ANIMATIONTYPE where MODELTYPE is the
 * lower case name of the [ModelType] enum value and ANIMATIONTYPE is the lower case
 * name of the [AnimationType] enum value
 * 2) Regions that belong together must be indexed which means e.g. that a run animation
 * with 4 regions must have the file names run_0, run_1, run_2 and run_3
 *
 * Example for a player idle animation with two frames would be player/idle_0 and player/idle_1
 * as keys for the regions of the atlas.
 */
class AnimationSystem(assets: AssetManager) :
    IteratingSystem(allOf(AnimationComponent::class, RenderComponent::class).get()), EntityListener {
    private val animationFamily = allOf(AnimationComponent::class).get()
    private val animationCache = EnumMap<ModelType, EnumMap<AnimationType, Animation>>(ModelType::class.java)
    private val textureAtlas = assets[TextureAtlasAssets.GAME_OBJECTS]
    // default texture region must not be null because we always want to render at least something
    // even if the real animation is missing or wrongly defined
    private val defaultRegion = textureAtlas.findRegion(DEFAULT_REGION_KEY)!!
    private val defaultAnimation = Animation(
        ModelType.UNKNOWN,
        AnimationType.IDLE,
        Array<TextureAtlas.AtlasRegion>(TextureAtlas.AtlasRegion::class.java).apply { add(defaultRegion) }
    )

    override fun addedToEngine(engine: Engine) {
        super.addedToEngine(engine)
        engine.addEntityListener(animationFamily, this)
    }

    override fun removedFromEngine(engine: Engine) {
        super.removedFromEngine(engine)
        engine.removeEntityListener(this)
    }

    override fun entityAdded(entity: Entity) {
        // set default value for lateinit animation of AnimationComponent
        // it defaults to the error animation
        entity[AnimationComponent.mapper]?.animation = defaultAnimation
    }

    override fun entityRemoved(entity: Entity?) {}

    /**
     * Extension method for animation cache to simply retrieve animations by calling
     * animationCache[modelType][animationType]
     *
     * If the animation is not part of the cache yet then it gets loaded via the
     * [getAnimation] method.
     */
    private operator fun EnumMap<ModelType, EnumMap<AnimationType, Animation>>.get(
        modelType: ModelType,
        animationType: AnimationType
    ): Animation {
        val animationMap = this.computeIfAbsent(modelType) { EnumMap(AnimationType::class.java) }
        return animationMap.computeIfAbsent(animationType) { getAnimation(modelType, animationType) }
    }

    /**
     * Retrieves the regions for the animations according to the conventions described in the [AnimationSystem].
     * If there are no regions then a default animation is returned to render at least a default texture.
     * Otherwise, a new [Animation] instance is created with the regions from the texture atlas.
     */
    private fun getAnimation(modelType: ModelType, animationType: AnimationType): Animation {
        val regions =
            textureAtlas.findRegions("${modelType.name.toLowerCase()}/${animationType.name.toLowerCase()}")
        LOG.debug { "Creating animation for $modelType/$animationType. Found ${regions.size} regions" }

        // if no regions are found then return the error animation to render at least something
        if (regions.isEmpty) {
            LOG.error { "There are no regions for $modelType/$animationType" }
            return defaultAnimation
        }

        // regions found -> create new animation
        return Animation(
            modelType,
            animationType,
            regions
        )
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        entity[AnimationComponent.mapper]?.let { aniCmp ->
            // increase animation time to play animation
            aniCmp.animationTime += deltaTime

            // check if aniCmp has a valid model and if the animation has to be updated
            if (aniCmp.modelType != ModelType.UNKNOWN && (aniCmp.animationType != aniCmp.animation.animationType || aniCmp.modelType != aniCmp.animation.modelType)) {
                // animation update needed -> retrieve it from cache
                // if it is not part of the cache yet then the cache will create it
                aniCmp.animation = animationCache[aniCmp.modelType, aniCmp.animationType]
                // start animation from the beginning
                aniCmp.animationTime = 0f
            }

            // update sprite information with animation like texture, texture region, size, ...
            // the sprite will then be used for the rendering in the RenderSystem
            entity[RenderComponent.mapper]?.let { render ->
                render.sprite.apply {
                    var textureRegion = aniCmp.animation.getKeyFrame(aniCmp.animationTime, true)
                    if (textureRegion == null) {
                        LOG.error { "Could not retrieve textureRegion for ${aniCmp.modelType}/${aniCmp.animationType} at time ${aniCmp.animationTime}" }
                        textureRegion = defaultRegion
                    }

                    texture = textureRegion.texture
                    setRegion(textureRegion)
                    // keep aspect ratio of original texture and scale it to fit into the world units
                    setSize(textureRegion.regionWidth * UNIT_SCALE, textureRegion.regionHeight * UNIT_SCALE)
                    setOriginCenter()
                }
            }
        }
    }
}
