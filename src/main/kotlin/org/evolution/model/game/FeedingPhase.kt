package org.evolution.model.game

import org.evolution.model.animal.Animal
import org.evolution.model.player.Player
import org.evolution.model.trait.FatTrait
import org.evolution.model.trait.PredatorTrait
import org.evolution.model.trait.ScavengerTrait
import org.evolution.utils.loopUntilValid
import org.evolution.utils.printPlayerStatus

class FeedingPhase : Phase() {
    override fun execute(game: Game) {
        println("\n=== ФАЗА ПИТАНИЯ ===")
        val activePlayers = game.getPlayersInTurnOrder().toMutableList()

        while (activePlayers.isNotEmpty() && (game.foodPool > 0 || canStillAttack(activePlayers))) {
            val iterator = activePlayers.iterator()
            while (iterator.hasNext()) {
                val player = iterator.next()

                loopUntilValid {
                    println("\nХод: ${player.name} (В базе: ${game.foodPool})")
                    println("1 - Взять еду | 2 - Атака | 3 - Жир | 4 - ПАС | 5 - МОИ ЖИВОТНЫЕ (СТАТУС)")

                    when (readlnOrNull()?.trim()) {
                        "1" -> if (game.foodPool > 0) handleFeeding(player, game) else false
                        "2" -> handleAttack(player, game)
                        "3" -> handleFatUsage(player)
                        "4" -> { iterator.remove(); true }
                        "5" -> {
                            printPlayerStatus(player, showFood = true)
                            false // Остаемся в цикле, ход не тратится
                        }
                        else -> false
                    }
                }
            }
        }
    }

    private fun handleFeeding(player: Player, game: Game): Boolean {
        val hungryAnimals = player.animals.filter { !it.isFull() || hasEmptyFatTissue(it) }
        if (hungryAnimals.isEmpty()) {
            println("У вас нет голодных животных или пустого жирового запаса.")
            return false
        }

        println("Выберите ID животного: ${hungryAnimals.map { it.id }}")
        val id = readLine()?.toIntOrNull()
        val animal = hungryAnimals.find { it.id == id }

        return if (animal != null) {
            if (!animal.isFull()) {
                animal.foodEaten++
                game.foodPool--
                println("Животное ${animal.id} накормлено.")
                // Вызываем триггер onFeed (для логики связей или бонусов)
                animal.traits.forEach { it.onFeed(animal, game) }
            } else {
                // Если сыто, но есть жировой запас
                val fatTrait = animal.traits.filterIsInstance<FatTrait>().find { !it.filled }
                if (fatTrait != null) {
                    fatTrait.filled = true
                    game.foodPool--
                    println("Животное ${animal.id} заполнило жировой запас.")
                }
            }
            true
        } else {
            println("Животное не найдено.")
            false
        }
    }

    private fun handleAttack(attacker: Player, game: Game): Boolean {
        val predators = attacker.animals.filter { it.traits.any { t -> t is PredatorTrait } }
        if (predators.isEmpty()) {
            println("У вас нет хищников.")
            return false
        }

        println("Выберите вашего хищника (ID): ${predators.map { it.id }}")
        val predId = readLine()?.toIntOrNull()
        val predator = predators.find { it.id == predId } ?: return false

        println("Выберите цель (ID игрока и ID животного):")
        game.players.forEach { p -> println("Игрок ${p.playerId} (${p.name}): ${p.animals.filter { it.isAlive }.map { it.id }}") }

        val vPlayerId = readLine()?.toIntOrNull()
        val vAnimalId = readLine()?.toIntOrNull()

        val victimPlayer = game.players.find { it.playerId == vPlayerId }
        val victim = victimPlayer?.animals?.find { it.id == vAnimalId && it.isAlive }

        if (predator != null && victim != null) {
            // Проверка правил через Traits
            val canAttack = predator.traits.all { it.canAttack(predator, victim) }
            val canBeAttacked = victim.traits.all { it.canBeAttacked(victim, predator) }

            if (canAttack && canBeAttacked) {
                println("Атака успешна! Животное ${victim.id} игрока ${victimPlayer.name} съедено.")

                // Триггер смерти (например, Ядовитое убивает хищника)
                victim.traits.forEach { it.onDeathByPredator(victim, predator) }
                victim.die()

                // Хищник получает 2 еды
                val foodToReceive = 2
                repeat(foodToReceive) {
                    if (!predator.isFull()) {
                        predator.foodEaten++
                    } else {
                        val fat = predator.traits.filterIsInstance<FatTrait>().find { !it.filled }
                        fat?.filled = true
                    }
                }

                // Свойство Падальщик: другие животные могут получить еду при смерти этого
                handleScavengers(game)

                return true
            } else {
                println("Атака невозможна по правилам!")
            }
        }
        return false
    }

    private fun handleFatUsage(player: Player): Boolean {
        println("Выберите животное для перевода жира в еду: ${player.animals.filter { it.traits.any { t -> t is FatTrait && (t as FatTrait).filled } }.map { it.id }}")
        val id = readLine()?.toIntOrNull()
        val animal = player.animals.find { it.id == id } ?: return false
        val fat = animal.traits.filterIsInstance<FatTrait>().find { it.filled }

        return if (fat != null && !animal.isFull()) {
            fat.filled = false
            animal.foodEaten++
            println("Жир конвертирован в еду.")
            true
        } else false
    }

    private fun handleScavengers(game: Game) {
        // Логика: каждое животное со свойством Падальщик получает 1 еду из банка
        game.players.forEach { p ->
            p.animals.filter { it.isAlive && it.traits.any { t -> t is ScavengerTrait } }.forEach { scav ->
                if (!scav.isFull() && game.foodPool > 0) {
                    scav.foodEaten++
                    game.foodPool--
                    println("Падальщик ${scav.id} получил еду.")
                }
            }
        }
    }


    private fun hasEmptyFatTissue(animal: Animal): Boolean {
        return animal.traits.filterIsInstance<FatTrait>().any { !it.filled }
    }

    private fun canStillAttack(players: List<Player>): Boolean {
        // Проверка, остались ли в игре живые хищники
        return players.flatMap { it.animals }.any { it.traits.any { t -> t is PredatorTrait } && it.isAlive }
    }
}