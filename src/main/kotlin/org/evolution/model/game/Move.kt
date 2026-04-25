package org.evolution.model.game

import java.util.Date

data class Move(
    val moveId: Int,
    val gameId: Int,
    val playerId: Int,
    val actionType: ActionType,
    val cardId: Int,
    val targetAnimalId: Int? = null,
    val foodAmount: Int? = null,
    val timestamp: Date = Date()
)
