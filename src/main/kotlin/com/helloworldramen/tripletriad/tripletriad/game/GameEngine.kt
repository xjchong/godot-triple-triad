import com.helloworldramen.tripletriad.tripletriad.models.*
import kotlin.random.Random

class GameEngine {

    private val stateMachine: GameStateMachine = GameStateMachine()

    fun startGame(players: List<Player>, advancedRules: List<AdvancedRule> = listOf(),
                   shouldShufflePlayers: Boolean = true): GameState {
        val startingPlayers = if (shouldShufflePlayers) players.shuffled() else players
        var initialGameState = GameState(Board.standardInstance(), startingPlayers, advancedRules)

        // Setup the advanced rules.
        if (advancedRules.contains(AllOpen)) {
            initialGameState = setupAllOpen(initialGameState)
        } else if (advancedRules.contains(ThreeOpen)) {
            initialGameState = setupThreeOpen(initialGameState)
        }

        if (advancedRules.contains(Swap)) {
            initialGameState = setupSwap(initialGameState)
        }

        if (advancedRules.contains(Chaos)) {
            initialGameState = setupChaos(initialGameState)
        } else if (advancedRules.contains(Order)) {
            initialGameState = setupOrder(initialGameState)
        } else {
            initialGameState = setupFreePlay(initialGameState)
        }

        stateMachine.setState(initialGameState)

        return initialGameState
    }

    fun nextState(): GameState {
        return redactState(stateMachine.states.last())
    }

    fun playMove(move: Move): GameState {
        if (stateMachine.states.last().isGameOver()) {
            println("Game is over!")
        } else {
            stateMachine.makeMove(move.playerCardIndex, move.position)
        }

        return nextState()
    }

    private fun redactState(gameState: GameState): GameState {
        val turnPlayerId = gameState.nextPlayer().id

        return gameState.copy(players = gameState.players.map { player ->
            if (player.id == turnPlayerId) {
                player
            } else {
                player.withCards(player.cards.map { playerCard ->
                    if (playerCard.isHidden) playerCard.copy(card = Card.UNKNOWN) else playerCard
                })
            }
        })
    }

    private fun setupAllOpen(gameState: GameState): GameState {
        return gameState.copy(players = gameState.players.map { player ->
            player.withCards(player.cards.map {
                it.unhidden()
            })
        })
    }

    private fun setupThreeOpen(gameState: GameState): GameState {
        return gameState.copy(players = gameState.players.map { player ->
            val indicesToOpen = listOf(0, 1, 2, 3, 4).shuffled().take(3)

            player.withCards(player.cards.mapIndexed { index, playerCard ->
                if (index in indicesToOpen) playerCard.unhidden() else playerCard.hidden()
            })
        })
    }

    private fun setupOrder(gameState: GameState): GameState {
        return gameState.copy(players = gameState.players.mapIndexed { playerIndex, player ->
            if (playerIndex == 0) {
                player.withCards(player.cards.mapIndexed { cardIndex, card ->
                    if (cardIndex == 0) card.playable() else card.unplayable()
                })
            } else {
                player.withCards(player.cards.map { it.unplayable() })
            }
        })
    }

    private fun setupChaos(gameState: GameState): GameState {
        return gameState.copy(players = gameState.players.mapIndexed { playerIndex, player ->
            if (playerIndex == 0) {
                val randomCardIndex = Random.nextInt(0, player.cards.size)
                player.withCards(player.cards.mapIndexed { cardIndex, card ->
                    if (cardIndex == randomCardIndex) card.playable() else card.unplayable()
                })
            } else {
                player.withCards(player.cards.map { it.unplayable() })
            }
        })
    }

    private fun setupFreePlay(gameState: GameState): GameState {
        return gameState.copy(players = gameState.players.mapIndexed { playerIndex, player ->
            if (playerIndex == 0) {
                player.withCards(player.cards.map { it.playable() })
            } else {
                player.withCards(player.cards.map { it.unplayable() })
            }
        })
    }

    private fun setupSwap(gameState: GameState): GameState {
        return gameState
    }
}