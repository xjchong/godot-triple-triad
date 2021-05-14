import com.helloworldramen.tripletriad.tripletriad.game.GameStateStep
import com.helloworldramen.tripletriad.tripletriad.models.*
import models.*
import java.util.*
import kotlin.random.Random

class GameStateMachine {

    var states: List<GameState> = listOf()
        private set
    var stepsList: List<List<GameStateStep>> = listOf()
        private set

    fun setState(gameState: GameState) {
        states = listOf(gameState)
        stepsList = listOf(listOf())
    }

    @Throws(IllegalStateException::class)
    fun makeMove(playerCardIndex: Int, position: Position): GameState {
        val currentState = states.lastOrNull() ?:
            throw IllegalStateException("Need to initialize engine.")

        val playerCard = currentState.nextPlayer().cards.getOrNull(playerCardIndex) ?:
            throw IllegalStateException("Couldn't get a card for player ${currentState.nextPlayer().id} with card index $playerCardIndex.")

        return makeMove(playerCard, position)
    }

    @Throws(IllegalStateException::class)
    fun makeMove(playerCard: PlayerCard, position: Position): GameState {
        val currentState = states.lastOrNull() ?:
            throw IllegalStateException("Need to initialize engine.")

        val player = currentState.players.find { player ->
            player.cards.any { it.id == playerCard.id }
        } ?: throw IllegalStateException("No player owns the card being played.")

        requireCardPlayable(currentState, player, playerCard, position)

        // Prepare a new steps list.
        stepsList = stepsList + listOf(listOf(GameStateStep.Move(position)))

        // Place the card on the board.
        val nextBoard = placeCard(playerCard, position, currentState.board, currentState.advancedRules)

        // Remove the card from the player.
        val playerAfterMove = player.lessCard(playerCard)
        val playersAfterMove = currentState.players.map {
            if (it.id == playerAfterMove.id) playerAfterMove else it
        }

        var nextState = currentState.copy(board = nextBoard, players = playersAfterMove).movePlayed()

        if (nextState.advancedRules.contains(Ascension)) {
            nextState = resolveAscension(nextState, position)
        }

        if (nextState.advancedRules.contains(Descension)) {
            nextState = resolveDescension(nextState, position)
        }

        if (nextState.advancedRules.contains(SuddenDeath)) {
            nextState = resolveSuddenDeath(nextState)
        }

        nextState = resolvePlayability(nextState)
        states = states + nextState

        return nextState
    }

    @Throws(IllegalStateException::class)
    private fun requireCardPlayable(currentState: GameState, player: Player, playerCard: PlayerCard,
                                    position: Position) {
        if (currentState.nextPlayer() != player)
            throw IllegalStateException("Card not playable due to not being this player's turn.")

        if (!player.cards.contains(playerCard))
            throw IllegalStateException("Card not playable due to the player not owning that card.")

        if (!playerCard.isPlayable)
            throw IllegalStateException("Card not playable on this turn.")

        if (!currentState.board.playerCards.containsKey(position))
            throw IllegalStateException("Card can't be played to a position that doesn't exist.")

        if (currentState.board.playerCards[position] != null)
            throw IllegalStateException("Card( $playerCard) can't be played to a position ($position) that is occupied.")
    }

    @Throws(IllegalStateException::class)
    private fun placeCard(playerCard: PlayerCard, position: Position, board: Board,
                          advancedRules: List<AdvancedRule>): Board {
        var nextBoard = board.setCard(playerCard.unhidden(), position) ?:
            throw IllegalStateException("Board didn't allow placement.")

        for (advancedRule in advancedRules) {
            nextBoard = when(advancedRule) {
                Plus -> resolvePlacedCardPlus(nextBoard, position, advancedRules)
                Same -> resolvePlacedCardSame(nextBoard, position, advancedRules)
                else -> nextBoard
            }
        }

        val boardAndFlips = when {
            advancedRules.contains(FallenAce) && advancedRules.contains(Reverse) -> {
                resolvePlacedCardFallenAceReverse(nextBoard, position)
            }
            advancedRules.contains(Reverse) -> resolvePlacedCardReverse(nextBoard, position)
            advancedRules.contains(FallenAce) -> resolvePlacedCardFallenAce(nextBoard, position)
            else -> resolvePlacedCardBasic(nextBoard, position)
        }

        addStep(GameStateStep.Placed(position, boardAndFlips.second))
        return boardAndFlips.first
    }

