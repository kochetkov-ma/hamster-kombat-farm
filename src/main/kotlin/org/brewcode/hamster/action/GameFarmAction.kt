package org.brewcode.hamster.action

import com.codeborne.selenide.Condition
import com.codeborne.selenide.Condition.text
import io.github.oshai.kotlinlogging.KotlinLogging
import org.brewcode.hamster.action.GameBoostAction.boostStamina
import org.brewcode.hamster.action.GameEarnAction.tryDailyEarn
import org.brewcode.hamster.action.GameLaunchAction.fastReload
import org.brewcode.hamster.staminaCheckPeriod
import org.brewcode.hamster.staminaMinimumLevel
import org.brewcode.hamster.staminaWaitInterval
import org.brewcode.hamster.view.HamsterKombatGameView
import kotlin.time.toJavaDuration


object GameFarmAction {

    private val logger = KotlinLogging.logger {}

    fun farm(statistic: ExecutionStatistic): ExecutionStatistic {
        val hm = HamsterKombatGameView()
        var currentStatic = statistic

        while (currentStatic.updateTime().elapsedMs < statistic.duration.inWholeMilliseconds) {
            currentStatic = currentStatic.updateIterations()
            val clicks = 5
            repeat(clicks) { hm.hamsterButton.clickLikeHuman() }
            currentStatic = currentStatic.updateTime().updateClicks(clicks)

            if (currentStatic.iterations % staminaCheckPeriod == 0) {
                currentStatic.println()

                val stamina = hm.staminaLevel()
                logger.info { "Check Stamina: $stamina" }

                if (stamina.first < staminaMinimumLevel) {
                    fastReload() // reload()

                    if (hm.staminaLevel().first < staminaMinimumLevel + 500) {
                        logger.info { "Try use boost..." }
                        boostStamina()

                        runCatching { hm.hamsterButton.shouldBe(Condition.visible) }
                            .onFailure { fastReload() }

                        if (hm.staminaLevel().first < staminaMinimumLevel + 500) {
                            val max = hm.staminaLevel().second
                            logger.info { "Try wait till entire refresh..." }

                            tryDailyEarn()

                            runCatching {
                                hm.staminaText.shouldBe(text("$max / $max"), staminaWaitInterval.toJavaDuration())
                            }.onFailure { logger.info { "Stamina is " + hm.staminaLevel() + " after long wait " + staminaWaitInterval } }
                        }
                    }
                }
            }
        }

        return currentStatic
    }
}
