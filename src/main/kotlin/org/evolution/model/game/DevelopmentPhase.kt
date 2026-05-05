package org.evolution.model.game

import org.evolution.model.player.Player
import org.evolution.model.card.TraitCard
import org.evolution.utils.loopUntilValid
import org.evolution.utils.printPlayerStatus

class DevelopmentPhase : Phase() {
    override fun execute(game: Game) {
        println("\nФАЗА РАЗВИТИЯ")
        val passed = mutableSetOf<Player>()
        val orderedPlayers = game.getPlayersInTurnOrder()

        while (passed.size < game.players.size) {
            for (player in orderedPlayers) {
                if (player.hand.isEmpty() && player !in passed) {
                    println("${player.name} автоматически пасует (нет карт).")
                    passed.add(player)
                }
                if (player in passed) continue

                loopUntilValid {
                    println("\nХод игрока ${player.name}. Карт: ${player.hand.size}")
                    println("1 - Создать животное | 2 - Сыграть свойство | 3 - МОИ ЖИВОТНЫЕ | 0 - ПАС")

                    when (readlnOrNull()?.trim()) {
                        "1" -> {
                            val card = pickCardFromHand(player) ?: return@loopUntilValid false
                            game.playCard(player, card, null, ActionType.PLAY_ANIMAL_CARD)
                            true
                        }
                        "2" -> {
                            if (player.animals.isEmpty()) {
                                println("Ошибка: У вас нет животных для навешивания свойств!")
                                return@loopUntilValid false
                            }
                            val card = pickCardFromHand(player) ?: return@loopUntilValid false

                            println("Выберите ID животного: ${player.animals.map { it.id }}")
                            val id = readlnOrNull()?.toIntOrNull()
                            val target = player.animals.find { it.id == id }

                            if (target != null) {
                                game.playCard(player, card, target, ActionType.PLAY_TRAIT_CARD)
                                true
                            } else {
                                println("Ошибка: Животное с ID $id не найдено.")
                                false
                            }
                        }
                        "3" -> {
                            printPlayerStatus(player, showFood = false)
                            false
                        }
                        "0" -> {
                            passed.add(player)
                            println("${player.name} пасует.")
                            true
                        }
                        else -> {
                            println("Некорректный ввод. Выберите действие от 0 до 3.")
                            false
                        }
                    }
                }
            }
        }
    }

    private fun pickCardFromHand(player: Player): TraitCard? {
        val traitCards = player.hand.filterIsInstance<TraitCard>()

        if (traitCards.isEmpty()) {
            println("В руке нет доступных свойств.")
            return null
        }
        println("Ваши карты:")
        traitCards.forEachIndexed { i, card ->
            println("${i + 1} - ${card.traitType}")
        }

        print("Выберите номер карты: ")
        val index = (readlnOrNull()?.toIntOrNull() ?: 0) - 1
        val selectedCard = traitCards.getOrNull(index)

        if (selectedCard == null) {
            println("Ошибка: Неверный номер карты.")
        }
        return selectedCard
    }
}