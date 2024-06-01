package org.brewcode.hamster.view.mine.block

import com.codeborne.selenide.Condition.clickable
import org.brewcode.hamster.service.Upgrade
import org.brewcode.hamster.service.UpgradeSection
import org.brewcode.hamster.util.*
import org.openqa.selenium.By.xpath

open class SmallUpgradeCard(
    private val section: UpgradeSection,
    selfXpath: String
) {
    val self = element(xpath(selfXpath))
    val image = element(xpath(selfXpath.xChild("android.view.View[1]")))
    open val countdown = element(xpath(selfXpath.xChild("android.view.View[1]").xChild("android.widget.TextView")))
    open val name = element(xpath(selfXpath.xChild("android.widget.TextView[1]")))
    open val profit = element(xpath(selfXpath.xChild("android.view.View[2]").xAnyChild("android.widget.TextView")))
    open val level = element(xpath(selfXpath.xChild("android.widget.TextView[3]")))
    open val cost = element(xpath(selfXpath.xChild("android.view.View[3]").xAnyChild("android.widget.TextView")))
    open val needs = element(xpath(selfXpath.xChild("android.widget.TextView[4]")))

    fun openCard() {
        if (name.has(clickable) && cost.has(clickable)) name.click()
        else throw IllegalStateException("Cannot open card. Card overlapped by other element. Name or Cost are not clickable: $this")
    }

    open fun toUpgrade(fromPreviousLevel: Upgrade = Upgrade.none, extName: String = ""): Upgrade = fromPreviousLevel
        .copy(
            section = section,
            name = extName.ifBlank { name.text },
            level = if (level.isDisplayed) level.text.int() else 0,
            totalProfit = if (profit.isDisplayed) profit.text.money() else 0,
            cost = if (cost.isDisplayed) cost.text.money() else Int.MAX_VALUE,
            needText = if (needs.isDisplayed) needs.text else "",
            isUnlocked = self.has(clickable)
        )
        .let { it.copy(relativeProfit = if (fromPreviousLevel.totalProfit > 0) it.totalProfit - fromPreviousLevel.totalProfit else 0) }
}
