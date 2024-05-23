package org.brewcode.hamster.action

import com.codeborne.selenide.ClickOptions
import com.codeborne.selenide.SelenideElement
import io.github.oshai.kotlinlogging.KotlinLogging
import org.brewcode.hamster.timeout
import org.brewcode.hamster.util.rnd
import java.time.LocalDateTime
import java.time.LocalDateTime.now
import kotlin.time.Duration

val logger = KotlinLogging.logger {}

typealias JDuration = java.time.Duration

fun SelenideElement.clickLikeHuman(left: Number = -50, right: Number = 50L, fast: Boolean = true) {
    if (fast) click() else click(ClickOptions.withOffset(0.rnd(left, right), 0.rnd(left, right)))
}

data class ExecutionStatistic(
    val duration: Duration,
    val start: LocalDateTime = now(),
    val elapsedMs: Long = 0,
    val remainingMs: Long = 0,
    val iterations: Int = 0,
    val end: LocalDateTime = LocalDateTime.MAX,
    val clicks: Int = 0
) {

    fun updateClicks(newClicks: Int = 1) = copy(clicks = clicks + newClicks)

    fun updateTime() = copy(
        elapsedMs = JDuration.between(start, now()).toMillis(),
        remainingMs = duration.inWholeMilliseconds - elapsedMs
    )

    fun updateIterations() = copy(iterations = iterations + 1)

    fun println() =
        logger.info { "Elapsed time: ${elapsedMs / 1000} sec | Remaining time: ${(timeout.inWholeMilliseconds - elapsedMs) / 1000} sec" }
}
