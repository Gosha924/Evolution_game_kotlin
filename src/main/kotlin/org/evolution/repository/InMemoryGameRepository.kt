package org.evolution.repository

import org.evolution.model.game.Game
import org.evolution.model.game.Move

class InMemoryGameRepository : GameRepository {
    private val games = mutableListOf<Game>()
    private val moves = mutableListOf<Move>()

    override fun saveGame(game: Game) {
        val index = games.indexOfFirst { it.gameId == game.gameId }
        if (index != -1) games[index] = game
        else games.add(game)
    }

    override fun loadGame(gameId: Int): Game {
        return games.find { it.gameId == gameId }
            ?: throw NoSuchElementException("Game with id $gameId not found")
    }

    override fun saveMove(move: Move) {
        val index = moves.indexOfFirst { it.moveId == move.moveId }
        if (index != -1) moves[index] = move
        else moves.add(move)
    }

    override fun getAllMovesForGame(gameId: Int): List<Move> {
        return moves.filter { it.gameId == gameId }
    }

    override fun getAllGames(): List<Game> {
        return games.toList()
    }
}