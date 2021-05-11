package com.helloworldramen.tripletriad.tripletriad.models

data class Player(val id: Int, val cards: List<PlayerCard> = listOf()) {

    constructor(id: Int, cards: Array<Card>): this (
        id, cards.map { PlayerCard(it, id) }
    )

    fun plusCard(playerCard: PlayerCard): Player {
        return copy(cards = cards + playerCard.assignedToPlayer(id))
    }

    fun lessCard(playerCard: PlayerCard): Player {
        return copy(cards = cards.filter { it != playerCard })
    }

    fun withCards(cards: List<PlayerCard>): Player {
        return copy(cards = cards)
    }
}