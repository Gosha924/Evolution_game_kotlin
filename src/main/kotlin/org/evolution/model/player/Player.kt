package org.evolution.model.player

import org.evolution.model.animal.Animal
import org.evolution.model.card.Card

class Player(val name: String, val playerId: Int) {
    val animals = mutableListOf<Animal>()
    val hand = mutableListOf<Card>()
    var score: Int = 0

    fun addCard(card: Card) {
        hand.add(card)
    }
    fun removeCard(card: Card) {
        hand.remove(card)
    }

    fun addAnimal(animal: Animal) {
        animals.add(animal)
    }

    fun removeAnimal(animal: Animal) {
        animals.remove(animal)
    }

}