    private fun resolvePlayability(gameStateAfterPlace: GameState): GameState {
        val nextPlayer = gameStateAfterPlace.nextPlayer()
        val advancedRules = gameStateAfterPlace.advancedRules
        val playableIndices = when {
            advancedRules.contains(Chaos) -> listOf(Random.nextInt(0, nextPlayer.cards.size))
            advancedRules.contains(Order) -> listOf(0)
            else -> nextPlayer.cards.indices
        }

        return gameStateAfterPlace.copy(players = gameStateAfterPlace.players.map { player ->
            if (player == nextPlayer) {
                player.withCards(player.cards.mapIndexed { cardIndex, card ->
                    if (cardIndex in playableIndices) card.playable() else card.unplayable()
                })
            } else {
                player.withCards(player.cards.map { it.unplayable() })
            }
        })

    }

    private fun resolvePlacedCardBasic(board: Board, position: Position): Pair<Board, List<Position>> {
        return resolvePlacedCardCompareWith(board, position) { placedValue, otherValue ->
            placedValue > otherValue
        }
    }

    private fun resolvePlacedCardReverse(board: Board, position: Position): Pair<Board, List<Position>> {
        return resolvePlacedCardCompareWith(board, position) { placedValue, otherValue ->
            placedValue < otherValue
        }
    }

    private fun resolvePlacedCardSame(board: Board, position: Position, advancedRules: List<AdvancedRule>): Board {
        val positionsOfSameCards = mutableListOf<Position>()
        val placedCard = board.playerCards[position] ?: return board

        board.playerCards[position.north()]?.run {
            if (placedCard.n() == s()) {
                positionsOfSameCards.add(position.north())
            }
        }
        board.playerCards[position.east()]?.run {
            if (placedCard.e() == w()) {
                positionsOfSameCards.add(position.east())
            }
        }
        board.playerCards[position.south()]?.run {
            if (placedCard.s() == n()) {
                positionsOfSameCards.add(position.south())
            }
        }
        board.playerCards[position.west()]?.run {
            if (placedCard.w() == e()) {
                positionsOfSameCards.add(position.west())
            }
        }

        return if (positionsOfSameCards.size >= 2) {
            var nextBoard = board
            val comboPositions = mutableListOf<Position>()

            for (positionOfSame in positionsOfSameCards) {
                if (board.playerCards[positionOfSame]?.playerId != placedCard.playerId) {
                   comboPositions.add(positionOfSame)
                }

                nextBoard = nextBoard.flipped(positionOfSame, placedCard.playerId)
            }

            addStep(GameStateStep.Same(position, comboPositions))
            resolveCombo(comboPositions, nextBoard, advancedRules)
        } else board
    }

    private fun resolvePlacedCardPlus(board: Board, position: Position, advancedRules: List<AdvancedRule>): Board {
        val valuesToPositions = mutableMapOf<Int, MutableList<Position>>()
        val plusSet = mutableSetOf<Int>()
        val placedCard = board.playerCards[position] ?: return board
        val updateSet: (Int, Position) -> Unit = { value, otherPosition ->
            valuesToPositions.getOrDefault(value, mutableListOf()).run {
                if (isNotEmpty()) {
                    plusSet.add(value)
                }

                add(otherPosition)
                valuesToPositions[value] = this
            }
        }

        board.playerCards[position.north()]?.run {
            updateSet(placedCard.n() + s(), position.north())
        }
        board.playerCards[position.east()]?.run {
            updateSet(placedCard.e() + w(), position.east())
        }
        board.playerCards[position.south()]?.run {
            updateSet(placedCard.s() + n(), position.south())
        }
        board.playerCards[position.west()]?.run {
            updateSet(placedCard.w() + e(), position.west())
        }

        return if (plusSet.isNotEmpty()) {
            var nextBoard = board
            val comboPositions = mutableListOf<Position>()

            plusSet.forEach {
                valuesToPositions.getOrDefault(it, mutableListOf()).forEach { otherPosition ->
                    if (nextBoard.playerCards[otherPosition]?.playerId != placedCard.playerId) {
                        comboPositions.add(otherPosition)
                    }

                    nextBoard = nextBoard.flipped(otherPosition, placedCard.playerId)
                }
            }

            addStep(GameStateStep.Plus(position, comboPositions))
            resolveCombo(comboPositions, nextBoard, advancedRules)
        } else board
    }

    private fun resolvePlacedCardFallenAce(board: Board, position: Position): Pair<Board, List<Position>> {
        return resolvePlacedCardCompareWith(board, position) { placedValue, otherValue ->
            placedValue > otherValue || (placedValue == 1 && otherValue == 10)
        }
    }

    private fun resolvePlacedCardFallenAceReverse(board: Board, position: Position): Pair<Board, List<Position>> {
        return resolvePlacedCardCompareWith(board, position) { placedValue, otherValue ->
            placedValue < otherValue || (placedValue == 10 && otherValue == 1)
        }
    }

