import org.evolution.model.game.*
import org.evolution.repository.InMemoryGameRepository
import org.evolution.statistics.Statistics
import org.evolution.model.player.Player
import org.evolution.model.animal.Animal
import org.evolution.model.trait.*
import org.evolution.model.card.TraitCard
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class GameRepositoryTest {
    private val repository = InMemoryGameRepository()

    @Test
    fun `test save and load game`() {
        val gameId = repository.getNextGameId()
        val game = Game(gameId)

        repository.saveGame(game)
        val loadedGame = repository.loadGame(gameId)

        assertNotNull(loadedGame)
        assertEquals(gameId, loadedGame?.gameId)
    }

    @Test
    fun `test return null for non-existing game`() {
        val loadedGame = repository.loadGame(999)
        assertNull(loadedGame)
    }
}


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

class GameTurnOrderTest {
    @Test
    fun `test players turn order starting from index 1`() {
        val game = Game(1)
        val p1 = Player("A", 1)
        val p2 = Player("B", 2)
        val p3 = Player("C", 3)
        game.players.addAll(listOf(p1, p2, p3))

        game.currentPlayerIndex = 1

        val order = game.getPlayersInTurnOrder()

        // Ожидаемый порядок: B, C, A
        assertEquals("B", order[0].name)
        assertEquals("C", order[1].name)
        assertEquals("A", order[2].name)
    }
}


class EvolutionIntegrationTest {

    @Test
    fun `test complex predator attack and starvation scenario`() {
        val repository = InMemoryGameRepository()
        val game = Game(repository.getNextGameId())
        val feedingPhase = FeedingPhase()

        val playerA = Player("Хищник-Тим", 1)
        val playerB = Player("Жертва-Боб", 2)
        game.players.addAll(listOf(playerA, playerB))

        val predatorAnimal = Animal(id = 10)
        predatorAnimal.traits.add(TraitCard(1, TraitType.PREDATOR).createNewTrait())
        playerA.animals.add(predatorAnimal)

        val preyAnimal = Animal(id = 20)
        preyAnimal.traits.add(TraitCard(2, TraitType.PIRACY).createNewTrait())
        playerB.animals.add(preyAnimal)

        game.foodPool = 1

        // Сценарий: Хищник атакует Большое животное
        // В норме хищник получает 2 еды за атаку
        feedingPhase.handleAttack(playerA, game, predatorAnimal, preyAnimal)

        // Жертва должна умереть
        assertTrue(playerB.animals.isEmpty(), "Животное Боба должно быть удалено из списка")

        //  Хищник должен получить 2 единицы еды и стать сытым
        assertEquals(2, predatorAnimal.foodEaten, "Хищник должен получить 2 еды после атаки")
        assertTrue(predatorAnimal.isFull(), "Хищник должен быть полностью сыт")

        assertEquals(1, game.foodPool, "Еда в базе не должна уменьшиться, так как хищник поел за счет жертвы")

        val extinction = ExtinctionPhase()
        extinction.execute(game)

        //  Проверяем, что хищник выжил
        assertTrue(playerA.animals.contains(predatorAnimal), "Хищник должен выжить в фазе вымирания")
    }
}