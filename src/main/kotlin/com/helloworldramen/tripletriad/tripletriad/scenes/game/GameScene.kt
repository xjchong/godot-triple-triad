package com.helloworldramen.tripletriad.tripletriad.scenes.game

import com.helloworldramen.tripletriad.tripletriad.models.Board
import com.helloworldramen.tripletriad.tripletriad.models.Card
import com.helloworldramen.tripletriad.tripletriad.models.GameState
import com.helloworldramen.tripletriad.tripletriad.models.Player
import com.helloworldramen.tripletriad.tripletriad.scenes.board.BoardScene
import com.helloworldramen.tripletriad.tripletriad.scenes.hand.HandScene
import godot.Spatial
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.extensions.getNodeAs

@RegisterClass
class GameScene: Spatial() {

	private val handScene1: HandScene by lazy { getNodeAs("HandScene1")!! }
	private val handScene2: HandScene by lazy { getNodeAs("HandScene2")!! }
	private val boardScene: BoardScene by lazy { getNodeAs("BoardScene")!! }

	@RegisterFunction
	override fun _ready() {
		val player1 = Player(0, arrayOf(Card.Bomb, Card.Coeurl, Card.Dodo, Card.Mandragora, Card.Sabotender))
		val player2 = Player(1, arrayOf(Card.Amaljaa, Card.Ahriman, Card.AlexanderPrime, Card.Dodo, Card.Coeurl))
		val gameState = GameState(Board.standardInstance(), listOf(player1, player2))

		bind(gameState)
	}

	fun bind(gameState: GameState) {
		handScene1.bind(gameState.players.first())
		handScene2.bind(gameState.players[1])
		boardScene.bind(gameState.board)
	}
}
