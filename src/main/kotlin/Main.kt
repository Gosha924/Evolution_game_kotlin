package org.evolution

import org.evolution.ui.Console
import org.evolution.statistics.Statistics
import org.evolution.repository.InMemoryGameRepository


fun main() {
    val repository = InMemoryGameRepository()
    val statistics = Statistics(repository)
    val console = Console(statistics, repository)
    while (true) {
        println("1. Начать новую партию")
        println("2. Посмотреть статистику")
        println("3. Выход")
        print("Выберите действие: ")
        when (readlnOrNull()) {
            "1" -> console.startNewGame()
            "2" -> console.showStatistics()
            "3" -> console.exit()
            else -> println("Некорректный ввод")
        }
    }

}
