package org.brewcode.hamster.util

import com.codeborne.selenide.Selenide
import com.codeborne.selenide.SelenideElement
import com.codeborne.selenide.appium.SelenideAppium
import com.codeborne.selenide.appium.SelenideAppiumCollection
import com.codeborne.selenide.appium.SelenideAppiumElement
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.openqa.selenium.By

fun Int.rnd(left: Number = -20, right: Number = 20) = this + (left.toInt()..right.toInt()).random()
fun Long.rnd(left: Number = -20, right: Number = 20) = this + (left.toInt()..right.toInt()).random()

fun retry(times: Int = 1, onFail: (err: Throwable) -> Unit = {}, block: () -> Unit) {
    var count = 0
    while (count <= times) {
        try {
            block()
            return
        } catch (err: Throwable) {
            logger.info { "Error on retry: ${err.localizedMessage}" }
            runCatching { onFail(err) }
                .onFailure { logger.info { "Error on repair action: ${it.localizedMessage}" } }
            count++
        }
    }

    throw Exception("Failed after $times retries")
}

val mapper = ObjectMapper()
    .registerKotlinModule()
    .apply {
        findAndRegisterModules()
        enable(SerializationFeature.INDENT_OUTPUT)
    }

fun Any?.toJson(): String = mapper.writeValueAsString(this)

inline fun <reified T> String.fromJson(): T = mapper.readValue(this)

fun String.int() = filter(Char::isDigit).toInt()
fun String.double() = filter { it.isDigit() || it == '.' || it == ',' }.replace(",", ".").toDouble()

fun String.money() = when (last().uppercaseChar()) {
    'K' -> double() * 1000
    'M' -> double() * 1000000
    'B' -> double() * 1000000000
    else -> double()
}.toInt()

val String.parent get() = "$this/.."

fun String.sibling(index: Int) = "$this/following-sibling::*[$index]"

fun String.siblingPrev(index: Int, tag: String = "*") = "$this/preceding-sibling::$tag[$index]"

fun String.child(tag: String) = "$this/$tag"

fun String.childTxt(text: String, tag: String = "*") = "$this/$tag[text()='$text']"

fun String.anyChild(tag: String) = "$this//$tag"

fun String.anyChildTxt(text: String, tag: String = "*") = "$this//$tag[text()='$text']"

val String.xpathTxt get() = ".//*[@text='$this']"

val String.xpathDesc get() = ".//*[@content-desc='$this']"

fun String.index(i: Int) = "($this)[$i]"

fun String.attr(attr: String, value: String) = "$this[@$attr='$value']"

fun element(by: By): SelenideAppiumElement = SelenideAppium.`$`(by)
fun elements(by: By): SelenideAppiumCollection = SelenideAppium.`$$`(by)

val String.x get() = By.xpath(this)

fun SelenideElement.scrollTo(to: SelenideElement) = Selenide.actions().clickAndHold(this).moveToElement(to).pause(500).release().pause(500).perform()
