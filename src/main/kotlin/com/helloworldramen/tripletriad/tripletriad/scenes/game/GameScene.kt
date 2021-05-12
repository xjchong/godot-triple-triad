package com.helloworldramen.tripletriad.tripletriad.scenes.game

import GameEngine
import com.helloworldramen.tripletriad.tripletriad.ai.GameStateMCTSNode
import com.helloworldramen.tripletriad.tripletriad.ai.MCTS
import com.helloworldramen.tripletriad.tripletriad.models.*
import com.helloworldramen.tripletriad.tripletriad.scenes.board.BoardScene
import com.helloworldramen.tripletriad.tripletriad.scenes.card.PlayerCardScene
import com.helloworldramen.tripletriad.tripletriad.scenes.hand.HandScene
import godot.*
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.extensions.connect
import godot.extensions.getNodeAs
import godot.global.GD
import godot.signals.Signal

@RegisterClass
class GameScene: Node2D() {

	private val handScene1: HandScene by lazy { getNodeAs("HandScene1")!! }
	private val handScene2: HandScene by lazy { getNodeAs("HandScene2")!! }
	private val boardScene: BoardScene by lazy { getNodeAs("BoardScene")!! }
	private val testCardScene: PlayerCardScene by lazy { getNodeAs("TestCardScene")!! }

	private val gameEngine: GameEngine = GameEngine()
	private val ai: MCTS = MCTS()
	private var testPlayerCard: PlayerCard = PlayerCard(Card.Bomb, 0, true, false)

	@RegisterFunction
	override fun _ready() {
		testCardScene.connect("player_card_entered", this, "on_player_card_entered")
		testCardScene.connect("player_card_exited", this, "on_player_card_exited")
		testCardScene.bind(testPlayerCard)
	}

	private var hoveredCardScene: PlayerCardScene? = null
	private var grabbedCardScene: PlayerCardScene? = null
	@RegisterFunction
	fun onPlayerCardEntered(cardScene: PlayerCardScene) {
		GD.print("mouse enter $cardScene ${System.currentTimeMillis()}")
		if (hoveredCardScene == null) {
			hoveredCardScene = cardScene
		}
	}

	@RegisterFunction
	fun onPlayerCardExited(cardScene: PlayerCardScene) {
		GD.print("mouse exit $cardScene ${System.currentTimeMillis()}")
		hoveredCardScene = null
	}

	private var didRun = false
	private var isMousePrimaryPressed = false

	@RegisterFunction
	override fun _input(event: InputEvent) {
		if (isMousePrimaryPressed && event.isActionReleased("mouse_primary")) {
			isMousePrimaryPressed = false
			grabbedCardScene?.unhighlight()
			grabbedCardScene = null
		} else if (!isMousePrimaryPressed && event.isActionPressed("mouse_primary")) {
			isMousePrimaryPressed = true
			grabbedCardScene = hoveredCardScene
			grabbedCardScene?.highlight()
		}

		if (isMousePrimaryPressed && event is InputEventMouseMotion) {
			grabbedCardScene?.position = grabbedCardScene!!.position + event.relative
		}

		if (event.isActionPressed("ui_up")) {
			if (grabbedCardScene == null) {
				testCardScene.flip(false) {
					testPlayerCard = testPlayerCard.assignedToPlayer((testPlayerCard.playerId + 1) % 2)
					testCardScene.flip(false, newCard = testPlayerCard)
				}
			}
		} else if (event.isActionPressed("ui_right")) {
			if (grabbedCardScene == null) {
				testCardScene.flip {
					testPlayerCard = testPlayerCard.assignedToPlayer((testPlayerCard.playerId + 1) % 2)
					testCardScene.flip(newCard = testPlayerCard)
				}
			}
		}

		if (!didRun && event.isActionPressed("ui_accept")) {
			didRun = true

			setupAiGame()
		} else if (didRun && event.isActionPressed("ui_accept")) {
			nextTurn()
		}
	}

	fun bind(gameState: GameState) {
		gameState.players.find { it.id == 0 }?.let {
			handScene1.bind(it)
		}
		gameState.players.find { it.id == 1 }?.let {
			handScene2.bind(it)
		}
		boardScene.bind(gameState.board)
	}

	private fun setupAiGame() {
		gameEngine.startGame(listOf(
				Player(1, arrayOf(Card.Ananta, Card.AlexanderPrime, Card.Dodo, Card.Mandragora, Card.Amaljaa)),
				Player(0, arrayOf(Card.Adamantoise, Card.Ananta, Card.Bomb, Card.Coeurl, Card.Sabotender))),
				advancedRules = listOf(AllOpen, Descension, SuddenDeath),
				shouldShufflePlayers = false
		)

		bind(gameEngine.nextState())
	}

	private fun nextTurn() {
		val nextState = gameEngine.nextState()

		if (nextState.isGameOver()) return

		val move = getAiMove(ai, nextState)
		gameEngine.playMove(move)

		bind(gameEngine.nextState())
	}

	private fun testAi() {
		val gameEngine = GameEngine()

		gameEngine.startGame(listOf(
				Player(1, arrayOf(Card.Ananta, Card.AlexanderPrime, Card.Dodo, Card.Mandragora, Card.Amaljaa)),
				Player(0, arrayOf(Card.Adamantoise, Card.Ananta, Card.Bomb, Card.Coeurl, Card.Sabotender))),
				advancedRules = listOf(AllOpen, Descension, SuddenDeath),
				shouldShufflePlayers = false
		)

		val ai = MCTS()
		var nextState = gameEngine.nextState()

		while (!nextState.isGameOver()) {
			val move = if (nextState.nextPlayer().id == 0) {
				bind(nextState)
				getAiMove(ai, nextState)
//            getPlayerMove(nextState)
			} else {
				getAiMove(ai, nextState)
			}

			gameEngine.playMove(move)
			nextState = gameEngine.nextState()

			if (nextState.nextPlayer().id == 1) {
				bind(nextState)
			}
		}

		if (nextState.isGameOver() && nextState.nextPlayer().id == 0) {
			bind(nextState)
		}
	}

	private fun getAiMove(ai: MCTS, gameState: GameState): Move {
		val bestNode = ai.getBestNode(GameStateMCTSNode(gameState), null, 2000)

		return (bestNode as GameStateMCTSNode).moves.first()
	}
}
