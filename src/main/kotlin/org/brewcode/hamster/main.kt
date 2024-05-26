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
import java.lang.Thread.sleep
import java.time.LocalDateTime.now
import java.util.concurrent.CompletableFuture
import kotlin.time.Duration.Companion.minutes


private val logger = KotlinLogging.logger {}

fun main() {

    logger.info { "Starting..." }
    logger.info { Cfg.toString() }

    configureSession()

    if (Cfg.auto_move_mouse) {
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

    var statistic = ExecutionStatistic(Cfg.timeout)
    println("\n > > > Started at '${now()}' | duration: ${Cfg.timeout}  < < < \n")
    statistic.printStatistic()

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

    statistic.printStatistic()
    logger.info { " > > > Finished at '${now()}' < < < " }
}

