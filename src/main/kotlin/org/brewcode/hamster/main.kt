package org.brewcode.hamster

import io.github.oshai.kotlinlogging.KotlinLogging
import org.brewcode.hamster.action.ExecutionStatistic
import org.brewcode.hamster.action.GameFarmAction.farm
import org.brewcode.hamster.action.GameLaunchAction.fastReload
import org.brewcode.hamster.action.GameLaunchAction.load
import org.brewcode.hamster.action.OpenHamsterBot.closeTelegram
import org.brewcode.hamster.action.OpenHamsterBot.openHamsterBot
import org.brewcode.hamster.action.OpenHamsterBot.openTelegram
import org.brewcode.hamster.util.configureSession
import org.brewcode.hamster.util.retry
import org.brewcode.hamster.view.HamsterKombatGameView
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

const val availableBoostLevel = 0
val timeout = 7.hours
const val staminaCheckPeriod = 5
const val staminaMinimumLevel = 500
val staminaWaitInterval = 10.minutes

private val logger = KotlinLogging.logger {}

fun main() {

    logger.info { "Start..." }
    logger.info { "Timeout: $timeout | Stamina check period: $staminaCheckPeriod | Minimum stamina level: $staminaMinimumLevel" }

    configureSession()

    openTelegram()
    if (openHamsterBot())
        load()

    val hamsterView = HamsterKombatGameView()
    val now = LocalDateTime.now()
    val initAmount = hamsterView.amount()

    var statistic = ExecutionStatistic(timeout)
    logger.info { "Start with timeout: $timeout at '$now' | Amount $initAmount" }

    retry(5, onFail = {
        closeTelegram()
        openTelegram()
        if (openHamsterBot())
            load()
    }) {
        retry(5, onFail = { fastReload() }) {
            statistic = farm(statistic)
        }
    }

    val profit = hamsterView.amount() - initAmount
    logger.info { "Finished at '$now' | Clicks: ${statistic.iterations * staminaCheckPeriod * 5} | Amount: ${hamsterView.amount()} | Profit: $profit | Speed: ${profit / (statistic.elapsedMs / 1000)} coins/sec" }
}

