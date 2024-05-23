package org.brewcode.hamster.view

import com.codeborne.selenide.Selenide.element
import io.appium.java_client.AppiumBy.ByAndroidUIAutomator
import io.appium.java_client.AppiumBy.xpath
import org.brewcode.hamster.view.block.*

class HamsterKombatGameView {

    val app = element(xpath(".//android.webkit.WebView[@text=\"Hamster Kombat\"]"))

    val hamsterButton =
        element(app).find(xpath(".//android.view.View[contains(@content-desc, 'Level')]/following-sibling::android.view.View/android.widget.Button"))
    val staminaText =
        element(app).find(xpath(".//android.view.View[contains(@content-desc, 'Level')]/following-sibling::android.view.View/android.widget.TextView"))
    val amountText =
        element(app).find(xpath("(.//android.widget.ListView/following-sibling::android.view.View/android.widget.Image/following-sibling::android.widget.TextView)[1]"))

    val boostButton = element(ByAndroidUIAutomator("new UiSelector().text(\"Boost\")"))
    val availableButton = element(ByAndroidUIAutomator("new UiSelector().textContains(\"available\")")) // доступно

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    val startBlock = StartBlock()
    val commonBlock = CommonBlock()
    val navigationBlock = NavigationBlock()
    val bottomMenuBlock = BottomMenuBlock()
    val earnBlock = EarnBlock()

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private val staminaRegex = """(\d+) / (\d+)""".toRegex()
    fun staminaLevel(): Pair<Int, Int> =
        staminaRegex.find(staminaText.text)?.destructured?.let { (current, max) -> current.toInt() to max.toInt() }
            ?: (0 to 0)

    fun amount() = amountText.text.filter(Char::isDigit).toInt()

    private val availableRegex = """(\d+)/(\d+).+""".toRegex()
    fun available() =
        availableRegex.find(availableButton.text)?.destructured?.let { (current, max) -> current.toInt() to max.toInt() }
            ?: (0 to 0)
}
