package org.brewcode.hamster.view.mine

import com.codeborne.selenide.CollectionCondition
import com.codeborne.selenide.Condition.visible
import io.github.oshai.kotlinlogging.KotlinLogging
import org.brewcode.hamster.service.Upgrade
import org.brewcode.hamster.service.UpgradeSection
import org.brewcode.hamster.service.UpgradeSection.Companion.isSpecial
import org.brewcode.hamster.util.*
import org.brewcode.hamster.view.base.GameView
import org.brewcode.hamster.view.mine.MineView.X.cardsXpath
import org.brewcode.hamster.view.mine.MineView.X.specialCardsXpath
import org.brewcode.hamster.view.mine.MineView.findSmallCard
import org.brewcode.hamster.view.mine.block.*

private val logger = KotlinLogging.logger {}

object MineView : GameView() {

    object X {
        val cardsSectionXpath = TopMenuBlock.X.menuSectionXpath.xSibling(1)
        val cardsXpath = cardsSectionXpath.xChild("android.view.View")
        val specialCardsXpath = cardsXpath.xChild("android.view.View").xChild("android.view.View")
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    val dailyComboApplyButton = element(xText("Daily combo", "android.widget.TextView").xSibling(1, "android.widget.Button").xBy())
    val topMenuBlock = TopMenuBlock
    val additionalMenuBlock = AdditionalMenuBlock
    val cards = elements(cardsXpath.xBy())
    val specialCards = elements(specialCardsXpath.xBy())
    val dailyComboCardFound = element(xText("Daily combo", "android.widget.TextView").xBy())

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    fun upgradeCardBlock(upgrade: Upgrade): UpgradeCardBlock = UpgradeCardBlock(upgrade.name, upgrade)

    fun readSmallCards(section: UpgradeSection, searchName: String = "", exclude: Collection<String> = emptyList()): Map<String, Upgrade> {
        val tmp =
            if (section.isSpecial) specialCards.shouldHave(CollectionCondition.sizeGreaterThan(0))
            else cards.shouldHave(CollectionCondition.sizeGreaterThan(0))

        return (1..tmp.size())
            .map { cardElement(section, it) }
            .filter { it.level.isDisplayed }
            .associateBy { it.name.text.also { logger.debug { "Upgrade card saved: $it" } } }
            .filterKeys { it.contains(searchName) && it !in exclude }
            .map { it.key to it.value.toUpgrade(extName = it.key) }
            .toMap()
    }

    fun scrollToLastVisibleCard(section: UpgradeSection, dryRun: Boolean = false) {
        val fastVisibleCards =
            (if (section.isSpecial) specialCards.shouldHave(CollectionCondition.sizeGreaterThan(0))
            else cards.shouldHave(CollectionCondition.sizeGreaterThan(0))).filter(visible)

        val fastLastVisible = fastVisibleCards.last()
        var fastLastVisibleIndex = fastVisibleCards.indexOf(fastVisibleCards.last())

        val slowCard = cardElement(section, fastLastVisibleIndex)
        val slowCardVisible = slowCard.name.isDisplayed && slowCard.cost.isDisplayed

        if (slowCardVisible)
            fastLastVisible.scrollTo(TopMenuBlock.markets, false)
        else {
            fastLastVisibleIndex = fastVisibleCards.size() - 3
            val prevRowVisible = fastVisibleCards[fastLastVisibleIndex]
            prevRowVisible.scrollTo(TopMenuBlock.markets, true)
        }

        if (dryRun)
            logger.trace { "Last visible has index $fastLastVisibleIndex of ${fastVisibleCards.size()}. Scroll successful!" }
    }

    fun findSmallCard(upgrade: Upgrade, dryRun: Boolean = false): SmallUpgradeCard? {
        val tmp =
            if (upgrade.section in arrayOf(UpgradeSection.SpecialsMy, UpgradeSection.SpecialsNew)) specialCards.shouldHave(CollectionCondition.sizeGreaterThan(0))
            else cards.shouldHave(CollectionCondition.sizeGreaterThan(0))

        return (1..tmp.size())
            .map { cardElement(upgrade.section, it) }
            .filter { it.name.isDisplayed && it.level.isDisplayed }
            .onEach { if (dryRun) logger.trace { "Check visible card: " + it.name.text } }
            .find { it.name.text == upgrade.name }
            ?.also { logger.debug { "Card found: ${upgrade.name}" } }
    }

    private fun cardElement(section: UpgradeSection, index: Int) =
        if (section.isSpecial)
            if (SmallSpecialWihTimerUpgradeCard.isSpecialWihTimer(specialCardsXpath.xIndex(index)))
                SmallSpecialWihTimerUpgradeCard(section, specialCardsXpath.xIndex(index))
            else
                SmallSpecialUpgradeCard(section, specialCardsXpath.xIndex(index))
        else SmallUpgradeCard(section, cardsXpath.xIndex(index))
}

fun main() {
    configureSession()
    findSmallCard(Upgrade.none.copy(section = UpgradeSection.Markets, name = "Meme coins"))
}
