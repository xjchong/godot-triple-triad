package com.helloworldramen.tripletriad.tripletriad.models

sealed class CardRarity(val value: Int) {
    object Unknown : CardRarity(0)
    object Common : CardRarity(1)
    object Uncommon : CardRarity(2)
    object Rare : CardRarity(3)
    object Epic : CardRarity(4)
    object Legendary : CardRarity(5)
}