    private fun resolvePlacedCardCompareWith(board: Board, position: Position,
                                             compare: (placedValue: Int, otherValue: Int) -> Boolean)
    : Pair<Board, List<Position>> {
        val flippedPositions = mutableListOf<Position?>()

        var flipPair = board.flippedIf(position, position.north()) { placedCard, otherCard ->
            compare(placedCard.n(), otherCard.s())
        }
        flippedPositions.add(flipPair.second)

        flipPair = flipPair.first.flippedIf(position, position.east()) { placedCard, otherCard ->
            compare(placedCard.e(), otherCard.w())
        }
        flippedPositions.add(flipPair.second)

        flipPair = flipPair.first.flippedIf(position, position.south()) { placedCard, otherCard ->
            compare(placedCard.s(), otherCard.n())
        }
        flippedPositions.add(flipPair.second)

        flipPair = flipPair.first.flippedIf(position, position.west()) { placedCard, otherCard ->
            compare(placedCard.w(), otherCard.e())
        }
        flippedPositions.add(flipPair.second)

        return Pair(flipPair.first, flippedPositions.filterNotNull())
    }

    private fun resolveAscension(gameState: GameState, position: Position): GameState {
        with(resolveModifier(gameState, position, 1)) {
            addStep(GameStateStep.Ascension(second))
            return this.first
        }
    }

    private fun resolveDescension(gameState: GameState, position: Position): GameState {
        with(resolveModifier(gameState, position, -1)) {
            addStep(GameStateStep.Descension(second))
            return this.first
        }
    }

    private fun resolveModifier(gameState: GameState, position: Position, modifierIncrement: Int)
            : Pair<GameState, List<UUID>> {
        val typeToModify = gameState.board.playerCards[position]?.card?.type ?: return Pair(gameState, listOf())
        val boardCardsWithType = gameState.board.playerCards.filter { (_, boardCard) ->
            boardCard != null && boardCard.card.type == typeToModify
        }
        val modifiedPlayerCardIDs: MutableList<UUID> = mutableListOf()

        // Increment cards on the board.
        var nextBoard = gameState.board
        for ((boardPosition, boardCard) in boardCardsWithType) {
            if (boardCard == null) continue

            nextBoard.setCard(boardCard.modified(modifierIncrement), boardPosition)?.let {
                nextBoard = it
                modifiedPlayerCardIDs.add(boardCard.id)
            }
        }

        // Increment cards in player hands.
        val nextPlayers = gameState.players.map { player ->
            player.withCards(player.cards.map {
                if (it.card.type == typeToModify) {
                    modifiedPlayerCardIDs.add(it.id)
                    it.modified(modifierIncrement)
                } else it
            })
        }

        return Pair(gameState.copy(board = nextBoard, players = nextPlayers), modifiedPlayerCardIDs)
    }

    private fun resolveCombo(positions: List<Position>, board: Board, advancedRules: List<AdvancedRule>): Board {
        if (positions.isEmpty()) return board

        var nextBoard = board
        val flippedToComboed: MutableMap<Position, List<Position>> = mutableMapOf()
        val newlyFlippedPositions: MutableList<Position> = mutableListOf()

        for (position in positions) {
            val boardAndFlips = when {
                advancedRules.contains(FallenAce) && advancedRules.contains(Reverse) -> {
                    resolvePlacedCardFallenAceReverse(nextBoard, position)
                }
                advancedRules.contains(Reverse) -> resolvePlacedCardReverse(nextBoard, position)
                advancedRules.contains(FallenAce) -> resolvePlacedCardFallenAce(nextBoard, position)
                else -> resolvePlacedCardBasic(nextBoard, position)
            }

            nextBoard = boardAndFlips.first
            flippedToComboed[position] = boardAndFlips.second
            newlyFlippedPositions.addAll(boardAndFlips.second)
        }

        addStep(GameStateStep.Combo(flippedToComboed))
        return resolveCombo(newlyFlippedPositions, nextBoard, advancedRules)
    }

    private fun resolveSuddenDeath(gameState: GameState): GameState {
        // After 5 draws, sudden death ends (with a draw). Each game is 9 turns long.
        if (!gameState.isGameOver() || gameState.score() != 0 || gameState.movesPlayed >= (9 * 5)) return gameState

        val allCards = gameState.players.flatMap { it.cards } + gameState.board.playerCards.mapNotNull { it.value }

        val nextState = gameState.copy(board = Board.standardInstance(), players = gameState.players.map { player ->
            player.withCards(allCards.map{ it.noModifiers() }.filter { it.playerId == player.id }.take(5))
        }.reversed())

        addStep(GameStateStep.SuddenDeath(nextState))
        return nextState
    }

    private fun addStep(step: GameStateStep) {
        stepsList = stepsList.dropLast(1) + listOf(stepsList.last() + listOf(step))
    }

    inner class IllegalStateException(message: String) : Exception(message)
}