package org.brewcode.hamster.action

import com.codeborne.selenide.Condition.visible
import io.appium.java_client.android.nativekey.AndroidKey
import io.appium.java_client.android.nativekey.KeyEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.brewcode.hamster.util.android
import org.brewcode.hamster.util.configureSession
import org.brewcode.hamster.view.earn.EarnView
import org.brewcode.hamster.view.main.MainView
import org.brewcode.hamster.view.mine.MineView

private val logger = KotlinLogging.logger {}

object GameCommonAction {

    fun goToBack() {
        logger.debug { "Go to BACK..." }
        android.pressKey(KeyEvent(AndroidKey.BACK))
    }

    fun goToExchange() {
        logger.debug { "Go to Exchange..." }
        MainView.bottomMenu.exchange.click()
        MainView.hamsterButton.shouldBe(visible)
    }

    fun goToMine() {
        logger.debug { "Go to Mine..." }
        MainView.bottomMenu.mine.click()
        MineView.topMenuBlock.self.shouldBe(visible)
    }

    fun goToEarn() {
        logger.debug { "Go to Earn..." }
        MainView.bottomMenu.earn.click()
        EarnView.daily.shouldBe(visible)
    }
}


fun main() {
    configureSession()

    GameCommonAction.goToExchange()
    GameCommonAction.goToMine()
    GameCommonAction.goToEarn()
    GameCommonAction.goToBack()
}
