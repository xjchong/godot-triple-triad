package com.helloworldramen.tripletriad.tripletriad.models

sealed class CardType(val name: String) {
    object Beastman : CardType("Beastman")
    object Garland : CardType("Garland")
    object Primal : CardType("Primal")
    object Scions : CardType("Scions")
    object Unknown : CardType("Unknown")
}