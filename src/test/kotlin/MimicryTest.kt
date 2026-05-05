import org.evolution.model.game.*
import org.evolution.model.player.Player
import org.evolution.model.animal.Animal
import org.evolution.model.trait.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

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