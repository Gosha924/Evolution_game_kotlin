import org.evolution.model.game.*
import org.evolution.model.player.Player
import org.evolution.model.animal.Animal
import org.evolution.model.trait.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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