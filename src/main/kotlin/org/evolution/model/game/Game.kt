package org.evolution.model.game

import org.evolution.model.player.Player
import org.evolution.model.card.Card
import org.evolution.model.card.AnimalCard
import org.evolution.model.card.TraitCard
import org.evolution.model.animal.Animal
import kotlin.random.Random

class Game(val gameId: Int) {
    val players = mutableListOf<Player>()
    var foodPool: Int = 0
    val deck = mutableListOf<Card>()
    val discardPile = mutableListOf<Card>()

    // Инициализируем стартовой фазой
    var currentPhase: Phase = DevelopmentPhase()
    var currentPlayerIndex: Int = 0
    val history = History(gameId)

    fun initGame() {
        if (players.isEmpty()) return

        deck.shuffle()

        for (player in players) {
            repeat(6) {
                if (deck.isNotEmpty()) {
                    player.addCard(deck.removeFirst())
                }
            }
        }

        currentPlayerIndex = Random.nextInt(players.size)
        println("Игра началась. Первый ход у игрока: ${players[currentPlayerIndex].name}")
    }

    fun nextPhase() {
        currentPhase = when (currentPhase) {
            is DevelopmentPhase -> FeedingPhase()
            is FeedingPhase -> ExtinctionPhase()
            is ExtinctionPhase -> {
                if (deck.isNotEmpty()) {
                    prepareNewRound()
                    DevelopmentPhase()
                } else {
                    endGame()
                    return
                }
            }

            else -> DevelopmentPhase()
        }
        println("Переход к фазе: ${currentPhase::class.simpleName}")
        currentPhase.execute(this)
    }

    private fun prepareNewRound() {
        // Раздача карт в начале нового раунда
        for (player in players) {
            val cardsToDraw = player.animals.count { it.isAlive } + 1
            repeat(cardsToDraw) {
                if (deck.isNotEmpty()) player.addCard(deck.removeFirst())
            }
        }
    }

    private fun endGame() {
        calculateFinalScore()
        val winner = getWinner()
        println("Игра окончена! Победитель: ${winner?.name ?: "Ничья"} со счетом ${winner?.score}")
    }

    fun calculateFinalScore() {
        for (player in players) {
            var totalScore = 0
            for (animal in player.animals.filter { it.isAlive }) {
                totalScore += 2 // 2 очка за выжившее животное
                // Очки за свойства берутся из самих свойств
                totalScore += animal.traits.sumOf { it.calculateScore() }
            }
            player.score = totalScore
        }
    }

    fun getWinner(): Player? {
        return players.maxByOrNull { it.score }
    }

    // Проверка возможности розыгрыша карты из руки
    fun canPlayCard(player: Player, card: Card, target: Animal?): Boolean {
        if (currentPhase !is DevelopmentPhase) return false
        if (!player.hand.contains(card)) return false

        return when (card) {
            is AnimalCard -> true
            is TraitCard -> target != null && target in player.animals
            else -> false
        }
    }

    fun playCard(player: Player, card: Card, target: Animal?, actionType: ActionType) {
        if (card !is TraitCard) return

        when (actionType) {
            ActionType.PLAY_ANIMAL_CARD -> {
                // Любая карта свойства может стать новым животным
                val newAnimal = Animal(id = card.id) // Используем ID карты как ID животного
                player.addAnimal(newAnimal)
                println("Игрок ${player.name} создал животное из карты ${card.traitType}")
            }

            ActionType.PLAY_TRAIT_CARD -> {
                if (target != null) {
                    val newTrait = card.createNewTrait()
                    target.addTrait(newTrait)
                    println("Животное ${target.id} получило свойство ${newTrait.traitType}")
                }
            }
            /**
             Тут нужно поделать проверку других ходов
             */
            else -> { /* обработка других действий */
            }
        }

        player.removeCard(card)
        discardPile.add(card)

        // Запись в историю
        history.addMove(
            Move(
                moveId = history.moves.size + 1,
                gameId = gameId,
                playerId = player.playerId,
                actionType = actionType,
                cardId = card.id,
                targetAnimalId = target?.id
            )
        )
    }
}