package org.brewcode.hamster.view.main

import com.codeborne.selenide.Selenide.element
import io.appium.java_client.AppiumBy.ByAndroidUIAutomator
import org.brewcode.hamster.util.*
import org.brewcode.hamster.view.base.GameView

object MainView : GameView() {

    object X {
        val boost = xDesc("Boost Boost", "android.view.View")

        val mainView = boost.xParent.xParent
        val ceoName = mainView.xChild("android.view.View").xIndex(1)
        val level = mainView.xChild("android.view.View").xIndex(2)
        val profit = mainView.xChild("android.view.View").xIndex(3)
        val daily = mainView.xChild("android.view.View").xIndex(4)
        val coins = mainView.xChild("android.view.View").xIndex(5)
        val hamster = mainView.xChild("android.view.View").xIndex(6)
    }

    val profit = element(X.profit.xChild("android.view.View").xIndex(2).xAnyChild("android.widget.TextView").xIndex(2).xBy())
    val coins = element(X.coins.xChild("android.widget.TextView").xBy())
    val hamsterButton = element(xText("Hamster Kombat", "android.widget.Button").xBy())
    val stamina = element(X.hamster.xChild("android.widget.TextView").xBy())
    val boostButton = element(X.boost.xBy())

    val availableButton = element(ByAndroidUIAutomator("new UiSelector().textContains(\"available\")"))

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    val startBlock = StartBlock

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private val staminaRegex = """(\d+) / (\d+)""".toRegex()
    private val availableRegex = """(\d+)/(\d+).+""".toRegex()

    fun staminaLevel(): Pair<Int, Int> = staminaRegex.find(stamina.text)?.destructured?.let { (current, max) -> current.toInt() to max.toInt() } ?: (0 to 0)
    fun coinsAmount() = coins.text.int()
    fun available() = availableRegex.find(availableButton.text)?.destructured?.let { (current, max) -> current.toInt() to max.toInt() } ?: (0 to 0)
}

fun main() {
    configureSession()
    println(MainView.hamsterButton.isDisplayed)
    println(MainView.stamina.text())
    println(MainView.coins.text())
    println(MainView.boostButton.isDisplayed)
    println(MainView.profit.text())
}
