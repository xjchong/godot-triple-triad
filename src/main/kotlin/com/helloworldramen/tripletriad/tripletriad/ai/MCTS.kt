package com.helloworldramen.tripletriad.tripletriad.ai

import kotlin.math.ln
import kotlin.math.sqrt

class MCTS(private val explorationConstant: Double = sqrt(2.0)) {

    fun getBestNode(rootNode: MCTSNode, maxIterations: Int?, maxTimeMs: Int?): MCTSNode {
        require(maxIterations != null || maxTimeMs != null)

        val startTime = System.currentTimeMillis()
        var iterationCount = 0

        while ((maxIterations == null || iterationCount++ < maxIterations) &&
            (maxTimeMs == null || System.currentTimeMillis() - startTime < maxTimeMs)) {
            traverse(rootNode).run {
                backpropagate(rollout())
            }
        }

        return rootNode.children.maxByOrNull { it.visits } ?: rootNode
    }

    private fun traverse(node: MCTSNode): MCTSNode {
        var nextNode = node

        while (nextNode.isFullyVisited) {
            nextNode = nextNode.children.maxByOrNull {
                (it.accumulatedValue / it.visits) + (explorationConstant * sqrt(ln(nextNode.visits / it.visits.toDouble())))
            } ?: break
        }

        return nextNode.children.shuffled().firstOrNull {
            it.visits == 0
        } ?: nextNode
    }
}