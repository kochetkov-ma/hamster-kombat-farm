package org.brewcode.hamster.action

import com.codeborne.selenide.Condition
import com.codeborne.selenide.Condition.hidden
import com.codeborne.selenide.WebDriverRunner
import io.appium.java_client.android.AndroidDriver
import io.appium.java_client.android.nativekey.AndroidKey
import io.appium.java_client.android.nativekey.KeyEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.brewcode.hamster.view.HamsterKombatGameView
import org.brewcode.hamster.view.TelegramAppView

object OpenHamsterBot {
    private val logger = KotlinLogging.logger {}

    fun closeTelegram() {
        val telegramView = TelegramAppView()
        var i = 0
        while (telegramView.goBack.has(Condition.visible) && i < 5) {
            telegramView.goBack.click()
            i++
        }

        (WebDriverRunner.getWebDriver() as AndroidDriver).pressKey(KeyEvent(AndroidKey.HOME))
    }

    fun openTelegram() {
        val telegramView = TelegramAppView()

        if (telegramView.telegramApp.has(Condition.visible)) {
            logger.info { "Telegram opening." }
            telegramView.telegramApp.click()
        }
    }

    fun openHamsterBot(): Boolean {
        val telegramView = TelegramAppView()
        val hamsterView = HamsterKombatGameView()

        if (hamsterView.hamsterButton.has(hidden)) {
            logger.info { "Bot is not open. Try to find it..." }
            telegramView.searchButton.click()
            telegramView.searchInput.sendKeys("@hamster_kombat_bot")
            telegramView.hamsterItem.click()
            logger.info { "Bot open." }
            return true
        }

        logger.info { "Bot already open!" }
        return false
    }
}
