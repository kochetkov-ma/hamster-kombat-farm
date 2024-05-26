package org.brewcode.hamster.action

import com.codeborne.selenide.Condition
import com.codeborne.selenide.Condition.text
import io.github.oshai.kotlinlogging.KotlinLogging
import org.brewcode.hamster.action.GameBoostAction.boostStamina
import org.brewcode.hamster.action.GameCommonAction.goToBack
import org.brewcode.hamster.action.GameCommonAction.goToExchange
import org.brewcode.hamster.action.GameEarnAction.tryDailyEarn
import org.brewcode.hamster.action.GameMineAction.chooseAndBuyUpgrades
import org.brewcode.hamster.service.UpgradeService.updateUpgrades
import org.brewcode.hamster.Cfg
import org.brewcode.hamster.util.Retryer.Companion.retry
import org.brewcode.hamster.util.progress
import org.brewcode.hamster.view.main.MainView
import org.brewcode.hamster.view.tg.TelegramView
import kotlin.time.toJavaDuration


object GameFarmAction {

    private val logger = KotlinLogging.logger {}

    fun farm(statistic: ExecutionStatistic): ExecutionStatistic {
        var currentStatic = statistic

        while (currentStatic.elapsedMs < statistic.duration.inWholeMilliseconds) {
            currentStatic = currentStatic.updateIterations()
            val clicks = 5
            repeat(clicks) { MainView.hamsterButton.clickLikeHuman() }
            currentStatic = currentStatic.updateClicks(clicks)

            if (currentStatic.iterations % Cfg.staminaCheckPeriod == 0) {

                if (currentStatic.iterations % (Cfg.staminaCheckPeriod * 5) == 0)
                    currentStatic.printStatistic()

                val stamina = MainView.staminaLevel()
                if (stamina.second == 0) println("ERROR STAMINA: " + MainView.staminaText.text)
                logger.info { "Check Stamina: $stamina" }

                if (stamina.first < Cfg.staminaMinimumLevel) {
                    GameLaunchAction.reload()

                    if (MainView.staminaLevel().first < Cfg.staminaMinimumLevel + 500) {
                        logger.info { "Try use boost..." }
                        boostStamina()

                        runCatching { MainView.hamsterButton.shouldBe(Condition.visible) }
                            .onFailure { GameLaunchAction.reload() }

                        if (MainView.staminaLevel().first < Cfg.staminaMinimumLevel + 500) {
                            val max = MainView.staminaLevel().second

                            retry("Try daily earn")
                                .noRetry()
                                .ignoreErrors()
                                .onFail { logger.info { "Daily error : " + it.message } }
                                .action { tryDailyEarn() }
                                .evaluate()

                            retry("Choose and buy upgrades")
                                .ignoreErrors()
                                .onFail {
                                    if (TelegramView.searchButton.isDisplayed || TelegramView.searchInput.isDisplayed || TelegramView.playButton.isDisplayed) {
                                        logger.info { "Game crushed and now Telegram view open" }
                                        TelegramAction.closeTelegram()
                                        TelegramAction.openTelegram()
                                        if (TelegramAction.openHamsterBot())
                                            GameLaunchAction.loadTheGameFromBotChat()
                                    } else goToBack()

                                    goToExchange()
                                }
                                .action { chooseAndBuyUpgrades(Cfg.buy_something, Cfg.min_cost, Cfg.target_upgrade) }
                                .evaluate()
                            statistic.printStatistic()

                            if (currentStatic.iterations % (Cfg.staminaCheckPeriod * 5) == 0)
                                updateUpgrades()

                            logger.info { "Wait [${Cfg.staminaWaitInterval}] till entire refresh..." }

                            val control = progress()

                            runCatching { MainView.staminaText.shouldBe(text("$max / $max"), Cfg.staminaWaitInterval.toJavaDuration()) }
                                .onFailure {
                                    logger.error { "Stamina is " + MainView.staminaLevel() + " after long wait " + Cfg.staminaWaitInterval }
                                    control.set(false)
                                }.onSuccess { control.set(false) }
                        }
                    }
                }
            }
        }

        return currentStatic
    }
}
