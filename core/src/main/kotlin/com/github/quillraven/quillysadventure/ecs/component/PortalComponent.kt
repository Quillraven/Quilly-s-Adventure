package com.github.quillraven.quillysadventure.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import com.github.quillraven.quillysadventure.map.MapType
import ktx.ashley.get
import ktx.ashley.mapperFor

class PortalComponent : Component, Pool.Poolable {
    var portalID: Int = -1
    var active: Boolean = true
    var targetMap: MapType = MapType.TEST_MAP
    var targetPortal: Int = 0
    var targetOffsetX: Int = 0

    companion object {
        val mapper = mapperFor<PortalComponent>()
    }

    override fun reset() {
        portalID = -1
        active = true
        targetMap = MapType.TEST_MAP
        targetPortal = 0
    }
}

val Entity.portalCmp: PortalComponent
    get() = this[PortalComponent.mapper]
        ?: throw KotlinNullPointerException("Trying to access a portal component which is null")
