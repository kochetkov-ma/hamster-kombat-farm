package org.brewcode.hamster.action

import com.codeborne.selenide.Condition
import com.codeborne.selenide.Condition.text
import io.github.oshai.kotlinlogging.KotlinLogging
import org.brewcode.hamster.action.GameBoostAction.boostStamina
import org.brewcode.hamster.action.GameCommonAction.goToBack
import org.brewcode.hamster.action.GameCommonAction.goToExchange
import org.brewcode.hamster.action.GameEarnAction.tryDailyEarn
import org.brewcode.hamster.action.GameMineAction.chooseAndBuyUpgrades
import org.brewcode.hamster.buy_something
import org.brewcode.hamster.service.UpgradeService.updateUpgrades
import org.brewcode.hamster.staminaCheckPeriod
import org.brewcode.hamster.staminaMinimumLevel
import org.brewcode.hamster.staminaWaitInterval
import org.brewcode.hamster.util.Retryer.Companion.retry
import org.brewcode.hamster.view.main.MainView
import org.brewcode.hamster.view.tg.TelegramView
import kotlin.time.toJavaDuration


object GameFarmAction {

    private val logger = KotlinLogging.logger {}

    fun farm(statistic: ExecutionStatistic): ExecutionStatistic {
        var currentStatic = statistic

        while (currentStatic.updateTime().elapsedMs < statistic.duration.inWholeMilliseconds) {
            currentStatic = currentStatic.updateIterations()
            val clicks = 5
            repeat(clicks) { MainView.hamsterButton.clickLikeHuman() }
            currentStatic = currentStatic.updateTime().updateClicks(clicks)

            if (currentStatic.iterations % staminaCheckPeriod == 0) {
                currentStatic.println()

                val stamina = MainView.staminaLevel()
                if (stamina.second == 0) println("ERROR STAMINA: " + MainView.staminaText.text)
                logger.info { "Check Stamina: $stamina" }

                if (stamina.first < staminaMinimumLevel) {
                    GameLaunchAction.reload()

                    if (MainView.staminaLevel().first < staminaMinimumLevel + 500) {
                        logger.info { "Try use boost..." }
                        boostStamina()

                        runCatching { MainView.hamsterButton.shouldBe(Condition.visible) }
                            .onFailure { GameLaunchAction.reload() }

                        if (MainView.staminaLevel().first < staminaMinimumLevel + 500) {
                            val max = MainView.staminaLevel().second
                            logger.info { "Try wait till entire refresh..." }

                            retry("Try daily earn")
                                .noRetry()
                                .ignoreErrors()
                                .onFail { logger.info { "Daily error : " + it.localizedMessage } }
                                .action { tryDailyEarn() }
                                .evaluate()

                            retry("Choose and buy upgrades")
                                .ignoreErrors()
                                .onFail {
                                    if (TelegramView.searchInput.isDisplayed) {
                                        logger.info { "Game crushed and now Telegram view open" }
                                        TelegramAction.closeTelegram()
                                        TelegramAction.openHamsterBot()
                                        GameLaunchAction.loadTheGameFromBotChat()
                                    } else goToBack()

                                    goToExchange()
                                }
                                .action { chooseAndBuyUpgrades(buy_something) }
                                .evaluate()

                            if (currentStatic.iterations % (staminaCheckPeriod * 5) == 0)
                                updateUpgrades()

                            runCatching { MainView.staminaText.shouldBe(text("$max / $max"), staminaWaitInterval.toJavaDuration()) }
                                .onFailure { logger.error { "Stamina is " + MainView.staminaLevel() + " after long wait " + staminaWaitInterval } }
                        }
                    }
                }
            }
        }

        return currentStatic
    }
}
