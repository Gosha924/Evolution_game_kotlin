package org.evolution.model.trait

import org.evolution.model.animal.Animal
import org.evolution.model.game.Game

class LargeTrait(id: Int) : Trait(id, TraitType.LARGE) {
    override fun extraFoodRequired(): Int = 1
}

class ParasiteTrait(id: Int) : Trait(id, TraitType.PARASITE) {
    override fun extraFoodRequired(): Int = 2
    override fun calculateScore(): Int = 2
}

class FatTrait(id: Int) : Trait(id, TraitType.FAT) {
    var filled: Boolean = false

    override fun onFeed(animal: Animal, game: Game) {
        // Если животное уже сыто, еда может пойти в жировой запас
        if (animal.isFull() && !filled) {
            filled = true
            println("Жировой запас на животном ${animal.id} заполнен.")
        }
    }
}

// Свойства нападения

class PredatorTrait(id: Int) : Trait(id, TraitType.PREDATOR) {
    override fun extraFoodRequired(): Int = 1

    override fun canAttack(predator: Animal, victim: Animal): Boolean {
        return predator.getSize() >= victim.getSize()
    }
}

class SharpVisionTrait(id: Int) : Trait(id, TraitType.SHARP_VISION)

// Защитные свойства

class AquaticTrait(id: Int) : Trait(id, TraitType.AQUATIC) {
    override fun canBeAttacked(victim: Animal, predator: Animal): Boolean {
        return predator.traits.any { it.traitType == TraitType.AQUATIC }
    }
}

class BurrowingTrait(id: Int) : Trait(id, TraitType.BURROWING) {
    override fun canBeAttacked(victim: Animal, predator: Animal): Boolean {
        return !victim.isFull()
    }
}

class CamouflageTrait(id: Int) : Trait(id, TraitType.CAMOUFLAGE) {
    override fun canBeAttacked(victim: Animal, predator: Animal): Boolean {
        return predator.traits.any { it.traitType == TraitType.SHARP_VISION }
    }
}

class PoisonousTrait(id: Int) : Trait(id, TraitType.POISONOUS) {
    override fun onDeathByPredator(victim: Animal, predator: Animal) {
        predator.die() // Хищник погибает после фазы питания
    }
}

class RunningTrait(id: Int) : Trait(id, TraitType.RUNNING) {
    // В админке при атаке на такое животное консоль должна спросить: "Удался ли бросок кубика?"
}

class MimicryTrait(id: Int) : Trait(id, TraitType.MIMICRY) {
    // При атаке админ должен позволить перенаправить атаку на другое животное того же игрока
}


class ScavengerTrait(id: Int) : Trait(id, TraitType.SCAVENGER) {
    // Логика падалщика обычно реализуется в Game при обработке смерти любого животного
}

class HibernationTrait(id: Int) : Trait(id, TraitType.HIBERNATION) {
    var isUsedThisRound = false
}


// Парные свойства
abstract class PairedTrait(id: Int, type: TraitType) : Trait(id, type) {
    var partner: Animal? = null
}

class SymbiosisTrait(id: Int) : PairedTrait(id, TraitType.SYMBIOSIS) {
    override fun canBeAttacked(victim: Animal, predator: Animal): Boolean {
        // Жертва под симбиозом не может быть атакована, пока жив партнер
        return partner?.isAlive == false
    }
}

class CooperationTrait(id: Int) : PairedTrait(id, TraitType.COOPERATION) {
    override fun onFeed(animal: Animal, game: Game) {
        // Если это животное получает еду, напарник тоже получает из синей базы
        if (game.foodPool > 0) {
            partner?.let { if (!it.isFull()) { it.foodEaten++; game.foodPool-- } }
        }
    }
}

class CommunicationTrait(id: Int) : PairedTrait(id, TraitType.COMMUNICATION) {
    override fun onFeed(animal: Animal, game: Game) {
        // Аналогично кооперации
        // В базе — берется из общей кормовой базы.
        if (game.foodPool > 0) {
            partner?.let { if (!it.isFull()) { it.foodEaten++; game.foodPool-- } }
        }
    }
}

class PiracyTrait(id: Int) : Trait(id, TraitType.PIRACY) {
    // В рамках приложения-администратора сам факт применения пиратства
    // будет вводиться вручную как отдельное действие в фазе питания.
}

// Топотун
// Когда животное берет 1 фишку еды, еще 1 фишка еды из кормовой базы уничтожается.
class TramplingTrait(id: Int) : Trait(id, TraitType.TRAMPLING) {
    override fun onFeed(animal: Animal, game: Game) {
        if (game.foodPool > 0) {
            game.foodPool--
            println("Сработало свойство 'Топотун' (ID ${animal.id}): 1 еда уничтожена из базы.")
        }
    }
}

// Отбрасывание хвоста
class TailLossTrait(id: Int) : Trait(id, TraitType.TAIL_LOSS) {
    // Логика отбрасывания хвоста обрабатываться когда администратор укажет, что жертва спаслась ценой свойства.
}