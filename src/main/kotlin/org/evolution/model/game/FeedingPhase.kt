package org.evolution.model.game

import org.evolution.model.animal.Animal
import org.evolution.model.player.Player
import org.evolution.model.trait.FatTrait
import org.evolution.model.trait.PredatorTrait
import org.evolution.model.trait.ScavengerTrait

class FeedingPhase : Phase() {
    override fun execute(game: Game) {
        println("\n=== ФАЗА ПИТАНИЯ ===")

        // Список игроков, которые еще не спасовали в этом раунде
        val activePlayers = game.players.toMutableList()

        // Очищаем статус "использовано" для свойств типа Спячка (если есть)
        game.players.flatMap { it.animals }.flatMap { it.traits }.forEach {
            // Тут можно сбросить флаги использования свойств раз в раунд
        }

        while (activePlayers.isNotEmpty() && (game.foodPool > 0 || canStillAttack(activePlayers))) {
            val iterator = activePlayers.iterator()
            while (iterator.hasNext()) {
                val player = iterator.next()

                // Если у игрока нет животных или все сыты/нет еды/нет хищников, он может только пасовать
                println("\nХод игрока: ${player.name} (Еды в банке: ${game.foodPool})")
                println("1 - Взять еду из кормовой базы")
                println("2 - Атака хищником")
                println("3 - Использовать жировой запас")
                println("4 - ПАС")

                when (readLine()?.toIntOrNull()) {
                    1 -> {
                        if (game.foodPool > 0) {
                            if (handleFeeding(player, game)) continue
                        } else {
                            println("Кормовая база пуста!")
                        }
                    }
                    2 -> {
                        if (handleAttack(player, game)) continue
                    }
                    3 -> {
                        if (handleFatUsage(player)) continue
                    }
                    4 -> {
                        println("Игрок ${player.name} пасовал до конца фазы.")
                        iterator.remove()
                    }
                    else -> println("Неверный ввод. Выберите 1-4.")
                }
            }
        }

        println("\n--- Фаза питания завершена. Применяем эффекты голодания... ---")
        applyStarvation(game)
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

    private fun applyStarvation(game: Game) {
        game.players.forEach { player ->
            val iterator = player.animals.iterator()
            while (iterator.hasNext()) {
                val animal = iterator.next()
                if (animal.foodEaten < animal.totalFoodRequired()) {
                    println("Животное ${animal.id} игрока ${player.name} погибло от голода.")
                    animal.die()
                    // В реальном репозитории здесь может быть удаление из списка
                } else {
                    animal.resetRounding() // Обнуляем еду для следующего раунда
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