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