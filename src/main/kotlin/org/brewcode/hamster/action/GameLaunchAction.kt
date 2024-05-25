package org.brewcode.hamster.action

import com.codeborne.selenide.Condition
import io.github.oshai.kotlinlogging.KotlinLogging
import org.brewcode.hamster.view.main.MainView
import org.brewcode.hamster.view.tg.TelegramView
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

object GameLaunchAction {
    private val logger = KotlinLogging.logger {}

    fun fastReload() {
        MainView.navigation.settings.click()
        MainView.navigation.reload.click()
        waitLoading()
    }

    fun waitLoading() {
        val hm = MainView
        logger.info { "Lading..." }
        runCatching {
            hm.startBlock.roadmap.should(Condition.appear)
            hm.hamsterButton.should(Condition.appear, 30.seconds.toJavaDuration())
        }
        logger.info { "Loaded successfully" }
        runCatching {
            hm.startBlock.thanksButton.click()
            logger.info { "Thanks took!" }
        }
    }

    fun load() {
        val tg = TelegramView

        tg.playButton.click()
        waitLoading()
    }

    fun reload() {
        logger.info { "Try to reload game session..." }

        val tg = TelegramView
        val hm = MainView

        tg.playButton.click()
        hm.hamsterButton.should(Condition.disappear)

        load()
    }
}
