package org.brewcode.hamster.action

import com.codeborne.selenide.ClickOptions
import com.codeborne.selenide.SelenideElement
import io.github.oshai.kotlinlogging.KotlinLogging
import org.brewcode.hamster.Cfg
import org.brewcode.hamster.util.money
import org.brewcode.hamster.util.rnd
import org.brewcode.hamster.view.main.MainView
import java.time.LocalDateTime
import java.time.LocalDateTime.MAX
import java.time.LocalDateTime.now
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

private val logger = KotlinLogging.logger {}

typealias JDuration = java.time.Duration

fun SelenideElement.clickLikeHuman(left: Number = -50, right: Number = 50L, fast: Boolean = true) {
    if (fast) click() else click(ClickOptions.withOffset(0.rnd(left, right), 0.rnd(left, right)))
}

data class ExecutionStatistic(
    val duration: Duration,
    val start: LocalDateTime = now,
    val end: LocalDateTime = MAX,
    val startCoins: Int = MainView.coinsAmount(),
    val startProfit: Int = MainView.profit.text.money(),
    var iterations: Int = 0,
    var clicks: Int = 0
) {

    val elapsedMs: Long get() = JDuration.between(start, now).toMillis()
    val remainingMs get() = Cfg.timeout.inWholeMilliseconds - elapsedMs
    val coinsAmount get() = MainView.coinsAmount()
    val coinsEarned get() = coinsAmount - startCoins
    val profitText get() = MainView.profit.text
    val profit get() = profitText.money()
    val profitEarned get() = profit - startProfit
    val profitIncrementPer10Minutes get() = profitEarned / (elapsedMs.toDouble() / 10.minutes.inWholeMilliseconds)
    val coinsPerSecond get() = coinsEarned / ((elapsedMs + 500) / 1000)

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    fun updateClicks(newClicks: Int = 1) {
        clicks += newClicks
    }

    fun updateIterations() = iterations++
    fun printStatistic() =
        println(
            " > > > execution: ${elapsedMs.sec} / ${remainingMs.sec} sec | profit:$profitText | earned profit:$profitEarned | profit increment per 10min:$profitIncrementPer10Minutes" +
                "| coins:$coinsAmount | earned coins:$coinsEarned | coins per sec:$coinsPerSecond | clicks:$clicks | iterations:$iterations"
        )

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    companion object {
        private val Long.sec get() = this / 1000
        private val now get() = now()
    }
}
