package com.game.quillyjumper.ecs.component

import com.badlogic.ashley.core.Component
import com.game.quillyjumper.map.MapType
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