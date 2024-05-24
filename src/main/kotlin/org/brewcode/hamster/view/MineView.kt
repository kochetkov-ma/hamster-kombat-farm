package org.brewcode.hamster.view

import com.codeborne.selenide.CollectionCondition
import com.codeborne.selenide.Condition
import io.appium.java_client.AppiumBy.ByAndroidUIAutomator
import org.brewcode.hamster.service.Upgrade
import org.brewcode.hamster.service.UpgradeSection
import org.brewcode.hamster.util.*
import org.openqa.selenium.By.xpath

object MineView {

    object X {
        val cardsSectionXpath = TopMenuBlock.X.menuSectionXpath.sibling(1)
        val cardsXpath = cardsSectionXpath.child("android.view.View")
    }

    val topMenuBlock = TopMenuBlock
    val additionalMenuBlock = AdditionalMenuBlock
    val cards = elements(X.cardsXpath.x)

    fun upgradeCardBlock(upgrade: Upgrade): UpgradeCardBlock = UpgradeCardBlock(upgrade.name, upgrade)

    fun readSmallCards(searchName: String = "", exclude: Collection<String> = emptyList()): Map<String, Upgrade> {
        val tmp = cards.shouldHave(CollectionCondition.sizeGreaterThan(0))

        val res = (1..tmp.size())
            .map { i -> SmallUpgradeCard(UpgradeSection.Markets, X.cardsXpath.index(i)) }
            .filter { it.level.isDisplayed }
            .associateBy { it.name.text }
            .filterKeys { it.contains(searchName) && it !in exclude }
            .map { it.key to it.value.toUpgrade(extName = it.key) }
            .toMap()

        tmp[res.size - 1].scrollTo(topMenuBlock.self)
        return res
    }

    fun findSmallCard(searchName: String): SmallUpgradeCard? {
        val tmp = cards.shouldHave(CollectionCondition.sizeGreaterThan(0))

        val res = (1..tmp.size())
            .map { i -> SmallUpgradeCard(UpgradeSection.Markets, X.cardsXpath.index(i)) }
            .filter { it.name.isDisplayed }
            .find { it.name.text == searchName }

        if (res == null)
            tmp[tmp.size() - 1].scrollTo(topMenuBlock.self)

        return res
    }
}

object TopMenuBlock {

    object X {
        val marketsX = "Markets".xpathTxt
        val menuSectionXpath = "Markets".xpathTxt.parent
    }

    val markets = element(X.marketsX.x)
    val prTeam = element(xpath("//*[@text='PR&Team']"))
    val legal = element(xpath("//*[@text='Legal']"))
    val specials = element(xpath("//*[@text='Specials']"))
    val self = element(X.menuSectionXpath.x)
}

object AdditionalMenuBlock {
    val myCards = element(xpath("//*[@text='My cards']"))
    val newCards = element(xpath("//*[@text='New cards']"))
    val missedCards = element(xpath("//*[@text='Missed opportunities']"))
}

data class UpgradeCardBlock(
    val name: String,
    private val upgrade: Upgrade
) {
    val selfXpath = "//*[@text='$name']/.."
    val relativeProfit = element(xpath(selfXpath.childTxt("Profit per hour").sibling(1).child("android.widget.TextView")))
    val applyButton = element(ByAndroidUIAutomator("new UiSelector().text(\"Go ahead\")"))

    fun toFullUpgrade() = upgrade.copy(
        relativeProfit = relativeProfit.text.money()
    )
}

data class SmallUpgradeCard(
    val section: UpgradeSection,
    val selfXpath: String
) {
    val self = element(xpath(selfXpath))
    val image = element(xpath(selfXpath.child("android.view.View[1]")))
    val name = element(xpath(selfXpath.child("android.widget.TextView[1]")))
    val profit = element(xpath(selfXpath.child("android.view.View[2]").anyChild("android.widget.TextView")))
    val level = element(xpath(selfXpath.child("android.widget.TextView[3]")))
    val cost = element(xpath(selfXpath.child("android.view.View[3]").anyChild("android.widget.TextView")))
    val needs = element(xpath(selfXpath.child("android.widget.TextView[4]")))

    fun openCard() {
        self.click()
    }

    fun toUpgrade(from: Upgrade = Upgrade.empty, extName: String = "") =
        from.copy(
            section = section,
            name = extName.ifBlank { name.text },
            level = level.text.int(),
            totalProfit = profit.text.money(),
            cost = if (cost.isDisplayed) cost.text.money() else 0,
            needText = if (needs.isDisplayed) needs.text else "",
            isUnlocked = self.has(Condition.clickable)
        )
}
