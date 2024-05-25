package org.brewcode.hamster.util

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.lang.Thread.sleep
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean

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
    else -> double()
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
        println("Finished in ${tenMs / 1000} sec")
    }

    return continueAction
}
