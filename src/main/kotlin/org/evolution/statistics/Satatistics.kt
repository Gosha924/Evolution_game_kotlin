package org.evolution.statistics

import org.evolution.repository.GameRepository
import org.evolution.model.player.Player

class Statistics(private val repository: GameRepository) {

    // Вычисляет процент побед игрока
    fun computeWinRate(playerId: Int): Double {
        val allGames = repository.getAllGames()
        val totalGames = allGames.count { game -> game.players.any { it.playerId == playerId } }
        if (totalGames == 0) {
            return 0.0
        }
        val wins = allGames.count { game ->
            val winner = game.getWinner()
            winner != null && winner.playerId == playerId
        }
        return (wins.toDouble() / totalGames) * 100.0
    }

    // Вычисляет средний счёт игрока за все сыгранные партии
    fun computeAverageScore(playerId: Int): Double {
        val allGames = repository.getAllGames()
        var totalScore = 0
        var gamesPlayed = 0

        for (game in allGames) {
            val player = game.players.find { it.playerId == playerId }
            if (player != null) {
                totalScore += player.score
                gamesPlayed++
            }
        }
        return if (gamesPlayed == 0) 0.0 else totalScore.toDouble() / gamesPlayed
    }

    fun getLeaderboard(): List<Player> {
        val allGames = repository.getAllGames()
        val playerStats = mutableMapOf<Int, PlayerStat>()

        // Собираем статистику по каждому игроку
        for (game in allGames) {
            val winner = game.getWinner()
            for (player in game.players) {
                val stats = playerStats.getOrPut(player.playerId) {
                    PlayerStat(player.name, player.playerId)
                }
                stats.gamesPlayed++
                stats.totalScore += player.score
                if (winner != null && winner.playerId == player.playerId) {
                    stats.wins++
                }
            }
        }
        // Сортируем по убыванию побед, затем по убыванию среднего счёта
        val sortedStats = playerStats.values.sortedWith(
            compareByDescending<PlayerStat> { it.wins }
                .thenByDescending { it.averageScore }
        )
        return sortedStats.map { stats ->
            Player(stats.name, stats.playerId).apply {
                score = stats.wins
            }
        }
    }

    private data class PlayerStat(
        val name: String,
        val playerId: Int,
        var gamesPlayed: Int = 0,
        var totalScore: Int = 0,
        var wins: Int = 0
    ) {
        val averageScore: Double
            get() = if (gamesPlayed == 0) 0.0 else totalScore.toDouble() / gamesPlayed
    }
}