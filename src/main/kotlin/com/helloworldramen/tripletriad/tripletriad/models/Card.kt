package com.helloworldramen.tripletriad.tripletriad.models

class Card private constructor(val name: String, val rarity: CardRarity,
                               val n: Int, val e: Int, val s: Int, val w: Int,
                               val type: CardType? = null) {

    companion object {
        val DUD = Card("DUD", CardRarity.Legendary, 1, 1, 1, 1)
        val UNKNOWN = Card("     ", CardRarity.Unknown, 0, 0, 0, 0, CardType.Unknown)

        val Adamantoise = Card("Adamantoise", CardRarity.Uncommon, 5, 7, 4, 4)
        val Adrammelech = Card("Adrammelech", CardRarity.Rare, 6, 1, 7, 8)
        val Ahriman = Card("Ahriman", CardRarity.Common, 5, 5, 2, 2)
        val AlexanderPrime = Card("Alexander Prime", CardRarity.Rare, 7, 3, 2, 8, CardType.Primal)
        val Alpha = Card("Alpha", CardRarity.Rare, 6, 6, 6, 6)
        val AlphinaudAlisaie = Card("Alphinaud & Alisaie", CardRarity.Epic, 9, 3, 3, 9, CardType.Scions)
        val Amaljaa = Card("Amalj'aa", CardRarity.Common, 1, 4, 7, 1, CardType.Beastman)
        val Amaro = Card("Amaro", CardRarity.Common, 1, 7, 2, 3)
        val Ananta = Card("Ananta", CardRarity.Uncommon, 3, 7, 5, 2, CardType.Beastman)
        val Apkallu = Card("Apkallu", CardRarity.Common, 3, 4, 4, 1)
        val Archaeornis = Card("Archaeornis", CardRarity.Uncommon, 5, 4, 6, 5)
        val Bomb = Card("Bomb", CardRarity.Common, 3, 4, 3, 3)
        val Coeurl = Card("Coeurl", CardRarity.Common, 2, 5, 2, 5)
        val Dodo = Card("Dodo", CardRarity.Common, 4, 2, 3, 4)
        val Mandragora = Card("Mandragora", CardRarity.Common, 4, 2, 5, 3)
        val Sabotender = Card("Sabotender", CardRarity.Common, 4, 3, 3, 3)
    }
}