package org.brewcode.hamster.action


import com.codeborne.selenide.Condition
import io.github.oshai.kotlinlogging.KotlinLogging
import org.brewcode.hamster.view.main.MainView
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

object GameBoostAction {
    private val logger = KotlinLogging.logger {}

    fun boostStamina(): Boolean {

        MainView.boostButton.click()
        val available = MainView.available()

        if (available.first > 0) {
            logger.info { "Great! Boost available ($available) - using." }

            MainView.availableButton.click()
            runCatching {
                runCatching { MainView.common.goAheadButton.shouldBe(Condition.visible, 4.seconds.toJavaDuration()) }
                    .onSuccess { MainView.common.goAheadButton.click() }
            }.onFailure {
                logger.error { "Boost not applied because cooldown: ${it.message}. Go back..." }
            }

            return runCatching {
                MainView.common.goAheadButton.should(Condition.hidden, 4.seconds.toJavaDuration())
                GameCommonAction.goToExchange()
                return true
            }.onFailure {
                logger.error { "Boost not applied go back due to error: ${it.message}. Go back..." }
                MainView.navigation.backButton.click()
                GameCommonAction.goToExchange()
                return false
            }.getOrElse {
                GameCommonAction.goToExchange()
                false
            }

        } else {
            logger.info { "Boost not available $available..." }
            GameCommonAction.goToExchange()
            return false
        }
    }
}
