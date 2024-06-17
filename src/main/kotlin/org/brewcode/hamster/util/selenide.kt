package org.brewcode.hamster.util

import com.codeborne.selenide.Selenide
import com.codeborne.selenide.SelenideElement
import com.codeborne.selenide.appium.SelenideAppium
import com.codeborne.selenide.appium.SelenideAppiumCollection
import com.codeborne.selenide.appium.SelenideAppiumElement
import org.openqa.selenium.By

fun element(by: By): SelenideAppiumElement = SelenideAppium.`$`(by)
fun elements(by: By): SelenideAppiumCollection = SelenideAppium.`$$`(by)

fun SelenideElement.scrollTo(to: SelenideElement, center: Boolean = false) = Selenide.actions()
    .moveToElement(this, 0, -(to.size.height / 2))
    .clickAndHold()
    .moveToElement(to, 0, if (center) 0 else (to.size.height / 2))
    .pause(500)
    .release()
    .perform()
