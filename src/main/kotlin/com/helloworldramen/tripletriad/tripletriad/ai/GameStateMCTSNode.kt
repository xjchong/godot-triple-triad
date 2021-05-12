package com.helloworldramen.tripletriad.tripletriad.ai

import GameStateMachine
import com.helloworldramen.tripletriad.tripletriad.models.Chaos
import com.helloworldramen.tripletriad.tripletriad.models.GameState
import com.helloworldramen.tripletriad.tripletriad.models.Order
import com.helloworldramen.tripletriad.tripletriad.models.*
import models.*
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlin.random.nextInt

class GameStateMCTSNode(private val rootState: GameState, val moves: List<Move> = listOf(),
                        override val parent: MCTSNode? = null): MCTSNode() {

    override val children: List<MCTSNode> by lazy {
        // The number of cards left can be calculated by the number of moves played in conjunction with the root state.
        val rootCardsCount = rootState.players.flatMap { it.cards }.size
        val cardsCount = ((rootCardsCount - moves.size) / 2.0).roundToInt()
        val possibleChildren = mutableListOf<GameStateMCTSNode>()
        val rootOccupiedPositions = rootState.board.playerCards.filter { it.value != null }.keys
        val moveOccupiedPositions = moves.map { it.position }
        val remainingPositions = sequenceOf(
            Position.TOP_LEFT,
            Position.TOP,
            Position.TOP_RIGHT,
            Position.LEFT,
            Position.CENTER,
            Position.RIGHT,
            Position.BOTTOM_LEFT,
            Position.BOTTOM,
            Position.BOTTOM_RIGHT
        ).filter { position ->
            position !in moveOccupiedPositions && position !in rootOccupiedPositions
        }

        val cardIndices = when {
            rootState.advancedRules.contains(Order) -> listOf(0)
            rootState.advancedRules.contains(Chaos) -> {
                if (moves.isEmpty()) {
                    listOf(rootState.nextPlayer().cards.indexOfFirst {
                        it.isPlayable
                    })
                } else {
                    listOf(Random.nextInt(0 until cardsCount))
                }
            }
            else -> (0 until cardsCount).toList()
        }

        for (cardIndex in cardIndices) {
            for (position in remainingPositions) {
                val nextMoves = moves + listOf(Move(cardIndex, position))

                possibleChildren.add(GameStateMCTSNode(rootState, nextMoves, this))
            }
        }

        possibleChildren
    }

    override val stateValue: Double
        get() {
            val gameStateMachine = GameStateMachine().also {
                it.setState(getPossibleInitialState())
            }
            moves.forEach {
                gameStateMachine.makeMove(it.playerCardIndex, it.position)
            }
            val endState = gameStateMachine.states.last()
            val playerId = rootState.nextPlayer().id
            val endStateScore = endState.score()
            return when (playerId) {
                1 -> if (endStateScore > 0) 1.0 else 0.0
                else -> if (endStateScore < 0) 1.0 else 0.0
            }
        }

    override fun rollout(): Double {
        var currentNode: MCTSNode = this

        while (!currentNode.isLeaf) {
            currentNode = currentNode.children.random()
        }

        return currentNode.stateValue
    }

    private fun getPossibleInitialState(): GameState {
        return rootState.copy(players = rootState.players.map { player ->
            player.withCards(player.cards.map { playerCard ->
                if (playerCard.card == Card.UNKNOWN) {
                    playerCard.copy(card = Card.Alpha)
                } else playerCard
            })
        }, advancedRules = rootState.advancedRules.filterNot { it == Chaos })
    }
}