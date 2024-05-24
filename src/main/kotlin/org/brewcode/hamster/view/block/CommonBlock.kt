package org.brewcode.hamster.view.block

import com.codeborne.selenide.Selenide.element
import io.appium.java_client.AppiumBy.ByAndroidUIAutomator
import org.brewcode.hamster.util.parent
import org.brewcode.hamster.util.x
import org.brewcode.hamster.util.xpathTxt

object CommonBlock {

    val levelUpProcessing = element(ByAndroidUIAutomator("new UiSelector().textContains(\"Level up processing\")"))
    val applyButton = element("Go ahead".xpathTxt.x) // Получить
    val insufficientFunds = element("Insufficient funds".xpathTxt.x) // Получить
    val applyButtonDiv = element("Go ahead".xpathTxt.parent.parent.x)
}
