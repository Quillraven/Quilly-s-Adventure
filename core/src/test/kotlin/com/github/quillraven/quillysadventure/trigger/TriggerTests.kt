package com.github.quillraven.quillysadventure.trigger

import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.Gdx
import com.github.quillraven.quillysadventure.ecsEngine
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionDelay
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionEnablePortal
import com.github.quillraven.quillysadventure.trigger.condition.TriggerConditionIsEntityAlive
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TriggerTests {
    private val engine = PooledEngine()

    @BeforeEach
    fun setup() {
        Gdx.app = mockk()
        every { Gdx.app.ecsEngine } returns engine
    }

    @Test
    fun `execute trigger with no condition and no actions`() {
        val trigger = Trigger()

        assertEquals(true, trigger.update(0f))
    }

    @Test
    fun `add conditions to trigger`() {
        var wasExecuted = false
        lateinit var aliveCondition1: TriggerConditionIsEntityAlive
        lateinit var aliveCondition2: TriggerConditionIsEntityAlive
        val trigger = Trigger().conditions {
            aliveCondition1 = condition<TriggerConditionIsEntityAlive> {
                tmxMapID = 0
                checkAlive = false
            }
            aliveCondition2 = condition<TriggerConditionIsEntityAlive> {
                tmxMapID = 1
                checkAlive = true
            }
            wasExecuted = true
        }

        assertTrue(wasExecuted)
        assertEquals(2, trigger.conditions.size())
        assertEquals(aliveCondition1, trigger.conditions[0])
        assertEquals(aliveCondition2, trigger.conditions[1])
    }

    @Test
    fun `add actions to trigger`() {
        var wasExecuted = false
        lateinit var delayAction: TriggerActionDelay
        lateinit var portalAction: TriggerActionEnablePortal
        val trigger = Trigger().actions {
            delayAction = action<TriggerActionDelay> { delay = 5f }
            portalAction = action<TriggerActionEnablePortal> {
                portalID = 1
            }
            wasExecuted = true
        }

        assertTrue(wasExecuted)
        assertEquals(2, trigger.actions.size())
        assertEquals(delayAction, trigger.actions[0])
        assertEquals(portalAction, trigger.actions[1])
    }

    @Test
    fun `execute trigger without conditions`() {
        val trigger = Trigger().actions {
            action<TriggerActionDelay> { delay = 1f }
            action<TriggerActionEnablePortal> { portalID = 0 }
        }

        assertTrue(trigger.checkConditions())
        assertTrue(trigger.update(1f))
    }

    @Test
    fun `execute trigger actions with delay`() {
        val trigger = Trigger().actions {
            action<TriggerActionDelay> { delay = 1f }
            action<TriggerActionEnablePortal> { portalID = 0 }
        }

        assertTrue(trigger.checkConditions())
        assertFalse(trigger.update(0.5f))
    }

    @Test
    fun `check trigger conditions`() {
        val trigger = Trigger().conditions {
            condition<TriggerConditionIsEntityAlive> {
                tmxMapID = 0
                checkAlive = true
            }
        }

        assertFalse(trigger.checkConditions())
    }
}
