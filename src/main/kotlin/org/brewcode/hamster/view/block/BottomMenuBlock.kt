package org.brewcode.hamster.view.block

import com.codeborne.selenide.Selenide
import io.appium.java_client.AppiumBy.ByAndroidUIAutomator

object BottomMenuBlock {

    val exchange = Selenide.element(ByAndroidUIAutomator("new UiSelector().description(\"Exchange\")"))
    val mine = Selenide.element(ByAndroidUIAutomator("new UiSelector().description(\"Mine\")"))
    val friends = Selenide.element(ByAndroidUIAutomator("new UiSelector().description(\"Friends\")"))
    val earn = Selenide.element(ByAndroidUIAutomator("new UiSelector().description(\"Earn\")"))
    val airdrop = Selenide.element(ByAndroidUIAutomator("new UiSelector().description(\"Airdrop\")"))
}
