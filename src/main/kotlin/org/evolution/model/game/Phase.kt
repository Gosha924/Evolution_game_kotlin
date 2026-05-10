package org.evolution.model.game

sealed class Phase {
    abstract fun execute(game: Game)
}