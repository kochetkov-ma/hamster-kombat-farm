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
        logger.debug { "Reloading game session (fast=$fast)..." }
        retry("Reloading")
            .maxAttempts(2)
            .onFail {
                TelegramAction.closeTelegram()
                TelegramAction.openTelegram()
                if (TelegramAction.openHamsterBot())
                    loadTheGameFromBotChat()
            }
            .action { if (fast) reloadBySettings() else reloadByPlay() }
            .evaluate()
        logger.info { "Reload successfully (fast=$fast)!" }
    }

    fun loadTheGameFromBotChat() {
        TelegramView.playOneCLickButton.click()
        waitLoading()
    }

    fun reloadBySettings() {
        MainView.navigation.settings.click()
        MainView.navigation.reload.click()
        waitLoading()
    }

    fun reloadByPlay() {
        TelegramView.playOneCLickButton.click()
        MainView.hamsterButton.should(Condition.disappear)
        loadTheGameFromBotChat()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private fun waitLoading() {
        val hm = MainView
        logger.trace { "Lading..." }
        Thread.sleep(2_500)

        hm.startBlock.roadmap.should(Condition.disappear, 60.seconds.toJavaDuration())
        hm.hamsterButton.should(Condition.appear)

        hm.startBlock.thanksButton.click()
        logger.info { "Thanks took!" }
    }
}
