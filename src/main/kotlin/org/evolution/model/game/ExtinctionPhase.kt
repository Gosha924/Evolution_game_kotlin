package org.evolution.model.game

class ExtinctionPhase : Phase() {
    override fun execute(game: Game) {
        println("--- Фаза вымирания: проверка выживаемости ---")

        for (player in game.players) {
            val animalsToRemove = player.animals.filter { !it.isAlive || !it.isFull() }

            for (animal in animalsToRemove) {
                player.removeAnimal(animal)
                println("Животное ${animal.id} игрока ${player.name} погибло от голода")
            }
            player.animals.forEach { it.resetRounding() }
        }

        if (game.deck.isEmpty() && game.players.all { it.hand.isEmpty() }) {
            println("Ресурсы исчерпаны. Игра близится к финалу.")
        }
    }
}