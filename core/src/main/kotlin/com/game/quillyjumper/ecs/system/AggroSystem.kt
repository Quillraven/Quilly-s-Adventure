package com.game.quillyjumper.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.Vector2
import com.game.quillyjumper.ecs.component.*
import ktx.ashley.allOf

class AggroSystem :
    IteratingSystem(allOf(AggroComponent::class, TransformComponent::class, MoveComponent::class).get()) {

    private fun isWithinAttackRange(pos: Vector2, size: Vector2, aggroPos: Vector2, aggroSize: Vector2, range: Float) =
        (pos.x - range <= aggroPos.x + aggroSize.x && pos.x >= aggroPos.x + aggroSize.x) // aggro entity is on left attack range
                || (pos.x + size.x <= aggroPos.x && pos.x + size.x + range >= aggroPos.x) // aggro entity is on right attack range

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val entities = entity.aggroCmp.aggroEntities
        if (entities.size > 0) {
            // entities are within aggro range -> move towards/attack the first entity
            val transform = entity.transfCmp
            val attack = entity.attackCmp
            val aggroTransform = entities[0].transfCmp
            if (attack.canAttack() && isWithinAttackRange(
                    transform.position,
                    transform.size,
                    aggroTransform.position,
                    aggroTransform.size,
                    attack.range
                )
            ) {
                // aggro entity is within attack range
                attack.order = AttackOrder.START
            }

            if (aggroTransform.position.x < transform.position.x) {
                // aggro entity is on the left side
                entity.moveCmp.order = MoveOrder.LEFT
            } else {
                // aggro entity is on the right side
                entity.moveCmp.order = MoveOrder.RIGHT
            }
        } else {
            // no entities in range -> do nothing
            entity.moveCmp.order = MoveOrder.NONE
        }
    }
}