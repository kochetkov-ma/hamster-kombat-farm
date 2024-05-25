package org.brewcode.hamster.view.main

import com.codeborne.selenide.Selenide.element
import io.appium.java_client.AppiumBy.ByAndroidUIAutomator

object StartBlock {

    val thanksButton = element(ByAndroidUIAutomator("new UiSelector().textContains(\"Thank\")")) // Спасибо
    val roadmap = element(ByAndroidUIAutomator("new UiSelector().textContains(\"Roadmap\")"))
}
