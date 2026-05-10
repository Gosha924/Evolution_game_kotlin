import org.evolution.model.game.*
import org.evolution.repository.InMemoryGameRepository
import org.evolution.statistics.Statistics
import org.evolution.model.player.Player
import kotlin.test.Test
import kotlin.test.assertEquals

class StatisticsTest {
    @Test
    fun `test win rate calculation`() {
        val repo = InMemoryGameRepository()
        val stats = Statistics(repo)
        val player = Player("Winner", 1)

        // Имитируем 2 игры: одна победа, одно поражение
        val game1 = Game(repo.getNextGameId()).apply {
            players.add(player)
            player.score = 10
        }
        repo.saveGame(game1)

        val game2 = Game(repo.getNextGameId()).apply {
            players.add(player)
            players.add(Player("Other", 2).apply { score = 100 })
        }
        repo.saveGame(game2)

        // Ожидаем 50% WinRate
        assertEquals(50.0, stats.computeWinRate(player.playerId))
    }
}