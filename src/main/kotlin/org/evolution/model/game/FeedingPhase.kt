package org.evolution.model.game

import org.evolution.model.animal.Animal
import org.evolution.model.player.Player
import org.evolution.model.trait.*
import org.evolution.utils.loopUntilValid
import org.evolution.utils.printPlayerStatus

class FeedingPhase : Phase() {
    override fun execute(game: Game) {
        println("\nФАЗА ПИТАНИЯ")

        // СБРОС ПИРАТСТВА: перед началом фазы разрешаем всем пиратам действовать
        game.players.forEach { p ->
            p.animals.forEach { a ->
                a.traits.filterIsInstance<PiracyTrait>().forEach { it.usedThisTurn = false }
            }
        }

        val activePlayers = game.getPlayersInTurnOrder().toMutableList()

        while (activePlayers.isNotEmpty() && (game.foodPool > 0 || canStillAttack(activePlayers))) {
            val iterator = activePlayers.iterator()
            while (iterator.hasNext()) {
                val player = iterator.next()

                loopUntilValid {
                    println("\nХод: ${player.name} (В базе: ${game.foodPool})")
                    println("1 - Взять еду | 2 - Атака | 3 - Пиратство | 4 - Жир | 5 - ПАС | 6 - СТАТУС")

                    when (readlnOrNull()?.trim()) {
                        "1" -> if (game.foodPool > 0) handleFeeding(player, game) else false
                        "2" -> handleAttack(player, game)
                        "3" -> handlePiracy(player, game)
                        "4" -> handleFatUsage(player)
                        "5" -> { iterator.remove(); true }
                        "6" -> {
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
        forcedVictim: Animal? = null,
        traitToDrop: Trait? = null,
        runningEscapeSuccess: Boolean? = null,
        forcedMimicryTarget: Animal? = null,
    ): Boolean {
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

        if (victimPlayer == null && victim != null) {
            victimPlayer = game.players.find { it.animals.contains(victim) }
        }

        if (victim != null && victimPlayer != null) {
            if (predator == victim) {
                println("Ошибка: Хищник не может атаковать самого себя!")
                return false
            }

            val canAttack = predator.traits.all { it.canAttack(predator, victim) }
            val canBeAttacked = victim.traits.all { it.canBeAttacked(victim, predator) }

            if (canAttack && canBeAttacked) {
                var currentVictim = victim

                // МИМИКРИЯ
                val mimicryTrait = currentVictim.traits.filterIsInstance<MimicryTrait>().firstOrNull()
                if (mimicryTrait != null) {
                    val validTargets = victimPlayer!!.animals.filter { newTarget ->
                        newTarget != currentVictim && newTarget.isAlive &&
                                predator.traits.all { it.canAttack(predator, newTarget) } &&
                                newTarget.traits.all { it.canBeAttacked(newTarget, predator) }
                    }

                    if (validTargets.isNotEmpty()) {
                        // Используем параметр forcedMimicryTarget, если он передан (для тестов)
                        val redirectTarget = forcedMimicryTarget ?: run {
                            println("Мимикрия! Выберите новую цель: ${validTargets.map { it.id }} (0 для отмены)")
                            val id = readLine()?.toIntOrNull() ?: 0
                            if (id == 0) null else validTargets.find { it.id == id }
                        }

                        if (redirectTarget != null) {
                            println("Мимикрия: Атака перенаправлена на ${redirectTarget.id}!")
                            currentVictim = redirectTarget
                        }
                    }
                }

                // БЫСТРОЕ
                val runningTrait = currentVictim.traits.filterIsInstance<RunningTrait>().firstOrNull()
                if (runningTrait != null) {
                    val escaped = runningEscapeSuccess ?: run {
                        println("Животное ${currentVictim.id} — Быстрое! Выпало 4, 5 или 6? (y/n)")
                        readLine()?.lowercase() == "y"
                    }
                    if (escaped) {
                        println("Животное убежало!")
                        return false
                    }
                }

                // ОТБРАСЫВАНИЕ ХВОСТА
                var victimSurvived = false
                var foodToReceive = 2
                val tailLoss = currentVictim.traits.filterIsInstance<TailLossTrait>().firstOrNull()

                if (tailLoss != null) {
                    val droppedTrait = traitToDrop ?: run {
                        println("Отбрасывание хвоста! Выберите ID свойства для сброса (0 - умереть):")
                        currentVictim.traits.forEach { println("- ID: ${it.id} (${it.traitType})") }
                        val dropId = readLine()?.toIntOrNull()
                        if (dropId == 0 || dropId == null) null else currentVictim.traits.find { it.id == dropId }
                    }
                    if (droppedTrait != null) {
                        tailLoss.escape(currentVictim, droppedTrait)
                        victimSurvived = true
                        foodToReceive = 1
                    }
                }

                // СМЕРТЬ И ПИТАНИЕ
                if (!victimSurvived) {
                    println("Атака успешна! Животное ${currentVictim.id} съедено.")
                    currentVictim.traits.forEach { it.onDeathByPredator(currentVictim, predator) }
                    victimPlayer!!.animals.remove(currentVictim)
                    currentVictim.die()
                }

                repeat(foodToReceive) {
                    if (!predator.isFull()) {
                        predator.foodEaten++
                        predator.traits.forEach { it.onFeed(predator, game, false) }
                    } else {
                        predator.traits.filterIsInstance<FatTrait>().find { !it.filled }?.filled = true
                    }
                }

                if (!victimSurvived) handleScavengers(game)
                return true
            } else {
                println("Атака невозможна по правилам!")
                return false
            }
        }
        return false
    }

    internal fun handlePiracy(attacker: Player, game: Game, forcedPirate: Animal? = null, forcedTarget: Animal? = null): Boolean {
        val pirates = attacker.animals.filter { a ->
            a.traits.any { it is PiracyTrait && !it.usedThisTurn } && !a.isFull()
        }
        if (pirates.isEmpty() && forcedPirate == null) {
            println("Нет доступных пиратов.")
            return false
        }
        val pirate = forcedPirate ?: run {
            println("Выберите пирата: ${pirates.map { it.id }}")
            val id = readLine()?.toIntOrNull()
            pirates.find { it.id == id }
        } ?: return false

        val validTargets = game.players.flatMap { it.animals }.filter { it.foodEaten > 0 && it != pirate }
        if (validTargets.isEmpty() && forcedTarget == null) {
            println("Нет целей с едой.")
            return false
        }
        val target = forcedTarget ?: run {
            println("Выберите цель: ${validTargets.map { it.id }}")
            val id = readLine()?.toIntOrNull()
            validTargets.find { it.id == id }
        } ?: return false

        target.foodEaten--
        pirate.foodEaten++
        pirate.traits.filterIsInstance<PiracyTrait>().first().usedThisTurn = true
        println("Пират ${pirate.id} украл еду у ${target.id}!")
        pirate.traits.forEach { it.onFeed(pirate, game, false) }
        return true
    }

    internal fun handleFatUsage(player: Player): Boolean {
        val selectable = player.animals.filter { it.traits.any { t -> t is FatTrait && t.filled } }
        if (selectable.isEmpty()) return false
        println("Выберите животное для жира: ${selectable.map { it.id }}")
        val id = readLine()?.toIntOrNull()
        val animal = selectable.find { it.id == id } ?: return false
        if (animal.isFull()) return false
        val fat = animal.traits.filterIsInstance<FatTrait>().find { it.filled }
        fat?.filled = false
        animal.foodEaten++
        return true
    }

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

    private fun hasEmptyFatTissue(animal: Animal): Boolean = animal.traits.filterIsInstance<FatTrait>().any { !it.filled }
    private fun canStillAttack(players: List<Player>): Boolean = players.flatMap { it.animals }.any { it.traits.any { t -> t is PredatorTrait } && it.isAlive }
}