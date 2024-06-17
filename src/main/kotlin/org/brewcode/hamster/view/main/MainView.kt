package org.brewcode.hamster.view.main

import com.codeborne.selenide.Selenide.element
import io.appium.java_client.AppiumBy.ByAndroidUIAutomator
import io.appium.java_client.AppiumBy.xpath
import org.brewcode.hamster.util.*
import org.brewcode.hamster.view.base.GameView

object MainView : GameView() {

    private val boostXpath = xDesc("Boost Boost")
    val hamsterButton = element(app).find(boostXpath.xParent.xChild("android.widget.Button").xBy())
    val staminaText = element(app).find(boostXpath.xSiblingPrev(1, "android.widget.TextView").xBy())
    val coinsText = element(app).find(xpath("(.//android.widget.ListView/following-sibling::android.view.View/android.widget.Image/following-sibling::android.widget.TextView)[1]"))
    val boostButton = element(boostXpath.xBy())
    val availableButton = element(ByAndroidUIAutomator("new UiSelector().textContains(\"available\")"))
    val profit = element(xText("Profit per hour").xSibling(1).xChild("android.widget.TextView").xBy())

    val dailyCipher = element(xText("Daily cipher", "android.widget.TextView").xBy())
    val earnPerTap = element(xText("Earn per tap").xBy())

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    val startBlock = StartBlock

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private val staminaRegex = """(\d+) / (\d+)""".toRegex()
    private val availableRegex = """(\d+)/(\d+).+""".toRegex()

    fun staminaLevel(): Pair<Int, Int> = staminaRegex.find(staminaText.text)?.destructured?.let { (current, max) -> current.toInt() to max.toInt() } ?: (0 to 0)
    fun coinsAmount() = coinsText.text.int()
    fun available() = availableRegex.find(availableButton.text)?.destructured?.let { (current, max) -> current.toInt() to max.toInt() } ?: (0 to 0)
}
