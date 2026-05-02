package org.evolution.repository
import org.evolution.model.game.Game
import org.evolution.model.game.Move

interface GameRepository {
    fun getNextGameId(): Int
    fun saveGame(game: Game)
    fun loadGame(gameId: Int): Game?
    fun saveMove(move: Move)
    fun getAllMovesForGame(gameId: Int): List<Move>
    fun getAllGames(): List<Game>
}
