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
import kotlin.test.assertFalse
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

class PairInteractionTest {
    @Test
    fun `test differences between Cooperation and Communication`() {
        val game = Game(1).apply { foodPool = 10 }
        println()
        // 1. Создаем и оживляем животных
        val animalA = Animal(id = 1).apply { isAlive = true }
        val animalB = Animal(id = 2).apply { isAlive = true }
        val coop = CooperationTrait(101).apply { partner = animalB }
        animalA.traits.add(coop)

        val animalC = Animal(id = 3).apply { isAlive = true }
        val animalD = Animal(id = 4).apply { isAlive = true }
        val comm = CommunicationTrait(102).apply { partner = animalD }
        animalC.traits.add(comm)

        // СЦЕНАРИЙ 1: Хищник А съел кого-то (еда НЕ из базы)
        animalA.foodEaten++
        // Cooperation срабатывает на ЛЮБУЮ еду, поэтому передаем false
        animalA.traits.forEach { it.onFeed(animalA, game, isFromPool = false) }

        // Теперь эти проверки должны пройти:
        assertEquals(1, animalB.foodEaten, "Сотрудничество должно сработать")
        assertEquals(9, game.foodPool, "Еда для партнера взята из базы")

        // СЦЕНАРИЙ 2: Животное В съело кого-то (еда НЕ из базы)
        animalC.foodEaten++
        animalC.traits.forEach { it.onFeed(animalC, game, isFromPool = false) }

        assertEquals(0, animalD.foodEaten, "Взаимодействие НЕ должно сработать, так как еда пришла не из базы")

        // СЦЕНАРИЙ 3: Животное В берет еду из БАЗЫ
        game.foodPool--
        animalC.foodEaten++
        animalC.traits.forEach { it.onFeed(animalC, game, isFromPool = true) }

        assertEquals(1, animalD.foodEaten, "Теперь Взаимодействие должно сработать")
        assertEquals(7, game.foodPool, "В базе осталось 7 (было 9, -1 за В, -1 за D)")
    }
}

class PoisonousTraitTest {
    @Test
    fun `test predator dies after eating poisonous animal`() {
        println()
        val game = Game(1)
        val feedingPhase = FeedingPhase()

        val playerA = Player("Хищник", 1)
        val playerB = Player("Жертва", 2)
        game.players.addAll(listOf(playerA, playerB))

        val predator = Animal(id = 10).apply { isAlive = true }
        predator.traits.add(PredatorTrait(1))
        playerA.animals.add(predator)

        val victim = Animal(id = 20).apply { isAlive = true }
        victim.traits.add(PoisonousTrait(2))
        playerB.animals.add(victim)

        feedingPhase.handleAttack(playerA, game, predator, victim)

        // Жертва должна быть мертва и удалена
        assertFalse(victim.isAlive, "Жертва должна погибнуть")

        // ХИЩНИК должен погибнуть от яда
        assertFalse(predator.isAlive, "Хищник должен погибнуть от яда после атаки")

        println("Тест пройден: Хищник съел жертву, но отравился.")
    }
}


class TailLossTest {
    @Test
    fun `test victim survives by dropping a trait`() {
        println()
        val game = Game(1)
        val feedingPhase = FeedingPhase()
        val playerA = Player("PredatorPlayer", 1)
        val playerB = Player("PreyPlayer", 2)
        game.players.addAll(listOf(playerA, playerB))

        val predator = Animal(id = 10).apply { isAlive = true }
        predator.traits.add(PredatorTrait(1))
        playerA.animals.add(predator)

        val victim = Animal(id = 20).apply { isAlive = true }
        val tailLoss = TailLossTrait(2)
        val otherTrait = FatTrait(3)
        victim.traits.addAll(listOf(tailLoss, otherTrait))
        playerB.animals.add(victim)

        val success = feedingPhase.handleAttack(
            attacker = playerA,
            game = game,
            forcedPredator = predator,
            forcedVictim = victim,
            traitToDrop = otherTrait
        )

        assertTrue(success, "Атака должна быть успешно проведена")

        assertTrue(victim.isAlive, "Жертва должна выжить")
        assertTrue(playerB.animals.contains(victim), "Жертва должна остаться в списке игрока")

        assertFalse(victim.traits.contains(otherTrait), "Свойство FatTrait должно быть удалено")
        assertEquals(1, victim.traits.size, "У жертвы должно остаться ровно одно свойство")
        assertTrue(victim.traits[0] is TailLossTrait, "Оставшееся свойство должно быть TailLoss")

        assertEquals(1, predator.foodEaten, "Хищник должен получить 1 еду за отброшенный хвост")
    }
}

