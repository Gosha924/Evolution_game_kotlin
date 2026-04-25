package org.evolution.model.game

// Класс для хранения и получения истории ходов партии.

class History(private val gameId: Int) {
    private val _moves = mutableListOf<Move>()
    val moves: List<Move> = _moves

    fun addMove(move: Move) {
        require(move.gameId == gameId) {
            "Cannot add move for game ${move.gameId} to history of game $gameId"
        }
        _moves.add(move)
    }

    fun getGameHistory(): List<Move> = moves
    fun getMovesByPlayer(playerId: Int): List<Move> = moves.filter { it.playerId == playerId }
}