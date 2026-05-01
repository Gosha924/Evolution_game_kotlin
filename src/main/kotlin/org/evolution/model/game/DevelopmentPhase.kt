package org.evolution.model.game

import org.evolution.model.player.Player
import org.evolution.model.card.TraitCard
import org.evolution.utils.loopUntilValid
import org.evolution.utils.printPlayerStatus

class DevelopmentPhase : Phase() {
    override fun execute(game: Game) {
        println("\n--- ФАЗА РАЗВИТИЯ ---")
        val passed = mutableSetOf<Player>()
        val orderedPlayers = game.getPlayersInTurnOrder()

        while (passed.size < game.players.size) {
            for (player in orderedPlayers) {
                if (player in passed || player.hand.isEmpty()) {
                    if (player !in passed) {
                        println("${player.name} автоматически пасует (нет карт).")
                        passed.add(player)
                    }
                    continue
                }

                loopUntilValid {
                    println("\nХод игрока ${player.name}. Карт: ${player.hand.size}")
                    println("1 - Создать животное | 2 - Сыграть свойство | 5 - МОИ ЖИВОТНЫЕ | 0 - ПАС")

                    when (readlnOrNull()?.trim()) {
                        "1" -> {
                            val card = pickCardFromHand(player) ?: return@loopUntilValid false
                            game.playCard(player, card, null, ActionType.PLAY_ANIMAL_CARD)
                            true
                        }
                        "2" -> {
                            if (player.animals.isEmpty()) {
                                println("У вас нет животных!")
                                return@loopUntilValid false
                            }
                            val card = pickCardFromHand(player) ?: return@loopUntilValid false
                            println("Выберите ID животного: ${player.animals.map { it.id }}")
                            val id = readlnOrNull()?.toIntOrNull()
                            val target = player.animals.find { it.id == id }
                            if (target != null) {
                                game.playCard(player, card, target, ActionType.PLAY_TRAIT_CARD)
                                true
                            } else false
                        }
                        "5" -> {
                            printPlayerStatus(player, showFood = false)
                            false // Возвращаем false, чтобы цикл повторился и игрок выбрал действие
                        }
                        "0" -> { passed.add(player); true }
                        else -> false
                    }
                }
            }
        }
    }

    private fun pickCardFromHand(player: Player): TraitCard? {
        println("Ваши карты:")
        player.hand.forEachIndexed { i, c -> println("${i + 1} - ${(c as TraitCard).traitType}") }
        print("Выберите номер: ")
        return player.hand.getOrNull((readlnOrNull()?.toIntOrNull() ?: 0) - 1) as? TraitCard
    }
}