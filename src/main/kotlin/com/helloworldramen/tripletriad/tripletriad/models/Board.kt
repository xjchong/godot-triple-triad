package com.helloworldramen.tripletriad.tripletriad.models

import models.Position

class Board private constructor(val rows: Int, val columns: Int, val playerCards: Map<Position, PlayerCard?> = mapOf()) {

    fun setCard(playerCard: PlayerCard, requestedPosition: Position): Board? {
        if (!playerCards.containsKey(requestedPosition)) return null // Position doesn't exist.

        return Board(rows, columns, playerCards.mapValues { (position, currentPlayerCard) ->
            if (position == requestedPosition) playerCard else currentPlayerCard
        })
    }

    fun flipped(position: Position, playerId: Int): Board {
        val playerCard = playerCards[position] ?: return this

        return setCard(playerCard.assignedToPlayer(playerId), position) ?: this
    }

    /**
     * @return pair of board, and position that got flipped if successfully flipped.
     */
    fun flippedIf(position: Position, otherPosition: Position,
                  shouldFlip: (card: PlayerCard, otherCard: PlayerCard) -> Boolean): Pair<Board, Position?> {
        val playerCard = playerCards[position]
        val otherPlayerCard = playerCards[otherPosition]

        return if (playerCard != null && otherPlayerCard != null && playerCard.playerId != otherPlayerCard.playerId &&
            shouldFlip(playerCard, otherPlayerCard)) {
            val setBoard = setCard(otherPlayerCard.assignedToPlayer(playerCard.playerId), otherPosition)

            if (setBoard != null) {
                Pair(setBoard, otherPosition)
            } else Pair(this, null)
        } else {
            Pair(this, null)
        }
    }

    companion object {
        fun standardInstance(): Board {
            return Board(3, 3, mutableMapOf(
                Position(0, 0) to null,
                Position(0, 1) to null,
                Position(0, 2) to null,
                Position(1, 0) to null,
                Position(1, 1) to null,
                Position(1, 2) to null,
                Position(2, 0) to null,
                Position(2, 1) to null,
                Position(2, 2) to null
            ))
        }
    }
}