package org.evolution.utils

import org.evolution.model.player.Player

inline fun loopUntilValid(action: () -> Boolean) {
    var turnCompleted = false
    while (!turnCompleted) {
        turnCompleted = action()
    }
}
fun printPlayerStatus(player: Player, showFood: Boolean = false) {
    if (player.animals.isEmpty()) {
        println("\n--- У игрока ${player.name} пока нет животных ---")
        return
    }
    println("\n=== АКТИВЫ ИГРОКА ${player.name} ===")
    player.animals.forEach { animal ->
        val traits = if (animal.traits.isEmpty()) "нет свойств"
        else animal.traits.joinToString(", ") { it.traitType.toString() }

        val foodStatus = if (showFood) " | Еда: ${animal.foodEaten}/${animal.totalFoodRequired()}" else ""

        println("Животное ID: ${animal.id} | Свойства: [$traits]$foodStatus")
    }
    println("======================================\n")
}