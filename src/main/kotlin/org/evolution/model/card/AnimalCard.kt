package org.evolution.model.card

import org.evolution.model.animal.Animal

class AnimalCard(id: Int): Card(id) {

    // Создаёт новое животное
    fun createNewAnimal(): Animal {
        val animalId = (System.currentTimeMillis() % 1000000).toInt() + (Math.random() * 1000).toInt()
        return Animal(id = animalId)
    }

    override fun play() {
    }
}