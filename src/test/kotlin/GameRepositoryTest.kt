import org.evolution.model.game.*
import org.evolution.repository.InMemoryGameRepository
import kotlin.test.Test
import kotlin.test.assertEquals
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