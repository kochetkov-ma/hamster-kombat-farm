package org.brewcode.hamster.action

import com.codeborne.selenide.Condition
import io.github.oshai.kotlinlogging.KotlinLogging
import org.brewcode.hamster.view.HamsterKombatGameView
import java.time.LocalDate
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

object GameEarnAction {
    private val logger = KotlinLogging.logger {}

    private val info = Path("build/daily.info").also {
        if (!it.exists()) it.writeText("")

        logger.info { "Daily info file: $it" }
    }

    fun tryDailyEarn(): Boolean {

        val mayBeAvailable = info.exists() && !info.readText().contains(LocalDate.now().toString())

        if (!mayBeAvailable) {
            logger.info { "Daily not available. Last award was: " + info.readText() }
            return false
        }

        val hm = HamsterKombatGameView
        hm.bottomMenuBlock.earn.click()
        hm.earnBlock.daily.click()

        val result = if (hm.earnBlock.isDailyAvailable) {
            hm.earnBlock.applyButton.click()
            hm.earnBlock.applyButton.should(Condition.hidden)
            info.writeText(LocalDate.now().toString())
            logger.info { "Daily award got. Last award was: " + info.readText() }
            true
        } else {
            logger.info { "Daily award not available yet. Last award was: " + info.readText() }
            false
        }

        GameCommonAction.goToExchange()
        return result
    }
}
