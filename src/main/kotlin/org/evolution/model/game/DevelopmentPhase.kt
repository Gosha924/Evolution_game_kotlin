package org.evolution.model.game

import org.evolution.model.player.Player
import org.evolution.model.card.TraitCard

class DevelopmentPhase : Phase() {
    override fun execute(game: Game) {
        println("\n--- ФАЗА РАЗВИТИЯ ---")
        val passed = mutableSetOf<Player>()

        // Основной цикл фазы
        while (passed.size < game.players.size) {
            for (player in game.players) {
                if (player in passed) continue

                if (player.hand.isEmpty()) {
                    println("У ${player.name} закончились карты.")
                    passed.add(player)
                    continue
                }
                var turnCompleted = false
                while (!turnCompleted) {
                    println("\nХод игрока ${player.name}. Карт в руке: ${player.hand.size}")
                    println("Выберите действие: 1 - Создать животное, 2 - Сыграть свойство, 0 - ПАС")

                    val input = readlnOrNull()?.trim()

                    when (input) {
                        "1" -> {
                            val card = player.hand.first()
                            game.playCard(player, card, null, ActionType.PLAY_ANIMAL_CARD)
                            turnCompleted = true // Действие выполнено, передаем ход
                        }
                        "2" -> {
                            if (player.animals.isEmpty()) {
                                println("У вас нет животных! Сначала создайте животное.")
                                // turnCompleted остается false, игрок пробует снова
                            } else {
                                val card = player.hand.firstOrNull()
                                if (card is TraitCard) {
                                    println("Выберите ID вашего животного: ${player.animals.map { it.id }}")
                                    val animalId = readlnOrNull()?.toIntOrNull()
                                    val target = player.animals.find { it.id == animalId }

                                    if (target != null) {
                                        game.playCard(player, card, target, ActionType.PLAY_TRAIT_CARD)
                                        turnCompleted = true // Успешно сыграли карту
                                    } else {
                                        println("Ошибка: Животное с ID $animalId не найдено.")
                                    }
                                } else {
                                    println("Упс! Первая карта в руке не является свойством.")
                                }
                            }
                        }
                        "0", "" -> {
                            println("${player.name} пасует до конца раунда.")
                            passed.add(player)
                            turnCompleted = true // Пас — это тоже завершение хода
                        }
                        else -> {
                            println("Ошибка: Неверный ввод '$input'. Введите 1, 2 или 0.")
                            // turnCompleted false, цикл повторится
                        }
                    }
                }
            }
        }
        println("\n--- Фаза развития завершена ---")
    }
}