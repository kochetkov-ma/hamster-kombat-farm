package org.brewcode.hamster.action

import com.codeborne.selenide.Condition
import com.codeborne.selenide.Condition.text
import io.github.oshai.kotlinlogging.KotlinLogging
import org.brewcode.hamster.Cfg
import org.brewcode.hamster.Cfg.staminaWaitInterval
import org.brewcode.hamster.Cfg.stamina_check_period
import org.brewcode.hamster.Cfg.upgrade_enabled
import org.brewcode.hamster.action.GameBoostAction.boostStamina
import org.brewcode.hamster.action.GameCommonAction.goToBack
import org.brewcode.hamster.action.GameCommonAction.goToExchange
import org.brewcode.hamster.action.GameEarnAction.tryDailyEarn
import org.brewcode.hamster.action.GameMineAction.chooseAndBuyUpgrades
import org.brewcode.hamster.service.UpgradeService.updateUpgrades
import org.brewcode.hamster.util.Retryer.Companion.retry
import org.brewcode.hamster.util.progress
import org.brewcode.hamster.view.main.MainView
import org.brewcode.hamster.view.tg.TelegramView
import kotlin.time.toJavaDuration


object GameFarmAction {

    private val logger = KotlinLogging.logger {}

    fun farm(statistic: ExecutionStatistic): ExecutionStatistic {

        while (statistic.elapsedMs < statistic.duration.inWholeMilliseconds) {
            statistic.updateIterations()
            val clicks = 5
            repeat(clicks) { MainView.hamsterButton.clickLikeHuman() }
            statistic.updateClicks(clicks)

            if (statistic.iterations % stamina_check_period == 0) {

                if (statistic.iterations % (stamina_check_period * 5) == 0)
                    statistic.printStatistic()

                val stamina = MainView.staminaLevel()
                if (stamina.second == 0) logger.error { "ERROR STAMINA: " + MainView.staminaText.text }
                logger.trace { "Check Stamina: $stamina" }

                if (stamina.first < Cfg.stamina_minimum_level) {
                    GameLaunchAction.reload()

                    if (MainView.staminaLevel().first < Cfg.stamina_minimum_level + 500) {
                        logger.debug { "Try use boost..." }
                        boostStamina()

                        runCatching { MainView.hamsterButton.shouldBe(Condition.visible) }
                            .onFailure { GameLaunchAction.reload() }

                        if (MainView.staminaLevel().first < Cfg.stamina_minimum_level + 500) {
                            val max = MainView.staminaLevel().second

                            retry("Try daily earn")
                                .noRetry()
                                .ignoreErrors()
                                .onFail { logger.info { "Daily error : " + it.message } }
                                .action { tryDailyEarn() }
                                .evaluate()

                            if (upgrade_enabled)
                                retry("Choose and buy upgrades")
                                    .ignoreErrors()
                                    .onFail {
                                        if (TelegramView.searchButton.isDisplayed || TelegramView.searchInput.isDisplayed || TelegramView.playButton.isDisplayed) {
                                            logger.error { "Game crushed and now Telegram view open" }
                                            TelegramAction.closeTelegram()
                                            TelegramAction.openTelegram()
                                            if (TelegramAction.openHamsterBot())
                                                GameLaunchAction.loadTheGameFromBotChat()
                                        } else goToBack()

                                        goToExchange()
                                    }
                                    .action { chooseAndBuyUpgrades() }
                                    .evaluate()
                            statistic.printStatistic()

                            if (statistic.iterations % (stamina_check_period * 10) == 0)
                                updateUpgrades()

                            logger.info { "Wait [$staminaWaitInterval] till entire refresh..." }

                            val control = progress()
                            runCatching { MainView.staminaText.shouldBe(text("$max / $max"), staminaWaitInterval.toJavaDuration()) }
                                .onFailure {
                                    control.set(false)
                                    logger.error { "Stamina is " + MainView.staminaLevel() + " after long wait " + staminaWaitInterval }
                                }.onSuccess { control.set(false) }
                        }
                    }
                }
            }
        }

        return statistic
    }
}
