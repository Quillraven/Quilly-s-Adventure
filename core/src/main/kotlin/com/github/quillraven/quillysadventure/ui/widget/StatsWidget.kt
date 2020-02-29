package com.github.quillraven.quillysadventure.ui.widget

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import com.github.quillraven.quillysadventure.ui.Images
import com.github.quillraven.quillysadventure.ui.LabelStyles
import com.github.quillraven.quillysadventure.ui.get
import ktx.actors.onClick
import ktx.actors.plus
import ktx.actors.plusAssign
import ktx.actors.txt
import ktx.scene2d.Scene2DSkin

class StatsWidget(
    title: String,
    skillText: String,
    private val skin: Skin = Scene2DSkin.defaultSkin
) : WidgetGroup() {
    private val table: Table
    private val levelLabel = Label("", skin)
    private val xpLabel = Label("", skin)
    private val lifeLabel = Label("", skin)
    private val manaLabel = Label("", skin)
    private val damageLabel = Label("", skin)
    private val armorLabel = Label("", skin)
    private val skills = Array<VerticalGroup>(3)

    init {
        table = Table(skin).apply {
            background = skin[Images.DIALOG_LIGHT]

            defaults().left().padLeft(65f)
            add(Label(title, skin, LabelStyles.LARGE.name).apply {
                setAlignment(Align.center)
            }).center().expandX().pad(20f, 0f, 30f, 0f).row()
            add(levelLabel).row()
            add(xpLabel).padBottom(15f).row()
            add(lifeLabel).row()
            add(manaLabel).padBottom(15f).row()
            add(damageLabel).row()
            add(armorLabel).row()
            add(Label(skillText, skin, LabelStyles.LARGE.name).apply {
                setAlignment(Align.center)
            }).center().pad(10f, 0f, 10f, 0f).row()

            left()
            pack()

            // group is not rotated or scaled and therefore we do not need to transform every draw call
            // -> increased draw performance because we avoid flushing the batch
            isTransform = false
        }

        addActor(table)
        addActor(Image(skin[Images.BUTTON_CLOSE]).apply {
            setPosition(table.width - 5f, table.height - drawable.minHeight)
            onClick { this@StatsWidget += fadeOut(1f) + Actions.moveBy(-2000f, 0f) }
        })
    }

    fun addSkill(name: String, requirementText: String, image: Images) {
        val skillGroup = VerticalGroup().apply {
            this.addActor(Label("[BLACK]$name[]", skin))
            this.addActor(Label("[BLACK]$requirementText[]", skin))
            this.addActor(Image(skin[image]).apply {
                setColor(0.25f, 0.25f, 0.25f, 0.75f)
                this.touchable = Touchable.disabled
            })
        }
        skills.add(skillGroup)
        table.add(skillGroup)
    }

    fun activateSkill(skillIdx: Int) {
        if (skillIdx < skills.size) {
            skills[skillIdx].children[2].run {
                setColor(1f, 1f, 1f, 1f)
                this.touchable = Touchable.enabled
            }
        }
    }

    fun skill(skillIdx: Int): Actor? {
        if (skillIdx < skills.size) {
            return skills[skillIdx].children[2]
        }
        return null
    }

    fun updateLife(lifeText: String, life: Int, maxLife: Int): StatsWidget {
        lifeLabel.txt = "[BLACK]$lifeText: $life / $maxLife[]"
        return this
    }

    fun updateMana(manaText: String, mana: Int, maxMana: Int): StatsWidget {
        manaLabel.txt = "[BLACK]$manaText: $mana / $maxMana[]"
        return this
    }

    fun updateExperience(xpText: String, xpAbbreviation: String, xp: Int, xpNeeded: Int): StatsWidget {
        xpLabel.txt = "[BLACK]$xpText ($xpAbbreviation): $xp / $xpNeeded[]"
        return this
    }

    fun updateLevel(levelText: String, level: Int): StatsWidget {
        levelLabel.txt = "[BLACK]$levelText: $level[]"
        return this
    }

    fun updateDamage(damageText: String, damage: Int): StatsWidget {
        damageLabel.txt = "[BLACK]$damageText: $damage[]"
        return this
    }

    fun updateArmor(armorText: String, armor: Int): StatsWidget {
        armorLabel.txt = "[BLACK]$armorText: $armor[]"
        return this
    }

    override fun getWidth(): Float = table.width

    override fun getHeight(): Float = table.height
}
