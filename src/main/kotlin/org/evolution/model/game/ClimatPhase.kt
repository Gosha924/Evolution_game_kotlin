package org.evolution.model.game
import kotlin.random.Random

class ClimatePhase : Phase() {
    override fun execute(game: Game) {
        val foodBase = when (game.players.size) {
            2 -> Random.nextInt(1, 7) + 2
            3 -> Random.nextInt(1, 7) + Random.nextInt(1, 7)
            4 -> Random.nextInt(1, 7) + Random.nextInt(1, 7) + 2
            else -> 0
        }
        game.foodPool = foodBase
    }
}