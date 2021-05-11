package com.helloworldramen.tripletriad.tripletriad.scenes.card

import com.helloworldramen.tripletriad.tripletriad.models.Card
import com.helloworldramen.tripletriad.tripletriad.models.CardRarity
import com.helloworldramen.tripletriad.tripletriad.models.CardType
import com.helloworldramen.tripletriad.tripletriad.models.PlayerCard
import godot.AspectRatioContainer
import godot.ColorRect
import godot.Label
import godot.Spatial
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.core.Color
import godot.extensions.getNodeAs
import godot.global.GD

@RegisterClass
class PlayerCardScene: Spatial() {

	private val container: AspectRatioContainer by lazy { getNodeAs("Container")!! }
	private val colorRect: ColorRect by lazy { getNodeAs(colorRectPath())!! }
	private val rarityLabel: Label by lazy { getNodeAs(colorRectPath("RarityLabel"))!! }
	private val typeLabel: Label by lazy { getNodeAs(colorRectPath("TypeLabel"))!! }
	private val nameLabel: Label by lazy { getNodeAs(colorRectPath("NameLabel"))!! }
	private val northLabel: Label by lazy { getNodeAs(colorRectPath("NorthLabel"))!! }
	private val eastLabel: Label by lazy { getNodeAs(colorRectPath("EastLabel"))!! }
	private val southLabel: Label by lazy { getNodeAs(colorRectPath("SouthLabel"))!! }
	private val westLabel: Label by lazy { getNodeAs(colorRectPath("WestLabel"))!! }

	private fun colorRectPath(path: String = ""): String {
		return "Container/MarginContainer/ColorRect${if (path.isEmpty()) "" else "/$path"}"
	}

	@RegisterFunction
	override fun _ready() {
//		GD.print("Hello world!")

//		val playerCard = PlayerCard(card = Card.AlexanderPrime, playerId = 0, isPlayable = false)
//		bind(playerCard)
	}

	fun bind(playerCard: PlayerCard?) {
		if (playerCard?.card == Card.UNKNOWN) {
			bindUnknown()
		} else {
			rarityLabel.text = when(playerCard?.card?.rarity) {
				CardRarity.Common -> "*"
				CardRarity.Uncommon -> "**"
				CardRarity.Rare -> "***"
				CardRarity.Epic -> "****"
				CardRarity.Legendary -> "*****"
				else -> "?????"
			}

			typeLabel.text = when(playerCard?.card?.type) {
				CardType.Beastman -> "B"
				CardType.Garland -> "G"
				CardType.Primal -> "P"
				CardType.Scions -> "S"
				else -> ""
			}

			nameLabel.text = playerCard?.card?.name?.take(7) ?: "???????"

			northLabel.text = playerCard?.n().getValueString()
			eastLabel.text = playerCard?.e().getValueString()
			southLabel.text = playerCard?.s().getValueString()
			westLabel.text = playerCard?.w().getValueString()
		}

		colorRect.color = when {
			playerCard?.isPlayable == false -> Color.gray
			playerCard?.playerId == 0 -> Color.cadetblue
			playerCard?.playerId == 1 -> Color.mediumvioletred
			else -> Color.black
		}

		if (playerCard == null) container.hide() else container.show()
	}

	private fun bindUnknown() {
		rarityLabel.text = ""
		typeLabel.text = ""
		nameLabel.text = ""
		northLabel.text = ""
		eastLabel.text = ""
		southLabel.text = ""
		westLabel.text = ""
	}

	private fun Int?.getValueString(): String {
		return when {
			this == null -> "?"
			this >= 10 -> "A"
			this <= 0 -> "1"
			else -> this.toString()
		}
	}
}
