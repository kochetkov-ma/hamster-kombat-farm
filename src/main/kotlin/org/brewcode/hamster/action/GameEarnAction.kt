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
            logger.info { "Daily not available: " + info.readText() }
            return false
        }

        val hm = HamsterKombatGameView()
        hm.bottomMenuBlock.earn.click()
        hm.earnBlock.daily.click()

        val result = if (hm.earnBlock.isDailyAvailable) {
            hm.earnBlock.daily.click()
            info.writeText(LocalDate.now().toString())
            hm.earnBlock.applyButton.should(Condition.hidden)
            logger.info { "Daily award got: " + info.readText() }
            true
        } else {
            hm.navigationBlock.backButton.click()
            hm.earnBlock.applyButton.should(Condition.hidden)
            hm.navigationBlock.backButton.click()
            logger.info { "Daily award not available yet: " + info.readText() }
            false
        }

        hm.hamsterButton.shouldBe(Condition.visible)
        return result
    }
}
