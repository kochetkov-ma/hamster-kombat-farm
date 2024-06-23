package org.brewcode.hamster

import io.github.oshai.kotlinlogging.KotlinLogging
import org.brewcode.hamster.Cfg.cfgPath
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
import kotlin.io.path.Path
import kotlin.io.path.notExists
import kotlin.time.Duration.Companion.minutes


private val logger = KotlinLogging.logger {}

fun main() {

    logger.debug { "Starting..." }

    logger.info { "Current directory: " + Path(".").toAbsolutePath() }

    if (cfgPath.notExists())
        throw Error(
            "EN: Configuration file not found: $cfgPath. Copy 'brew-hamster.yaml' from release archive to 'brew-hamster.yaml' and fill it by instruction!"
                + "\nRU: Файл конфигурации не найден: $cfgPath. Скопируйте 'brew-hamster.yaml' из релиза в 'brew-hamster.yaml' и заполните его по инструкции!"
        )

    logger.debug { Cfg.toString() }

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
    logger.info { "Started at '${now()}' | duration: ${Cfg.timeout}" }
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

