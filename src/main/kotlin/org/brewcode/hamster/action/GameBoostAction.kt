package org.brewcode.hamster.action


import com.codeborne.selenide.Condition
import io.github.oshai.kotlinlogging.KotlinLogging
import org.brewcode.hamster.availableBoostLevel
import org.brewcode.hamster.view.HamsterKombatGameView
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

object GameBoostAction {
    private val logger = KotlinLogging.logger {}

    fun boostStamina(): Boolean {

        val hm = HamsterKombatGameView
        hm.boostButton.click()
        val available = hm.available()

        if (available.first > availableBoostLevel) {
            logger.info { "Great! Boost available ($available) - using." }

            hm.availableButton.click()
            runCatching {
                runCatching { hm.commonBlock.applyButton.shouldBe(Condition.visible, 4.seconds.toJavaDuration()) }
                    .onSuccess { hm.commonBlock.applyButton.click() }
            }.onFailure {
                it.printStackTrace()
                logger.info { "Boost not applied because cooldown: ${it.localizedMessage}. Go back..." }
            }

            return runCatching {
                hm.commonBlock.applyButton.should(Condition.hidden, 4.seconds.toJavaDuration())
                GameCommonAction.goToExchange()
                return true
            }.onFailure {
                logger.info { "Boost not applied go back due to error: ${it.localizedMessage}. Go back..." }
                hm.navigationBlock.backButton.click()
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
