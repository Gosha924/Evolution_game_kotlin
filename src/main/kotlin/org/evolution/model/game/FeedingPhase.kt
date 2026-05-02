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
                            false
                        }
                        else -> false
                    }
                }
            }
        }
    }

    internal fun handleFeeding(player: Player, game: Game, forcedAnimal: Animal? = null): Boolean {
        val hungryAnimals = player.animals.filter { !it.isFull() || hasEmptyFatTissue(it) }
        if (hungryAnimals.isEmpty() && forcedAnimal == null) {
            println("У вас нет голодных животных или пустого жирового запаса.")
            return false
        }

        // Если пришло из теста — используем его, если нет — спрашиваем консоль
        val animal = forcedAnimal ?: run {
            println("Выберите ID животного: ${hungryAnimals.map { it.id }}")
            val id = readLine()?.toIntOrNull()
            hungryAnimals.find { it.id == id }
        }

        return if (animal != null) {
            if (!animal.isFull()) {
                animal.foodEaten++
                game.foodPool--
                println("Животное ${animal.id} накормлено.")
                animal.traits.forEach { it.onFeed(animal, game, true) }
            } else {
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

    internal fun handleAttack(
        attacker: Player,
        game: Game,
        forcedPredator: Animal? = null,
        forcedVictim: Animal? = null
    ): Boolean {
        // Логика выбора хищника
        val predator = forcedPredator ?: run {
            val predators = attacker.animals.filter { it.traits.any { t -> t is PredatorTrait } }
            if (predators.isEmpty()) {
                println("У вас нет хищников.")
                return false
            }
            println("Выберите вашего хищника (ID): ${predators.map { it.id }}")
            val predId = readLine()?.toIntOrNull()
            predators.find { it.id == predId }
        } ?: return false

        var victimPlayer: Player? = null
        val victim = forcedVictim ?: run {
            println("Выберите цель (ID игрока и ID животного):")
            game.players.forEach { p -> println("Игрок ${p.playerId} (${p.name}): ${p.animals.map { it.id }}") }

            val vPlayerId = readLine()?.toIntOrNull()
            val vAnimalId = readLine()?.toIntOrNull()

            victimPlayer = game.players.find { it.playerId == vPlayerId }
            victimPlayer?.animals?.find { it.id == vAnimalId && it.isAlive }
        }

        // Если это тест, ищем владельца жертвы автоматически
        if (victimPlayer == null && victim != null) {
            victimPlayer = game.players.find { it.animals.contains(victim) }
        }

        if (victim != null && victimPlayer != null) {
            val canAttack = predator.traits.all { it.canAttack(predator, victim) }
            val canBeAttacked = victim.traits.all { it.canBeAttacked(victim, predator) }

            if (canAttack && canBeAttacked) {
                println("Атака успешна! Животное ${victim.id} игрока ${victimPlayer?.name} съедено.")
                victim.traits.forEach { it.onDeathByPredator(victim, predator) }

                // Удаляем жертву из списка игрока
                victimPlayer?.animals?.remove(victim)
                victim.die()

                val foodToReceive = 2
                repeat(foodToReceive) {
                    if (!predator.isFull()) {
                        predator.foodEaten++
                    } else {
                        val fat = predator.traits.filterIsInstance<FatTrait>().find { !it.filled }
                        fat?.filled = true
                    }
                }
                handleScavengers(game)
                return true
            } else {
                println("Атака невозможна по правилам!")
            }
        }
        return false
    }

    internal fun handleFatUsage(player: Player): Boolean {
        val selectable = player.animals.filter { it.traits.any { t -> t is FatTrait && t.filled } }
        if (selectable.isEmpty()) return false

        println("Выберите животное для перевода жира в еду: ${selectable.map { it.id }}")
        val id = readLine()?.toIntOrNull()
        val animal = selectable.find { it.id == id } ?: return false
        val fat = animal.traits.filterIsInstance<FatTrait>().find { it.filled }

        return if (fat != null && !animal.isFull()) {
            fat.filled = false
            animal.foodEaten++
            println("Жир конвертирован в еду.")
            true
        } else false
    }

    // Вспомогательные методы тоже можно сделать internal для тестов
    internal fun handleScavengers(game: Game) {
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

    private fun hasEmptyFatTissue(animal: Animal): Boolean =
        animal.traits.filterIsInstance<FatTrait>().any { !it.filled }

    private fun canStillAttack(players: List<Player>): Boolean =
        players.flatMap { it.animals }.any { it.traits.any { t -> t is PredatorTrait } && it.isAlive }
}