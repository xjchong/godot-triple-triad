package com.helloworldramen.tripletriad.tripletriad.scenes.board

import com.helloworldramen.tripletriad.tripletriad.models.Board
import com.helloworldramen.tripletriad.tripletriad.scenes.card.PlayerCardScene
import godot.Spatial
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.extensions.getNodeAs
import models.Position

@RegisterClass
class BoardScene: Spatial() {

	val cardScenes: Map<Position, PlayerCardScene> by lazy {
		mapOf(
				Position.TOP_LEFT to getNodeAs("Container/Panel/PlayerCardScene1")!!,
				Position.TOP to getNodeAs("Container/Panel/PlayerCardScene2")!!,
				Position.TOP_RIGHT to getNodeAs("Container/Panel/PlayerCardScene3")!!,
				Position.LEFT to getNodeAs("Container/Panel/PlayerCardScene4")!!,
				Position.CENTER to getNodeAs("Container/Panel/PlayerCardScene5")!!,
				Position.RIGHT to getNodeAs("Container/Panel/PlayerCardScene6")!!,
				Position.BOTTOM_LEFT to getNodeAs("Container/Panel/PlayerCardScene7")!!,
				Position.BOTTOM to getNodeAs("Container/Panel/PlayerCardScene8")!!,
				Position.BOTTOM_RIGHT to getNodeAs("Container/Panel/PlayerCardScene9")!!
		)
	}

	@RegisterFunction
	override fun _ready() {
		super._ready()
	}

	fun bind(board: Board) {
		board.playerCards.forEach { (position, playerCard) ->
			cardScenes[position]?.bind(playerCard)
		}
	}
}
