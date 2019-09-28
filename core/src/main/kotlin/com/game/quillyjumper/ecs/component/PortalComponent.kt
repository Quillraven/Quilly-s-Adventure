package com.game.quillyjumper.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.game.quillyjumper.map.MapType
import ktx.ashley.get
import ktx.ashley.mapperFor

class PortalComponent(
    var targetMap: MapType = MapType.TEST_MAP,
    var targetPortal: Int = 0,
    var targetOffsetX: Int = 0
) : Component {
    companion object {
        val mapper = mapperFor<PortalComponent>()
    }
}

val Entity.portalCmp: PortalComponent
    get() = this[PortalComponent.mapper]
        ?: throw KotlinNullPointerException("Trying to access a portal component which is null")