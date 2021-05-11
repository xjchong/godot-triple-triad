package com.helloworldramen.tripletriad.tripletriad.models

import java.util.*

data class PlayerCard(val card: Card, val playerId: Int,
                      val isPlayable: Boolean = false,
                      val isHidden: Boolean = true, val modifier: Int = 0, val id: UUID = UUID.randomUUID()) {

    fun assignedToPlayer(playerId: Int): PlayerCard {
        return copy(playerId = playerId)
    }

    fun hidden(): PlayerCard {
        return copy(isHidden = true)
    }

    fun unhidden(): PlayerCard {
        return copy(isHidden = false)
    }

    fun playable(): PlayerCard {
        return copy(isPlayable = true)
    }

    fun unplayable(): PlayerCard {
        return copy(isPlayable = false)
    }

    fun modified(increment: Int): PlayerCard {
        return copy(modifier = modifier + increment)
    }

    fun noModifiers(): PlayerCard {
        return copy(modifier = 0)
    }

    fun n(): Int {
        return (card.n + modifier).coerceIn(MIN_VALUE, MAX_VALUE)
    }

    fun e(): Int {
        return (card.e + modifier).coerceIn(MIN_VALUE, MAX_VALUE)
    }

    fun s(): Int {
        return (card.s + modifier).coerceIn(MIN_VALUE, MAX_VALUE)
    }

    fun w(): Int {
        return (card.w + modifier).coerceIn(MIN_VALUE, MAX_VALUE)
    }

    companion object {
        const val MIN_VALUE = 1
        const val MAX_VALUE = 10
    }

}