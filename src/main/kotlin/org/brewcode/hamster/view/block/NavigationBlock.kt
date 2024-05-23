package org.brewcode.hamster.view.block

import com.codeborne.selenide.Selenide.element
import io.appium.java_client.AppiumBy.ByAndroidUIAutomator

class NavigationBlock {

    val backButton = element(ByAndroidUIAutomator("new UiSelector().description(\"Go back\")"))
    val settings = element(ByAndroidUIAutomator("new UiSelector().className(\"android.widget.ImageView\").instance(1)"))
    val reload = element(ByAndroidUIAutomator("new UiSelector().text(\"Reload Page\")"))
}
