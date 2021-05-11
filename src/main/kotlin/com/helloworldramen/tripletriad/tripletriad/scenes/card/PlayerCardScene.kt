package com.helloworldramen.tripletriad.tripletriad.scenes.card

import com.helloworldramen.tripletriad.tripletriad.models.Card
import com.helloworldramen.tripletriad.tripletriad.models.CardRarity
import com.helloworldramen.tripletriad.tripletriad.models.CardType
import com.helloworldramen.tripletriad.tripletriad.models.PlayerCard
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

	private val backgroundColorRect: ColorRect by lazy { getNodeAs("Container/BackgroundColorRect")!! }
	private val rarityLabel: Label by lazy { getNodeAs("Container/BackgroundColorRect/RarityLabel")!! }
	private val typeLabel: Label by lazy { getNodeAs("Container/BackgroundColorRect/TypeLabel")!! }
	private val nameLabel: Label by lazy { getNodeAs("Container/BackgroundColorRect/NameLabel")!! }
	private val northLabel: Label by lazy { getNodeAs("Container/BackgroundColorRect/NorthLabel")!! }
	private val eastLabel: Label by lazy { getNodeAs("Container/BackgroundColorRect/EastLabel")!! }
	private val southLabel: Label by lazy { getNodeAs("Container/BackgroundColorRect/SouthLabel")!! }
	private val westLabel: Label by lazy { getNodeAs("Container/BackgroundColorRect/WestLabel")!! }

	@RegisterFunction
	override fun _ready() {
		GD.print("Hello world!")

		val playerCard = PlayerCard(card = Card.Amaljaa, playerId = 0, isPlayable = true)
		bind(playerCard)
	}

	fun bind(playerCard: PlayerCard?) {
		rarityLabel.text = when(playerCard?.card?.rarity) {
			CardRarity.Common -> "*"
			CardRarity.Uncommon -> "**"
			CardRarity.Rare -> "***"
			CardRarity.Epic -> "****"
			CardRarity.Legendary -> "*****"
			else -> ""
		}

		typeLabel.text = when(playerCard?.card?.type) {
			CardType.Beastman -> "B"
			CardType.Garland -> "G"
			CardType.Primal -> "P"
			CardType.Scions -> "S"
			else -> ""
		}

		nameLabel.text = playerCard?.card?.name?.take(7) ?: ""

		northLabel.text = playerCard?.n().getValueString()
		eastLabel.text = playerCard?.e().getValueString()
		southLabel.text = playerCard?.s().getValueString()
		westLabel.text = playerCard?.w().getValueString()

		backgroundColorRect.color = when {
			playerCard?.isPlayable == false -> Color.gray
			playerCard?.playerId == 0 -> Color.cadetblue
			playerCard?.playerId == 1 -> Color.chartreuse
			else -> Color.transparent
		}
	}

	private fun Int?.getValueString(): String {
		return when {
			this == null -> ""
			this >= 10 -> "A"
			this <= 0 -> "1"
			else -> this.toString()
		}
	}
}
