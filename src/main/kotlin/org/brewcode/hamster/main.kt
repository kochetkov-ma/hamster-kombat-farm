package org.brewcode.hamster

import io.github.oshai.kotlinlogging.KotlinLogging
import org.brewcode.hamster.action.ExecutionStatistic
import org.brewcode.hamster.action.GameFarmAction.farm
import org.brewcode.hamster.action.GameLaunchAction.loadTheGameFromBotChat
import org.brewcode.hamster.action.GameLaunchAction.reload
import org.brewcode.hamster.action.MoverAction
import org.brewcode.hamster.action.TelegramAction.closeTelegram
import org.brewcode.hamster.action.TelegramAction.openHamsterBot
import org.brewcode.hamster.action.TelegramAction.openTelegram
import org.brewcode.hamster.util.Retryer.Companion.retry
import org.brewcode.hamster.util.configureSession
import org.brewcode.hamster.view.main.MainView
import java.lang.Thread.sleep
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

const val availableBoostLevel = 0
val timeout = 8.hours
const val staminaCheckPeriod = 5
const val staminaMinimumLevel = 250
val staminaWaitInterval = 10.minutes
val buy_something = true
val min_cost = 1_000
val auto_move_mouse = true
val target_upgrade = ""

private val logger = KotlinLogging.logger {}

fun main() {

    logger.info { "Start..." }
    logger.info { "Timeout: $timeout | Stamina check period: $staminaCheckPeriod | Minimum stamina level: $staminaMinimumLevel" }

    configureSession()

    if (auto_move_mouse) {
        val future = CompletableFuture.runAsync {
            while (true) {
                sleep(1.minutes.inWholeMilliseconds)
                MoverAction.mouseMove()
            }
        }
        Runtime.getRuntime().addShutdownHook(Thread { future.cancel(true) })
    }

    openTelegram()
    if (openHamsterBot())
        loadTheGameFromBotChat()

    val hamsterView = MainView
    val now = LocalDateTime.now()
    val initAmount = hamsterView.coinsAmount()

    var statistic = ExecutionStatistic(timeout)
    logger.info { "Start with timeout: $timeout at '$now' | Amount $initAmount" }

    retry("Main loop with Telegram reopening")
        .maxAttempts(10)
        .onFail {
            closeTelegram()
            openTelegram()
            if (openHamsterBot())
                loadTheGameFromBotChat()
        }
        .action {
            retry("Farming loop with fast reload")
                .maxAttempts(2)
                .onFail { reload() }
                .action { statistic = farm(statistic) }
                .evaluate()
        }
        .evaluate()

    val profit = hamsterView.coinsAmount() - initAmount
    logger.info { "Finished at '$now' | Clicks: ${statistic.iterations * staminaCheckPeriod * 5} | Amount: ${hamsterView.coinsAmount()} | Profit: $profit | Speed: ${profit / (statistic.elapsedMs / 1000)} coins/sec" }
}

