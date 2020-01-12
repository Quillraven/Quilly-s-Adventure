package com.game.quillyjumper.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.StringBuilder
import com.game.quillyjumper.assets.SoundAssets
import com.game.quillyjumper.audio.AudioService
import com.game.quillyjumper.ecs.component.*
import com.game.quillyjumper.ecs.findPortal
import com.game.quillyjumper.ecs.floatingText
import com.game.quillyjumper.ecs.isRemoved
import com.game.quillyjumper.event.GameEventManager
import com.game.quillyjumper.map.Map
import com.game.quillyjumper.map.MapChangeListener
import com.game.quillyjumper.map.MapManager
import ktx.ashley.allOf
import ktx.log.logger

private val LOG = logger<PlayerCollisionSystem>()

class PlayerCollisionSystem(
        private val mapManager: MapManager,
        private val audioService: AudioService,
        private val gameEventManager: GameEventManager
) :
        IteratingSystem(allOf(PlayerComponent::class, CollisionComponent::class).get()), MapChangeListener {
    private val itemInfoBuilder = StringBuilder(64)
    private var lastSavepoint: Entity? = null
    private var lastTrigger: Entity? = null

    override fun addedToEngine(engine: Engine?) {
        gameEventManager.addMapChangeListener(this)
        super.addedToEngine(engine)
    }

    override fun removedFromEngine(engine: Engine?) {
        gameEventManager.removeMapChangeListener(this)
        super.removedFromEngine(engine)
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        entity.collCmp.run {
            // loop through all colliding entities
            entities.forEach { collidingEntity ->
                if (collidingEntity.isRemoved()) {
                    // ignore entities that are getting removed at the end of the frame
                    return@forEach
                }

                @Suppress("OptionalWhenBraces")
                when (collidingEntity.typeCmp.type) {
                    EntityType.PORTAL -> {
                        // player collides with a portal -> move player to new location/map if portal is active
                        collidingEntity.portalCmp.run {
                            if (active) {
                                if (targetMap == mapManager.currentMap()) {
                                    engine.findPortal(targetPortal) {
                                        entity.physicCmp.body.setTransform(it.transfCmp.position, 0f)
                                    }
                                } else {
                                    mapManager.setMap(targetMap, targetPortal, targetOffsetX)
                                }
                                // ignore any other collisions for that frame because the player got moved to a new map
                                return
                            }
                        }
                    }
                    EntityType.ITEM -> {
                        // player is colliding with an item -> add powerup to player
                        itemCollision(entity, collidingEntity)
                    }
                    EntityType.SAVE_POINT -> {
                        if (collidingEntity.flags == ENTITY_FLAG_SAVE_POINT_NOT_ACTIVE) {
                            lastSavepoint?.flags = ENTITY_FLAG_SAVE_POINT_NOT_ACTIVE
                            collidingEntity.flags = ENTITY_FLAG_SAVE_POINT_ACTIVE
                            activateSavePoint(collidingEntity)
                        }
                    }
                    EntityType.TRIGGER -> {
                        if (lastTrigger != entity) {
                            lastTrigger = entity
                            gameEventManager.dispatchPlayerTriggerContact(entity, collidingEntity)
                        }
                    }
                    else -> {
                        // do nothing
                    }
                }
            }
        }
    }

    private fun itemCollision(player: Entity, item: Entity) {
        audioService.play(SoundAssets.POWER_UP_0)
        itemInfoBuilder.clear()

        // apply item bonus stats to player stats
        with(player.statsCmp) {
            val itemStats = item.statsCmp
            if (itemStats.life != 0f) {
                maxLife += itemStats.life
                // heal player to maximum when maximum life changes
                life = maxLife
                itemInfoBuilder.append("Max life increased by ")
                itemInfoBuilder.append(itemStats.life.toInt())
            }
            if (itemStats.mana != 0f) {
                maxMana += itemStats.mana
                // heal player to maximum when mana changes
                mana = maxMana
                itemInfoBuilder.append("Max mana increased by ")
                itemInfoBuilder.append(itemStats.mana.toInt())
            }

            LOG.debug { "Life ($maxLife), Mana ($maxMana)" }
        }

        // show information to player about the changed stats
        with(player.transfCmp) {
            engine.floatingText(
                    position.x,
                    position.y + size.y,
                    FontType.DEFAULT,
                    itemInfoBuilder,
                    Color.FIREBRICK,
                    0f,
                    -1f,
                    3f
            )
        }

        // remove the item from the game
        item.add(engine.createComponent(RemoveComponent::class.java))
    }

    private fun activateSavePoint(savepoint: Entity) {
        lastSavepoint = savepoint
        audioService.play(SoundAssets.CHECK_POINT)
        gameEventManager.dispatchGameActivateSavepointEvent(savepoint)
    }

    override fun mapChange(newMap: Map) {
        lastTrigger = null
        lastSavepoint = null
    }
}
