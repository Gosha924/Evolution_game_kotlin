import org.evolution.model.game.*
import org.evolution.model.player.Player
import org.evolution.model.animal.Animal
import org.evolution.model.trait.*

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

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