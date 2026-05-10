package org.evolution.repository
import org.evolution.model.game.Game
import org.evolution.model.game.Move

class InMemoryGameRepository : GameRepository {
    private val games = mutableMapOf<Int, Game>()
    private val moves = mutableMapOf<Int, MutableList<Move>>()
    private var nextGameId = 1

    override fun getNextGameId(): Int = nextGameId++

    override fun saveGame(game: Game) {
        games[game.gameId] = game
    }

    override fun loadGame(gameId: Int): Game? = games[gameId]

    override fun saveMove(move: Move) {
        moves.getOrPut(move.gameId) { mutableListOf() }.add(move)
    }

    override fun getAllMovesForGame(gameId: Int): List<Move> {
        return moves[gameId] ?: emptyList()
    }

    override fun getAllGames(): List<Game> = games.values.toList()
}