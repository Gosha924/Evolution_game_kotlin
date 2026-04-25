package org.evolution.model.animal
import org.evolution.model.trait.*

class Animal(val id: Int) {
    val traits = mutableListOf<Trait>()
    var foodEaten: Int = 0
    var isAlive: Boolean = true
    val pairedAnimals = mutableMapOf<TraitType, Animal>()

    fun addPair(type: TraitType, partner: Animal) {
        pairedAnimals[type] = partner
    }

    fun getSize(): Int {
        return 1 + traits.count { it is LargeTrait }
    }

    fun totalFoodRequired(): Int {
        return 1 + traits.sumOf { it.extraFoodRequired() }
    }

    fun addTrait(trait: Trait) {
        traits.add(trait)
    }

    fun isFull(): Boolean {
        val required = totalFoodRequired()
        return foodEaten >= required
    }

    fun needFood(): Int = (totalFoodRequired() - foodEaten).coerceAtLeast(0)

    fun die() {
        isAlive = false
    }

    fun resetRounding() {
        foodEaten = 0
    }
}