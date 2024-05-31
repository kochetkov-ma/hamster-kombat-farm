package org.brewcode.hamster.util

import com.codeborne.selenide.Selenide
import com.codeborne.selenide.SelenideElement
import com.codeborne.selenide.appium.SelenideAppium
import com.codeborne.selenide.appium.SelenideAppiumCollection
import com.codeborne.selenide.appium.SelenideAppiumElement
import org.openqa.selenium.By


fun element(by: By): SelenideAppiumElement = SelenideAppium.`$`(by)
fun elements(by: By): SelenideAppiumCollection = SelenideAppium.`$$`(by)

fun SelenideElement.scrollTo(to: SelenideElement, offset: Int = 0) = Selenide.actions().clickAndHold(this).moveToElement(to, 0, offset).pause(500).release().pause(500).perform()
