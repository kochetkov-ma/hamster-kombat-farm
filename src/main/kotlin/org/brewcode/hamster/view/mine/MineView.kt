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

    val topMenuBlock = TopMenuBlock
    val additionalMenuBlock = AdditionalMenuBlock
    val cards = elements(cardsXpath.xBy())
    val specialCards = elements(specialCardsXpath.xBy())

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    fun upgradeCardBlock(upgrade: Upgrade): UpgradeCardBlock = UpgradeCardBlock(upgrade.name, upgrade)

    fun readSmallCards(section: UpgradeSection, searchName: String = "", exclude: Collection<String> = emptyList()): Map<String, Upgrade> {
        val tmp =
            if (section.isSpecial) specialCards.shouldHave(CollectionCondition.sizeGreaterThan(0))
            else cards.shouldHave(CollectionCondition.sizeGreaterThan(0))

        return (1..tmp.size())
            .map { i ->
                if (section.isSpecial)
                    if (SmallSpecialWihTimerUpgradeCard.isSpecialWihTimer(specialCardsXpath.xIndex(i)))
                        SmallSpecialWihTimerUpgradeCard(section, specialCardsXpath.xIndex(i))
                    else
                        SmallSpecialUpgradeCard(section, specialCardsXpath.xIndex(i))
                else SmallUpgradeCard(section, cardsXpath.xIndex(i))
            }
            .filter { it.level.isDisplayed }
            .associateBy { it.name.text.also { logger.info { "Upgrade card saved: $it" } } }
            .filterKeys { it.contains(searchName) && it !in exclude }
            .map { it.key to it.value.toUpgrade(extName = it.key) }
            .toMap()
    }

    fun scrollToLastVisibleCard(section: UpgradeSection) {
        val visibleCards =
            if (section.isSpecial) specialCards.shouldHave(CollectionCondition.sizeGreaterThan(0))
            else cards.shouldHave(CollectionCondition.sizeGreaterThan(0))

        val lastVisible = visibleCards.filter(visible).last()
        val lastVisibleIndex = visibleCards.indexOf(lastVisible)

        lastVisible.scrollTo(TopMenuBlock.self, lastVisible.size.height)
        logger.debug { "Last visible has index $lastVisibleIndex of ${visibleCards.size()}. Scroll successful!" }
    }

    fun findSmallCard(upgrade: Upgrade, dryRun: Boolean = false): SmallUpgradeCard? {
        val tmp =
            if (upgrade.section in arrayOf(UpgradeSection.SpecialsMy, UpgradeSection.SpecialsNew)) specialCards.shouldHave(CollectionCondition.sizeGreaterThan(0))
            else cards.shouldHave(CollectionCondition.sizeGreaterThan(0))

        return (1..tmp.size())
            .map { i ->
                if (upgrade.section.isSpecial)
                    if (SmallSpecialWihTimerUpgradeCard.isSpecialWihTimer(specialCardsXpath.xIndex(i)))
                        SmallSpecialWihTimerUpgradeCard(upgrade.section, specialCardsXpath.xIndex(i))
                    else
                        SmallSpecialUpgradeCard(upgrade.section, specialCardsXpath.xIndex(i))
                else SmallUpgradeCard(upgrade.section, cardsXpath.xIndex(i))
            }
            .filter { it.name.isDisplayed && it.level.isDisplayed }
            .onEach { if (dryRun) logger.info { "Check visible card: " + it.name.text } }
            .find { it.name.text == upgrade.name }
            ?.also { logger.info { "Card found: ${upgrade.name}" } }
    }
}

fun main() {
    configureSession()
    findSmallCard(Upgrade.none.copy(section = UpgradeSection.Markets, name = "Meme coins"))
}
