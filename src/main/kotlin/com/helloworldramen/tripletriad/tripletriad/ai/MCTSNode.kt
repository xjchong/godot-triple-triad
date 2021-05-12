package com.helloworldramen.tripletriad.tripletriad.ai

abstract class MCTSNode {
    abstract val parent: MCTSNode?
    abstract val children: List<MCTSNode>

    var accumulatedValue: Double = 0.0
        private set
    var visits: Int = 0
        private set

    val isFullyVisited: Boolean
        get() = children.all { it.visits > 0 }

    val isLeaf: Boolean
        get() = children.isEmpty()

    abstract val stateValue: Double

    abstract fun rollout(): Double

    fun backpropagate(value: Double) {
        ++visits
        accumulatedValue += value
        parent?.backpropagate(value)
    }
}