package org.brewcode.hamster.view.earn

import com.codeborne.selenide.Condition
import com.codeborne.selenide.Selenide
import io.appium.java_client.AppiumBy.ByAndroidUIAutomator
import org.brewcode.hamster.view.base.GameView
import org.openqa.selenium.By

object EarnView: GameView() {

    val daily = Selenide.element(ByAndroidUIAutomator("new UiSelector().text(\"Daily reward\")"))
    val applyButton =
        Selenide.element(By.xpath("(//android.widget.TextView[@text='Daily reward'])[2]/following-sibling::android.widget.Button"))

    val isDailyAvailable get() =
        runCatching { applyButton.shouldBe(Condition.visible).text().contains("Claim") } // Come back tomorrow
            .getOrElse { false }
}
