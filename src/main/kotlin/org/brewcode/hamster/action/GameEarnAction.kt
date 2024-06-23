package org.brewcode.hamster.action

import com.codeborne.selenide.Condition
import io.github.oshai.kotlinlogging.KotlinLogging
import org.brewcode.hamster.action.GameCommonAction.goToEarn
import org.brewcode.hamster.view.earn.EarnView
import org.brewcode.hamster.view.main.MainView
import java.time.LocalDate
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

object GameEarnAction {
    private val logger = KotlinLogging.logger {}

    private val info = Path("daily.info").also {
        if (!it.exists()) it.writeText("")

        logger.trace { "Daily info file: $it" }
    }

    fun tryDailyEarn(): Boolean {

        val mayBeAvailable = info.exists() && !info.readText().contains(LocalDate.now().toString())

        if (!mayBeAvailable) {
            logger.debug { "Daily not available. Last award was: " + info.readText() }
            return false
        }

        val hm = MainView
        goToEarn()
        EarnView.daily.click()

        val result = if (EarnView.isDailyAvailable) {
            EarnView.applyButton.click()
            EarnView.applyButton.should(Condition.hidden)

            info.writeText(LocalDate.now().toString())
            logger.info { "Daily award got! Last award was: " + info.readText() }
            true
        } else {
            logger.info { "Daily award not available yet. Last award was: " + info.readText() }
            false
        }

        GameCommonAction.goToBack()
        GameCommonAction.goToExchange()
        return result
    }
}
