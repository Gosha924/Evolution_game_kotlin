package org.evolution

import org.evolution.ui.Console
import org.evolution.statistics.Statistics
import org.evolution.repository.InMemoryGameRepository


fun main() {
    val repository = InMemoryGameRepository()
    val statistics = Statistics(repository)
    val console = Console(statistics, repository)
    while (true) {
        console.startNewGame()
    }

}
