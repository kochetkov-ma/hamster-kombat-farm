package org.brewcode.hamster.util

import com.codeborne.selenide.WebDriverRunner
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.appium.java_client.android.AndroidDriver
import io.github.oshai.kotlinlogging.KotlinLogging
import java.lang.Thread.sleep
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

private val logger = KotlinLogging.logger {}

fun Int.rnd(left: Number = -20, right: Number = 20) = this + (left.toInt()..right.toInt()).random()
fun Long.rnd(left: Number = -20, right: Number = 20) = this + (left.toInt()..right.toInt()).random()

val mapper = ObjectMapper()
    .registerKotlinModule()
    .apply {
        findAndRegisterModules()
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        enable(SerializationFeature.INDENT_OUTPUT)
    }

fun Any?.toJson(): String = mapper.writeValueAsString(this)

inline fun <reified T> String.fromJson(): T = mapper.readValue(this)

fun String.int() = filter(Char::isDigit).ifEmpty { "0" }.toInt()
fun String.double() = filter { it.isDigit() || it == '.' || it == ',' }.replace(",", ".").toDouble()

fun String.money() = when (last().uppercaseChar()) {
    'K' -> double() * 1000
    'M' -> double() * 1000000
    'B' -> double() * 1000000000
    else -> replace(",", "").toDouble()
}.toInt()

fun Throwable.rootCause(): Throwable {
    var cause = this
    while (cause.cause != null) cause = cause.cause!!
    return cause
}

fun progress(prefix: String = "Progress", interval: Long = 1_000): AtomicBoolean {
    val continueAction = AtomicBoolean(true)

    CompletableFuture.runAsync {
        var tenMs = 0L

        while (continueAction.get()) {
            print("\r$prefix: ${tenMs / 1000} sec")
            sleep(1_000)
            tenMs += interval
        }
        println()
        logger.info { "Waiting finished! in ${tenMs / 1000} sec" }
    }

    return continueAction
}

val Int.sec get() = this.seconds.toJavaDuration()

val yaml = ObjectMapper(YAMLFactory()).registerKotlinModule()

inline fun <reified T> Path.fromYaml(): T = yaml.readValue(toFile())

val android get() = (WebDriverRunner.getWebDriver() as AndroidDriver)
