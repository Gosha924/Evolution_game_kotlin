import org.evolution.model.game.*
import org.evolution.model.animal.Animal
import org.evolution.model.trait.*
import kotlin.test.Test
import kotlin.test.assertEquals

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