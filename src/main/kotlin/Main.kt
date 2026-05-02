package org.evolution

import org.evolution.ui.Console
import org.evolution.statistics.Statistics
import org.evolution.repository.InMemoryGameRepository


fun main() {
    val repository = InMemoryGameRepository()
    val statistics = Statistics(repository)
    val console = Console(statistics, repository)
    while (true) {
        println("1. Новая игра")
        println("2. Показать статистику")
        println("0. Выход")
        print("Выберите действие: ")
        when (readlnOrNull()?.trim()) {
            "1" -> {
                try {
                    console.startNewGame()
                } catch (e: Exception) {
                    println("Произошла ошибка во время игры: ${e.message}")
                    e.printStackTrace()
                }
            }
            "2" -> console.showStatistics()
            "0" -> {
                console.exit()
                break
            }
            else -> println("Некорректный ввод, попробуйте снова.")
        }
    }
}
