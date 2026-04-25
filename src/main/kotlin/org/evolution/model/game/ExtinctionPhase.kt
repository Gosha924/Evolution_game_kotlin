package org.evolution.model.game

class ExtinctionPhase : Phase() {
    override fun execute(game: Game) {
        for (player in game.players) {
            val animalsToRemove = player.animals.filter { !it.isAlive || it.needFood() > 0 }
            for (animal in animalsToRemove) {
                player.removeAnimal(animal)
                println("Животное ${animal.id} игрока ${player.name} погибло")
            }
        }

        val remainingCards = game.deck.size + game.players.sumOf { it.hand.size }
        if (remainingCards == 0) {
            println("Колода пуста. Игра завершается")
            game.calculateFinalScore()
            return
        }

        for (player in game.players) {
            val cardsToDraw = player.animals.size
            repeat(cardsToDraw) {
                if (game.deck.isNotEmpty()) {
                    val newCard = game.deck.removeFirst()
                    player.addCard(newCard)
                }
            }
            println("${player.name} получил ${cardsToDraw} новых карт")
        }
        game.nextPhase()
    }
}