package org.brewcode.hamster.action

import com.codeborne.selenide.Condition
import com.codeborne.selenide.Condition.hidden
import com.codeborne.selenide.WebDriverRunner
import io.appium.java_client.android.AndroidDriver
import io.appium.java_client.android.nativekey.AndroidKey
import io.appium.java_client.android.nativekey.KeyEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.brewcode.hamster.action.TelegramAction.closeTelegram
import org.brewcode.hamster.util.Retryer.Companion.retry
import org.brewcode.hamster.util.configureSession
import org.brewcode.hamster.view.main.MainView
import org.brewcode.hamster.view.tg.TelegramView

object TelegramAction {
    private val logger = KotlinLogging.logger {}

    fun closeTelegram() {
        var i = 0
        while (TelegramView.goBack.isDisplayed && i < 5) {
            logger.info { "Go Back Telegram..." }
            runCatching { TelegramView.goBack.click() }
            i++
        }

        retry("Close Telegram")
            .maxAttempts(2)
            .delay(500)
            .action {
                logger.info { "Closing Telegram..." }
                (WebDriverRunner.getWebDriver() as AndroidDriver).pressKey(KeyEvent(AndroidKey.HOME))
                TelegramView.searchInput.should(hidden)
                logger.info { "Telegram Closed!" }
            }
            .evaluate()
    }

    fun openTelegram() {

        if (TelegramView.telegramApp.isDisplayed) {
            logger.info { "Telegram opening..." }
            TelegramView.telegramApp.click()
            TelegramView.searchButton.should(Condition.visible)
            logger.info { "Telegram opened!" }
        }
    }

    fun openHamsterBot(): Boolean {
        val telegramView = TelegramView
        val hamsterView = MainView

        val isInGame = hamsterView.hamsterButton.isDisplayed
        val isInBotChat = hamsterView.hamsterButton.isDisplayed

        if (isInGame) {
            logger.info { "Bot already open!" }
            return false
        }

        if (isInBotChat) {
            logger.info { "Bot open!" }
            return true
        }

        logger.info { "Bot is not open. Try to find it..." }
        telegramView.searchButton.click()
        telegramView.searchInput.sendKeys("@hamster_kombat_bot")
        telegramView.hamsterItem.click()
        logger.info { "Bot open!" }

        return true
    }
}

fun main() {
    configureSession()
    closeTelegram()
}
