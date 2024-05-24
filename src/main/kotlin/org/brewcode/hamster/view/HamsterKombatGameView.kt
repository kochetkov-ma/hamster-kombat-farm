package org.brewcode.hamster.view

import com.codeborne.selenide.Selenide.element
import io.appium.java_client.AppiumBy.ByAndroidUIAutomator
import io.appium.java_client.AppiumBy.xpath
import org.brewcode.hamster.util.*
import org.brewcode.hamster.view.block.*

object HamsterKombatGameView {

    private val boostXpath = "Boost Boost".xpathDesc
    val app = element(xpath(".//android.webkit.WebView[@text=\"Hamster Kombat\"]"))
    val hamsterButton = element(app).find(boostXpath.parent.child("android.widget.Button").x)
    val staminaText = element(app).find(boostXpath.siblingPrev(1, "android.widget.TextView").x)
    val amountText = element(app).find(xpath("(.//android.widget.ListView/following-sibling::android.view.View/android.widget.Image/following-sibling::android.widget.TextView)[1]"))
    val boostButton = element(boostXpath.x)
    val availableButton = element(ByAndroidUIAutomator("new UiSelector().textContains(\"available\")")) // доступно

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    val startBlock = StartBlock
    val commonBlock = CommonBlock
    val navigationBlock = NavigationBlock
    val bottomMenuBlock = BottomMenuBlock
    val earnBlock = EarnBlock

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private val staminaRegex = """(\d+) / (\d+)""".toRegex()
    private val availableRegex = """(\d+)/(\d+).+""".toRegex()

    fun staminaLevel(): Pair<Int, Int> = staminaRegex.find(staminaText.text)?.destructured?.let { (current, max) -> current.toInt() to max.toInt() } ?: (0 to 0)
    fun coinsAmount() = amountText.text.int()
    fun available() = availableRegex.find(availableButton.text)?.destructured?.let { (current, max) -> current.toInt() to max.toInt() } ?: (0 to 0)
}
