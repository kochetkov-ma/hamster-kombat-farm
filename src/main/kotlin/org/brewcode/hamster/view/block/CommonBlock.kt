package org.brewcode.hamster.view.block

import com.codeborne.selenide.Selenide.element
import io.appium.java_client.AppiumBy.ByAndroidUIAutomator

class CommonBlock {

    val levelUpProcessing = element(ByAndroidUIAutomator("new UiSelector().textContains(\"Level up processing\")"))
    val applyButton = element(ByAndroidUIAutomator("new UiSelector().text(\"Go ahead\")")) // Получить
}
