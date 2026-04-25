package org.evolution.model.game

import org.evolution.model.player.Player
import org.evolution.model.card.TraitCard

class DevelopmentPhase : Phase() {
    override fun execute(game: Game) {
        println("\n--- ФАЗА РАЗВИТИЯ ---")
        val passed = mutableSetOf<Player>()

        // Цикл идет до тех пор, пока все не пасанут
        while (passed.size < game.players.size) {
            for (player in game.players) {
                if (player in passed) continue

                if (player.hand.isEmpty()) {
                    println("У ${player.name} закончились карты.")
                    passed.add(player)
                    continue
                }
                println("\nХод игрока ${player.name}. Карт в руке: ${player.hand.size}")
                println("Выберите действие: 1 - Создать животное, 2 - Сыграть свойство, 0 - ПАС")

                val input = readlnOrNull()

                when (input) {
                    // тут пока заглушка
                    "1" -> {
                        val card = player.hand.first() // В реальной игре тут должен быть выбор индекса
                        game.playCard(player, card, null, ActionType.PLAY_ANIMAL_CARD)
                    }
                    "2" -> {
                        if (player.animals.isEmpty()) {
                            println("У вас нет животных, чтобы вешать свойства! Создайте животное.")
                            continue
                        }

                        val card = player.hand.first() as TraitCard
                        println("Выберите ID вашего животного: ${player.animals.map { it.id }}")
                        val animalId = readlnOrNull()?.toIntOrNull()
                        val target = player.animals.find { it.id == animalId }

                        if (target != null) {
                            game.playCard(player, card, target, ActionType.PLAY_TRAIT_CARD)
                        } else {
                            println("Животное с таким ID не найдено.")
                        }
                    }
                    "0", "" -> {
                        println("${player.name} пасует до конца фазы.")
                        passed.add(player)
                    }
                    else -> println("Неверный ввод. Попробуйте снова.")
                }
            }
        }
        println("\n--- Фаза развития завершена ---")
    }
}