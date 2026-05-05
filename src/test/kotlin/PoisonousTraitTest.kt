import org.evolution.model.game.*
import org.evolution.model.player.Player
import org.evolution.model.animal.Animal
import org.evolution.model.trait.*
import kotlin.test.Test
import kotlin.test.assertFalse

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