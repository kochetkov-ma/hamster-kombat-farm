package org.brewcode.hamster.view.tg

import com.codeborne.selenide.Selenide.element
import io.appium.java_client.AppiumBy

object TelegramView {

    val goBack = element(AppiumBy.ByAndroidUIAutomator("new UiSelector().description(\"Go back\")"))
    val telegramApp = element(AppiumBy.ByAndroidUIAutomator("new UiSelector().description(\"Telegram\")"))
    val searchButton = element(AppiumBy.accessibilityId("Search"))
    val searchInput = element(AppiumBy.className("android.widget.EditText"))
    val hamsterItem = element(AppiumBy.ByAndroidUIAutomator("new UiSelector().text(\"Hamster Kombat, bot\")"))
    val playButton = element(AppiumBy.ByAndroidUIAutomator("new UiSelector().description(\"Bot menu\")"))
}
