package com.helloworldramen.tripletriad.tripletriad.scenes.hand

import com.helloworldramen.tripletriad.tripletriad.models.Player
import com.helloworldramen.tripletriad.tripletriad.scenes.card.PlayerCardScene
import godot.Spatial
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.extensions.getNodeAs

@RegisterClass
class HandScene: Spatial() {

	private val card1: PlayerCardScene by lazy { getNodeAs("Card1")!! }
	private val card2: PlayerCardScene by lazy { getNodeAs("Card2")!! }
	private val card3: PlayerCardScene by lazy { getNodeAs("Card3")!! }
	private val card4: PlayerCardScene by lazy { getNodeAs("Card4")!! }
	private val card5: PlayerCardScene by lazy { getNodeAs("Card5")!! }

	private var initialPlayer: Player? = null

	@RegisterFunction
	override fun _ready() {
//		val player = Player(0, arrayOf(Card.Bomb, Card.Coeurl, Card.Dodo, Card.Mandragora, Card.Sabotender))
//
//		bind(player)
	}

	fun bind(player: Player) {
		val cardScenes = listOf(card1, card2, card3, card4, card5)

		if (player.cards.size == 5 || initialPlayer == null) {
			initialPlayer = player
		}

		initialPlayer?.let {
			for ((index, initialCard) in it.cards.withIndex()) {
				val currentCard = player.cards.find { card -> card.id == initialCard.id }

				cardScenes[index].bind(currentCard)
			}
		}
	}
}
