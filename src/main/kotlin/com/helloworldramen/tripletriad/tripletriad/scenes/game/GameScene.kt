package com.helloworldramen.tripletriad.tripletriad.scenes.game

import GameEngine
import com.helloworldramen.tripletriad.tripletriad.ai.GameStateMCTSNode
import com.helloworldramen.tripletriad.tripletriad.ai.MCTS
import com.helloworldramen.tripletriad.tripletriad.models.*
import com.helloworldramen.tripletriad.tripletriad.scenes.card.PlayerCardScene
import com.helloworldramen.tripletriad.tripletriad.scenes.slot.SlotScene
import godot.*
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.core.Vector2
import godot.extensions.getNodeAs
import godot.global.GD

@RegisterClass
class GameScene: Node2D() {

	//region Child Nodes

	private val testCardScene: PlayerCardScene by lazy { getNodeAs("TestCardScene")!! }
	private val boardSlotScenes: List<SlotScene> by lazy {
		listOf(
				getNodeAs("BoardSlotTopLeft")!!,
				getNodeAs("BoardSlotTop")!!,
				getNodeAs("BoardSlotTopRight")!!,
				getNodeAs("BoardSlotLeft")!!,
				getNodeAs("BoardSlotCenter")!!,
				getNodeAs("BoardSlotRight")!!,
				getNodeAs("BoardSlotBottomLeft")!!,
				getNodeAs("BoardSlotBottom")!!,
				getNodeAs("BoardSlotBottomRight")!!,
		)
	}

	//endregion

	//region Class Variables

	private val gameEngine: GameEngine = GameEngine()
	private val ai: MCTS = MCTS()

	private var testPlayerCard: PlayerCard = PlayerCard(Card.Bomb, 0, true, false)
	private var didRun = false
	private var isMousePrimaryPressed = false
	private var hoveredCardScene: PlayerCardScene? = null
	private var grabbedCardScene: PlayerCardScene? = null
	private var grabbedCardSceneOriginalPosition: Vector2 = Vector2()
	private var lastEnteredSlot: SlotScene? = null

	//endregion

	//region LifeCycle

	@RegisterFunction
	override fun _ready() {
		boardSlotScenes.forEach { boardSlotScene ->
			boardSlotScene.connect("card_entered", this, "on_slot_entered")
			boardSlotScene.connect("card_exited", this, "on_slot_exited")
		}

		testCardScene.connect("player_card_entered", this, "on_player_card_entered")
		testCardScene.connect("player_card_exited", this, "on_player_card_exited")
		testCardScene.bind(testPlayerCard)
	}

	@RegisterFunction
	override fun _input(event: InputEvent) {
		handleDragDrop(event)
		handleTestFlipping(event)
	}

	fun bind(gameState: GameState) {
	}

	//endregion

	//region Signals
	@RegisterFunction
	fun onSlotEntered(slotScene: SlotScene) {
		lastEnteredSlot = slotScene
		if (isMousePrimaryPressed) {
			slotScene.highlight()
		}
	}

	@RegisterFunction
	fun onSlotExited(slotScene: SlotScene) {
		lastEnteredSlot = null
		slotScene.unhighlight()
	}

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

	//endregion


	private fun handleDragDrop(event: InputEvent) {
		if (isMousePrimaryPressed && event.isActionReleased("mouse_primary")) {
			// Mouse released
			isMousePrimaryPressed = false

			lastEnteredSlot?.let {
				grabbedCardScene?.position = it.position
				lastEnteredSlot?.unhighlight()
			} ?: run {
				grabbedCardScene?.position = grabbedCardSceneOriginalPosition
			}

			grabbedCardScene?.unhighlight()
			grabbedCardScene = null

		} else if (!isMousePrimaryPressed && event.isActionPressed("mouse_primary")) {
			// Mouse pressed
			isMousePrimaryPressed = true
			grabbedCardScene = hoveredCardScene
			grabbedCardScene?.highlight()
			lastEnteredSlot?.highlight()
			grabbedCardSceneOriginalPosition = grabbedCardScene?.position ?: Vector2()
		}

		if (isMousePrimaryPressed && event is InputEventMouseMotion) {
			grabbedCardScene?.position = grabbedCardScene!!.position + event.relative
		}
	}

	private fun handleTestGameExecution(event: InputEvent) {
		if (!didRun && event.isActionPressed("ui_accept")) {
			didRun = true

			setupAiGame()
		} else if (didRun && event.isActionPressed("ui_accept")) {
			nextTurn()
		}
	}

	private fun handleTestFlipping(event: InputEvent) {
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
