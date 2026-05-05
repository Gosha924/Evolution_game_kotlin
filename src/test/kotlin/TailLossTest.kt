import org.evolution.model.game.*
import org.evolution.model.player.Player
import org.evolution.model.animal.Animal
import org.evolution.model.trait.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

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