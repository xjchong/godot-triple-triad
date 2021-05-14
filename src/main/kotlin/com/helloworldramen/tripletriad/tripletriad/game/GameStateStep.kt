package com.helloworldramen.tripletriad.tripletriad.game

import com.helloworldramen.tripletriad.tripletriad.models.GameState
import models.Position
import java.util.*

sealed class GameStateStep {
    class Move(val placedPosition: Position) : GameStateStep()
    class Same(val placedPosition: Position, val flippedPositions: List<Position>) : GameStateStep()
    class Plus(val placedPosition: Position, val flippedPositions: List<Position>) : GameStateStep()
    class Combo(val flippedToComboed: Map<Position, List<Position>>) : GameStateStep()
    class Placed(val placedPosition: Position, val flippedPositions: List<Position>) : GameStateStep()
    class Ascension(val ascendedCardIDs: List<UUID>) : GameStateStep()
    class Descension(val descendedCardIDs: List<UUID>) : GameStateStep()
    class SuddenDeath(val newState: GameState) : GameStateStep()
}