class RunningTraitTest {
    @Test
    fun `test victim escapes when running dice roll is successful`() {
        val game = Game(1)
        val feedingPhase = FeedingPhase()
        val playerA = Player("PredatorPlayer", 1)
        val playerB = Player("PreyPlayer", 2)
        game.players.addAll(listOf(playerA, playerB))

        val predator = Animal(id = 10).apply { isAlive = true }
        predator.traits.add(PredatorTrait(1))
        playerA.animals.add(predator)

        val victim = Animal(id = 20).apply { isAlive = true }
        victim.traits.add(RunningTrait(2))
        playerB.animals.add(victim)

        // Атака с УДАЧНЫМ броском (кубик 4-6)
        val attackResult = feedingPhase.handleAttack(
            playerA, game, predator, victim,
            runningEscapeSuccess = true
        )

        assertFalse(attackResult, "Атака должна провалиться, так как жертва убежала")
        assertTrue(victim.isAlive, "Жертва должна остаться живой")
        assertEquals(0, predator.foodEaten, "Хищник не должен получить еду, если жертва убежала")
    }

        @Test
        fun `test predator catches victim when running dice roll fails`() {
            val game = Game(1)
            val feedingPhase = FeedingPhase()
            val playerA = Player("PredatorPlayer", 1)
            val playerB = Player("PreyPlayer", 2)
            game.players.addAll(listOf(playerA, playerB))

            val predator = Animal(id = 10).apply { isAlive = true }
            predator.traits.add(PredatorTrait(1))
            playerA.animals.add(predator)

            val victim = Animal(id = 20).apply { isAlive = true }
            victim.traits.add(RunningTrait(2))
            playerB.animals.add(victim)

            // Атака с НЕУДАЧНЫМ броском (кубик 1-3)
            val attackResult = feedingPhase.handleAttack(
                playerA, game, predator, victim,
                runningEscapeSuccess = false
            )
            assertTrue(attackResult, "Атака должна быть успешной")
            assertFalse(victim.isAlive, "Жертва должна быть съедена")
            assertTrue(predator.foodEaten > 0, "Хищник должен получить еду")
        }
    }


class MimicryTest {
    @Test
    fun `test mimicry redirects attack to another animal`() {
        val game = Game(1)
        val feedingPhase = FeedingPhase()
        val attackerPlayer = Player("PredatorPlayer", 1)
        val victimPlayer = Player("PreyPlayer", 2)
        game.players.addAll(listOf(attackerPlayer, victimPlayer))

        val predator = Animal(id = 10).apply { isAlive = true }
        predator.traits.add(PredatorTrait(1))
        attackerPlayer.animals.add(predator)

        val mimicVictim = Animal(id = 21).apply { isAlive = true }
        mimicVictim.traits.add(MimicryTrait(2))
        victimPlayer.animals.add(mimicVictim)
        val normalVictim = Animal(id = 22).apply { isAlive = true }
        victimPlayer.animals.add(normalVictim)

        val success = feedingPhase.handleAttack(
            attacker = attackerPlayer,
            game = game,
            forcedPredator = predator,
            forcedVictim = mimicVictim,
            forcedMimicryTarget = normalVictim
        )

        assertTrue(success, "Атака должна завершиться успешно")
        assertTrue(mimicVictim.isAlive, "Животное с Мимикрией должно остаться живым")
        assertFalse(normalVictim.isAlive, "Животное, на которое перевели атаку, должно погибнуть")
        assertEquals(2, predator.foodEaten, "Хищник должен получить еду за съеденную цель")
    }
}

class PiracyTest {
    @Test
    fun `test piracy steals food successfully`() {
        val game = Game(1)
        val feedingPhase = FeedingPhase()
        val playerA = Player("PiratePlayer", 1)
        val playerB = Player("TargetPlayer", 2)
        game.players.addAll(listOf(playerA, playerB))

        val pirate = Animal(id = 10).apply { isAlive = true }
        pirate.traits.add(PiracyTrait(1))
        playerA.animals.add(pirate)

        // Жертва ограбления: Имеет 1 еду
        val target = Animal(id = 20).apply { isAlive = true; foodEaten = 1 }
        // Добавляем большое свойство, чтобы увеличить аппетит и не считаться "сытым", если это важно для логики игры
        target.traits.add(LargeTrait(2))
        playerB.animals.add(target)

        // Выполняем пиратство
        val success = feedingPhase.handlePiracy(playerA, game, forcedPirate = pirate, forcedTarget = target)

        assertTrue(success, "Пиратство должно сработать")
        assertEquals(1, pirate.foodEaten, "Пират должен получить 1 еду")
        assertEquals(0, target.foodEaten, "У жертвы должно стать 0 еды")

        val piracyTrait = pirate.traits.filterIsInstance<PiracyTrait>().first()
        assertTrue(piracyTrait.usedThisTurn, "Свойство должно пометиться как использованное")
    }
}