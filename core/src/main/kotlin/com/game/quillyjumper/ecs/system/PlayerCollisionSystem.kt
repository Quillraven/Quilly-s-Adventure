package com.game.quillyjumper.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.StringBuilder
import com.game.quillyjumper.ecs.component.*
import com.game.quillyjumper.ecs.floatingText
import com.game.quillyjumper.map.MapManager
import ktx.ashley.allOf
import ktx.log.logger

private val LOG = logger<PlayerCollisionSystem>()

class PlayerCollisionSystem(private val mapManager: MapManager) :
    IteratingSystem(allOf(PlayerComponent::class, CollisionComponent::class).get()) {
    private val itemInfoBuilder = StringBuilder(64)

    override fun processEntity(entity: Entity, deltaTime: Float) {
        entity.collCmp.run {
            // loop through all colliding entities
            entities.forEach { collidingEntity ->
                when (collidingEntity.typeCmp.type) {
                    EntityType.PORTAL -> {
                        // player collides with a portal -> move player to new location/map
                        collidingEntity.portalCmp.run { mapManager.setMap(targetMap, targetPortal, targetOffsetX) }
                        // ignore any other collisions for that frame because the player got moved to a new map
                        return
                    }
                    EntityType.ITEM -> {
                        // player is colliding wiht an item -> add powerup to player
                        itemCollision(entity, collidingEntity)
                    }
                    else -> {
                        // do nothing
                    }
                }
            }
        }
    }

    private fun itemCollision(player: Entity, item: Entity) {
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
}