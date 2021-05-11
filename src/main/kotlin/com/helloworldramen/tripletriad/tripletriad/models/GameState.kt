package com.helloworldramen.tripletriad.tripletriad.models

data class GameState(
        val board: Board,
        val players: List<Player>, // The order of player implicitly indicates the starting player and turn order.
        val advancedRules: List<AdvancedRule> = listOf(),
        val movesPlayed: Int = 0) {

    fun isGameOver(): Boolean {
        return players.any { it.cards.isEmpty() }
    }

    fun score(): Int {
        var score = 0

        players.flatMap { it.cards }.forEach {
            when (it.playerId) {
                0 -> --score
                1 -> ++score
            }
        }

        board.playerCards.values.forEach {
            when(it?.playerId) {
                0 -> --score
                1 -> ++score
            }
        }

        return score
    }

    fun getTurn(): Int {
        // The first turn is 0.
        return board.playerCards.values.sumBy { if (it != null) 1 else 0 }
    }

    fun nextPlayer(): Player {
        return players[getTurn() % players.size]
    }

    fun movePlayed(): GameState {
        return copy(movesPlayed = movesPlayed + 1)
    }
}