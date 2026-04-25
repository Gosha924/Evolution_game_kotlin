package org.evolution.model.animal

import org.evolution.model.trait.*

class Animal(val id: Int) {
    val traits = mutableListOf<Trait>()
    var foodEaten: Int = 0
    var isAlive: Boolean = true

    fun getSize(): Int {
        return 1 + traits.count { it is LargeTrait }
    }

    fun totalFoodRequired(): Int {
        return 1 + traits.sumOf { it.extraFoodRequired() }
    }

    fun addTrait(trait: Trait) {
        traits.add(trait)
    }

    fun isFull(): Boolean = foodEaten >= totalFoodRequired()

    fun needFood(): Int = (totalFoodRequired() - foodEaten).coerceAtLeast(0)

    fun die() {
        isAlive = false
    }

    fun resetRounding() {
        foodEaten = 0
    }
}