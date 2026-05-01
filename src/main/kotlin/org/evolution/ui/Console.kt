package org.evolution.ui

import org.evolution.model.game.Game
import org.evolution.model.player.Player
import org.evolution.model.card.Card
import org.evolution.model.card.TraitCard
import org.evolution.model.trait.TraitType
import org.evolution.statistics.Statistics
import org.evolution.repository.GameRepository
import kotlin.system.exitProcess

class Console(
    private val statistics: Statistics,
    private val repository: GameRepository
) {

    fun showStatistics() {
        val leaderboard = statistics.getLeaderboard()
        if (leaderboard.isEmpty()) {
            println("Нет сохранённых игр")
            return
        }
        println("\n=== ТАБЛИЦА ЛИДЕРОВ ===")
        println("Игрок\t\tПобеды\tСредний счёт\tWinRate")
        for (player in leaderboard) {
            val wins = player.score
            val avgScore = statistics.computeAverageScore(player.playerId)
            val winRate = statistics.computeWinRate(player.playerId)
            println("${player.name}\t\t$wins\t%.1f\t\t%.1f%%".format(avgScore, winRate))
        }
        println("========================\n")
    }

    fun startNewGame() {
        println("\n=== НОВАЯ ИГРА ===")
        val playerCount = readPlayerCount()
        val players = createPlayers(playerCount)
        val game = Game(repository.getNextGameId())

        game.initGame(players, createDeck())

        println("Игра создана. Начинаем партию...")
        game.start()

        val winner = game.getWinner()
        println("Игра завершена. Победитель: ${winner?.name ?: "Ничья"}")

        repository.saveGame(game)
        println("Партия сохранена в базу данных\n")
    }

    fun exit() {
        println("Выход из программы")
        exitProcess(0)
    }

    private fun readPlayerCount(): Int {
        while (true) {
            print("Введите количество игроков (2-4): ")
            val input = readlnOrNull()?.trim()
            val count = input?.toIntOrNull()
            if (count != null && count in 2..4) {
                return count
            }
            println("Некорректный ввод. Попробуйте ещё раз")
        }
    }

    private fun createPlayers(count: Int): List<Player> {
        val players = mutableListOf<Player>()
        for (i in 1..count) {
            print("Имя игрока $i: ")
            val name = readlnOrNull()?.trim()?.takeIf { it.isNotEmpty() } ?: "Игрок$i"
            players.add(Player(name, i))
        }
        return players
    }


    private fun createDeck(): List<Card> {
        val deck = mutableListOf<Card>()
        var currentId = 1
        val traitTypes = TraitType.values()

        for (type in traitTypes) {
            repeat(4) {
                deck.add(TraitCard(currentId++, type))
            }
        }

        val commonTraits = listOf(TraitType.PREDATOR, TraitType.FAT, TraitType.LARGE, TraitType.COOPERATION)
        repeat(2) {
            for (type in commonTraits) {
                deck.add(TraitCard(currentId++, type))
            }
        }
        return deck.shuffled()
    }
}