package org.evolution.model.trait

import org.evolution.model.animal.Animal
import org.evolution.model.game.Game

abstract class Trait(val id: Int, val traitType: TraitType) {

    // Сколько дополнительной еды требует животное из-за этого свойства?
    open fun extraFoodRequired(): Int = 0

    // Сколько очков дает в конце игры
    open fun calculateScore(): Int = 1

    // Триггер при получении животным еды (например, для Жирового запаса)
    open fun onFeed(animal: Animal, game: Game) {}

    // Может ли этот хищник атаковать конкретную жертву?
    open fun canAttack(predator: Animal, victim: Animal): Boolean = true

    // Защитное свойство: может ли жертва быть атакована этим хищником?
    open fun canBeAttacked(victim: Animal, predator: Animal): Boolean = true

    // Триггер, срабатывающий, если животное съели (для Ядовитого)
    open fun onDeathByPredator(victim: Animal, predator: Animal) {}
}