package org.brewcode.hamster.view.mine.block

import com.codeborne.selenide.Condition
import org.brewcode.hamster.service.Upgrade
import org.brewcode.hamster.service.UpgradeSection
import org.brewcode.hamster.util.*
import org.openqa.selenium.By.xpath

data class SmallUpgradeCard(
    val section: UpgradeSection,
    val selfXpath: String
) {
    val self = element(xpath(selfXpath))
    val image = element(xpath(selfXpath.xChild("android.view.View[1]")))
    val name = element(xpath(selfXpath.xChild("android.widget.TextView[1]")))
    val profit = element(xpath(selfXpath.xChild("android.view.View[2]").xAnyChild("android.widget.TextView")))
    val level = element(xpath(selfXpath.xChild("android.widget.TextView[3]")))
    val cost = element(xpath(selfXpath.xChild("android.view.View[3]").xAnyChild("android.widget.TextView")))
    val needs = element(xpath(selfXpath.xChild("android.widget.TextView[4]")))

    fun openCard() {
        self.click()
    }

    fun toUpgrade(from: Upgrade = Upgrade.empty, extName: String = ""): Upgrade {
        val costD = cost.isDisplayed
        val needD = needs.isDisplayed

        return from.copy(
            section = section,
            name = extName.ifBlank { name.text },
            level = level.text.int(),
            totalProfit = profit.text.money(),
            cost = if (costD) cost.text.money() else Int.MAX_VALUE,
            needText = if (needD) needs.text else "",
            isUnlocked = self.has(Condition.clickable)
        )
    }
}
