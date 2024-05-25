package org.brewcode.hamster.action

import com.codeborne.selenide.Condition
import io.github.oshai.kotlinlogging.KotlinLogging
import org.brewcode.hamster.util.Retryer.Companion.retry
import org.brewcode.hamster.view.main.MainView
import org.brewcode.hamster.view.tg.TelegramView
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

object GameLaunchAction {

    private val logger = KotlinLogging.logger {}

    fun reload(fast: Boolean = true) {
        logger.info { "Reloading game session (fast=$fast)..." }
        retry("Reloading")
            .maxAttempts(2)
            .onFail {
                TelegramAction.closeTelegram()
                TelegramAction.openHamsterBot()
                loadTheGameFromBotChat()
            }
            .action { if (fast) reloadBySettings() else reloadByPlay() }
            .evaluate()
        logger.info { "Reload successfully (fast=$fast)!" }
    }

    fun loadTheGameFromBotChat() {
        TelegramView.playButton.click()
        waitLoading()
    }

    fun reloadBySettings() {
        MainView.navigation.settings.click()
        MainView.navigation.reload.click()
        waitLoading()
    }

    fun reloadByPlay() {
        TelegramView.playButton.click()
        MainView.hamsterButton.should(Condition.disappear)
        loadTheGameFromBotChat()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private fun waitLoading() {
        val hm = MainView
        logger.info { "Lading..." }

        hm.startBlock.roadmap.should(Condition.appear)
        hm.hamsterButton.should(Condition.appear, 60.seconds.toJavaDuration())
        logger.info { "Loaded successfully" }

        hm.startBlock.thanksButton.click()
        logger.info { "Thanks took!" }
    }
}
