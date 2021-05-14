package com.helloworldramen.tripletriad.tripletriad.scenes.game

import GameEngine
import com.helloworldramen.tripletriad.tripletriad.ai.GameStateMCTSNode
import com.helloworldramen.tripletriad.tripletriad.ai.MCTS
import com.helloworldramen.tripletriad.tripletriad.game.GameStateStep
import com.helloworldramen.tripletriad.tripletriad.models.*
import com.helloworldramen.tripletriad.tripletriad.scenes.card.PlayerCardScene
import com.helloworldramen.tripletriad.tripletriad.scenes.slot.SlotScene
import godot.*
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.core.Vector2
import godot.extensions.getNodeAs
import godot.global.GD
import models.Position
import java.util.Timer
import kotlin.concurrent.timerTask

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
				getNodeAs("BoardSlotBottomRight")!!
		)
	}

	private val player1SlotScenes: List<SlotScene> by lazy {
		listOf(
				getNodeAs("Player1Slot1")!!,
				getNodeAs("Player1Slot2")!!,
				getNodeAs("Player1Slot3")!!,
				getNodeAs("Player1Slot4")!!,
				getNodeAs("Player1Slot5")!!
		)
	}

	private val player2SlotScenes: List<SlotScene> by lazy {
		listOf(
				getNodeAs("Player2Slot1")!!,
				getNodeAs("Player2Slot2")!!,
				getNodeAs("Player2Slot3")!!,
				getNodeAs("Player2Slot4")!!,
				getNodeAs("Player2Slot5")!!
		)
	}

	private val player1CardScenes: List<PlayerCardScene> by lazy {
		listOf(
				getNodeAs("Player1Card1")!!,
				getNodeAs("Player1Card2")!!,
				getNodeAs("Player1Card3")!!,
				getNodeAs("Player1Card4")!!,
				getNodeAs("Player1Card5")!!,
		)
	}

	private val player2CardScenes: List<PlayerCardScene> by lazy {
		listOf(
				getNodeAs("Player2Card1")!!,
				getNodeAs("Player2Card2")!!,
				getNodeAs("Player2Card3")!!,
				getNodeAs("Player2Card4")!!,
				getNodeAs("Player2Card5")!!,
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
		(boardSlotScenes + player1SlotScenes + player2SlotScenes).forEach {
			it.connect("slot_entered", this, "on_slot_entered")
			it.connect("slot_exited", this, "on_slot_exited")
		}

		(player1CardScenes + player2CardScenes).forEach {
			it.connect("player_card_entered", this, "on_player_card_entered")
			it.connect("player_card_exited", this, "on_player_card_exited")
		}

		testCardScene.connect("player_card_entered", this, "on_player_card_entered")
		testCardScene.connect("player_card_exited", this, "on_player_card_exited")
		testCardScene.bind(testPlayerCard)
	}

	@RegisterFunction
	override fun _input(event: InputEvent) {
		handleDragDrop(event)
		handleTestFlipping(event)
		handleTestGameExecution(event)
	}

	private fun bind(gameState: GameState) {
		val isNewGame = gameState.board.playerCards.values.filterNotNull().isEmpty()
		// Bind the player hands.
		gameState.players.forEach { player ->
			for ((cardIndex, playerCard) in player.cards.withIndex()) {
				if (isNewGame) {
					when (player.id) {
						0 -> {
							with(player1CardScenes[cardIndex]) {
								moveTo(player1SlotScenes[cardIndex].position, duringMove = {
									bind(playerCard)
								})
							}
						}
						1 -> {
							with(player2CardScenes[cardIndex]) {
								moveTo(player2SlotScenes[cardIndex].position, duringMove = {
									bind(playerCard)
								})
							}
						}
					}
				} else { // Not a new game.
					val playerCardScene = findPlayerCardScene(playerCard)
					playerCardScene?.bind(playerCard)
				}
			}
		}

		// Bind the board.
		for ((boardPosition, playerCard) in gameState.board.playerCards) {
			if (playerCard == null) continue

			val boardSlotScene = when(boardPosition) {
				Position.TOP_LEFT -> boardSlotScenes[0]
				Position.TOP -> boardSlotScenes[1]
				Position.TOP_RIGHT -> boardSlotScenes[2]
				Position.LEFT -> boardSlotScenes[3]
				Position.CENTER -> boardSlotScenes[4]
				Position.RIGHT -> boardSlotScenes[5]
				Position.BOTTOM_LEFT -> boardSlotScenes[6]
				Position.BOTTOM -> boardSlotScenes[7]
				Position.BOTTOM_RIGHT -> boardSlotScenes[8]
				else -> null
			} ?: continue

			findPlayerCardScene(playerCard)?.run {
				moveTo(boardSlotScene.position, duringMove = {
					bind(playerCard)
				})
			}
		}
	}

	private fun bindWithSteps(state: GameState, steps: List<GameStateStep>) {
		if (steps.isEmpty()) {
			bind(state)
			return
		}

		val nextStep = steps.first()
		val remainingSteps = steps.drop(1)

		when(nextStep) {
			is GameStateStep.Placed -> bindStepPlaced(state, nextStep)
			is GameStateStep.Same -> bindStepSame(state, nextStep)
			is GameStateStep.Plus -> bindStepPlus(state, nextStep)
			is GameStateStep.Combo -> bindStepCombo(state, nextStep)
			is GameStateStep.Ascension -> bind(state)
			is GameStateStep.Descension -> bind(state)
			is GameStateStep.SuddenDeath -> bind(state)
		}

		Timer().schedule(timerTask {
			bindWithSteps(state, remainingSteps)
		}, 1000)

	}

	private fun bindStepPlaced(state: GameState, step: GameStateStep.Placed) {
		val placedPosition = step.placedPosition
		val placedCard = state.board.playerCards[placedPosition] ?: return
		val placedCardScene = findPlayerCardScene(placedCard) ?: return

		getSlotPositionFromBoardPosition(step.placedPosition)?.let { slotPosition ->
			placedCardScene.moveTo(slotPosition, duringMove =  {
				placedCardScene.bind(placedCard)
			}) {
				for (flippedPosition in step.flippedPositions) {
					val flippedCard = state.board.playerCards[flippedPosition] ?: continue
					val flippedCardScene = findPlayerCardScene(flippedCard) ?: continue
					if (flippedCardScene.playerCard?.playerId == flippedCard.playerId) continue // No flip required.
					val isHorizontalFlip = placedPosition.row == flippedPosition.row

					flippedCardScene.flip(isHorizontalFlip) {
						flippedCardScene.flip(isHorizontalFlip, flippedCard)
					}
				}
			}
		}
	}

	private fun bindStepSame(state: GameState, step: GameStateStep.Same) {
		flipMultipleAndBind(state, step.placedPosition, step.flippedPositions)
	}

	private fun bindStepPlus(state: GameState, step: GameStateStep.Plus) {
		flipMultipleAndBind(state, step.placedPosition, step.flippedPositions)
	}

	private fun bindStepCombo(state: GameState, step: GameStateStep.Combo) {
		for ((placedPosition, flippedPositions) in step.flippedToComboed) {
			flipMultipleAndBind(state, placedPosition, flippedPositions)
		}
	}

	private fun flipMultipleAndBind(state: GameState, placedPosition: Position, flippedPositions: List<Position>) {
		for (flippedPosition in flippedPositions) {
			val flippedCard = state.board.playerCards[flippedPosition] ?: continue
			val flippedCardScene = findPlayerCardScene(flippedCard) ?: continue
			if (flippedCardScene.playerCard?.playerId == flippedCard.playerId) continue
			val isHorizontalFlip = placedPosition.row == flippedPosition.row

			flippedCardScene.flip(isHorizontalFlip) {
				flippedCardScene.flip(isHorizontalFlip, flippedCard)
			}
		}
	}

	private fun getSlotPositionFromBoardPosition(boardPosition: Position): Vector2? {
		val boardSlotScene = when(boardPosition) {
			Position.TOP_LEFT -> boardSlotScenes[0]
			Position.TOP -> boardSlotScenes[1]
			Position.TOP_RIGHT -> boardSlotScenes[2]
			Position.LEFT -> boardSlotScenes[3]
			Position.CENTER -> boardSlotScenes[4]
			Position.RIGHT -> boardSlotScenes[5]
			Position.BOTTOM_LEFT -> boardSlotScenes[6]
			Position.BOTTOM -> boardSlotScenes[7]
			Position.BOTTOM_RIGHT -> boardSlotScenes[8]
			else -> null
		}

		return boardSlotScene?.position
	}

	private fun findPlayerCardScene(playerCard: PlayerCard): PlayerCardScene? {
		val player1Card = player1CardScenes.find {
			it.playerCard?.id == playerCard.id
		}

		return player1Card ?: player2CardScenes.find {
			it.playerCard?.id == playerCard.id
		}
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
				if (lastEnteredSlot in boardSlotScenes) {
					grabbedCardScene?.position = it.position
				} else {
					grabbedCardScene?.position = grabbedCardSceneOriginalPosition
				}

				lastEnteredSlot?.unhighlight()
				lastEnteredSlot = null
			} ?: run {
				grabbedCardScene?.position = grabbedCardSceneOriginalPosition
			}

			grabbedCardScene?.unhighlight()
			grabbedCardScene?.zIndex = grabbedCardScene!!.zIndex - 100
			grabbedCardScene = null

		} else if (!isMousePrimaryPressed && event.isActionPressed("mouse_primary")) {
			// Mouse pressed
			isMousePrimaryPressed = true
			grabbedCardScene = hoveredCardScene
			grabbedCardScene?.zIndex = grabbedCardScene!!.zIndex + 100
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
		} else if (event.isActionPressed("ui_accept")) {
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

		bind(gameEngine.nextState().first)
	}

	private fun nextTurn() {
		val nextState = gameEngine.nextState().first

		if (nextState.isGameOver()) return

		gameEngine.playMove(getAiMove(ai, nextState))
		with(gameEngine.nextState()) {
			bindWithSteps(first, second)
		}
	}

	private fun testSetup() {
		val gameEngine = GameEngine()

		gameEngine.startGame(listOf(
				Player(1, arrayOf(Card.Ananta, Card.AlexanderPrime, Card.Dodo, Card.Mandragora, Card.Amaljaa)),
				Player(0, arrayOf(Card.Adamantoise, Card.Ananta, Card.Bomb, Card.Coeurl, Card.Sabotender))),
				advancedRules = listOf(AllOpen, Descension, SuddenDeath),
				shouldShufflePlayers = false
		)

		bind(gameEngine.nextState().first)
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
		var (nextState, steps) = gameEngine.nextState()

		while (!nextState.isGameOver()) {
			val move = if (nextState.nextPlayer().id == 0) {
				bind(nextState)
				getAiMove(ai, nextState)
//            getPlayerMove(nextState)
			} else {
				getAiMove(ai, nextState)
			}

			gameEngine.playMove(move)
			with(gameEngine.nextState()) {
				nextState = first
				steps = second
			}

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
