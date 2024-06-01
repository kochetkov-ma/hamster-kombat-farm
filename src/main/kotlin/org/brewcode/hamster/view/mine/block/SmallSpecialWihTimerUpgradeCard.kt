package org.brewcode.hamster.view.mine.block

import com.codeborne.selenide.appium.SelenideAppiumElement
import org.brewcode.hamster.service.Upgrade
import org.brewcode.hamster.service.UpgradeSection
import org.brewcode.hamster.util.*
import org.openqa.selenium.By.xpath

open class SmallSpecialWihTimerUpgradeCard(
    section: UpgradeSection,
    selfXpath: String
) : SmallSpecialUpgradeCard(section, selfXpath) {
    val timer = element(selfXpath.xChild("android.view.View").xChild("android.widget.TextView").xBy())
    override val name = element(selfXpath.xChild("android.widget.TextView[1]").xBy())
    override val countdown: SelenideAppiumElement = element(selfXpath.xChild("android.view.View[2]").xChild("android.widget.TextView").xBy())
    override val profit = element(xpath(selfXpath.xChild("android.view.View[3]").xAnyChild("android.widget.TextView").xIndex(2)))
    override val level = element(xpath(selfXpath.xChild("android.view.View[4]").xChild("android.widget.TextView[1]")))
    override val cost = element(xpath(selfXpath.xChild("android.view.View[4]").xChild("android.view.View").xAnyChild("android.widget.TextView[1]")))
    override val needs = element(xpath(selfXpath.xChild("android.view.View[4]").xChild("android.widget.TextView[2]")))

    override fun toUpgrade(fromPreviousLevel: Upgrade, extName: String): Upgrade {
        return super.toUpgrade(fromPreviousLevel, extName).copy(
            timer = timer.text
        )
    }

    companion object {
        fun isSpecialWihTimer(cardSelfXpath: String): Boolean =
            element(cardSelfXpath.xChild("android.view.View").xChild("android.widget.TextView").xBy()).run { isDisplayed && text.matches("\\d+:\\d+:\\d+".toRegex()) }
    }

}
