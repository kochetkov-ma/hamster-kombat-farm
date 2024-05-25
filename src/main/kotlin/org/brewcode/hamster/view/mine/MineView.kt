package org.brewcode.hamster.view.mine

import com.codeborne.selenide.CollectionCondition
import com.codeborne.selenide.Condition
import io.github.oshai.kotlinlogging.KotlinLogging
import org.brewcode.hamster.service.Upgrade
import org.brewcode.hamster.service.UpgradeSection
import org.brewcode.hamster.util.*
import org.brewcode.hamster.view.base.GameView
import org.brewcode.hamster.view.mine.block.AdditionalMenuBlock
import org.brewcode.hamster.view.mine.block.SmallUpgradeCard
import org.brewcode.hamster.view.mine.block.TopMenuBlock
import org.brewcode.hamster.view.mine.block.UpgradeCardBlock

val logger = KotlinLogging.logger {}

object MineView : GameView() {

    object X {
        val cardsSectionXpath = TopMenuBlock.X.menuSectionXpath.xSibling(1)
        val cardsXpath = cardsSectionXpath.xChild("android.view.View")
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    val topMenuBlock = TopMenuBlock
    val additionalMenuBlock = AdditionalMenuBlock
    val cards = elements(X.cardsXpath.xBy())

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    fun upgradeCardBlock(upgrade: Upgrade): UpgradeCardBlock = UpgradeCardBlock(upgrade.name, upgrade)

    fun readSmallCards(section: UpgradeSection, searchName: String = "", exclude: Collection<String> = emptyList()): Map<String, Upgrade> {
        val tmp = cards.shouldHave(CollectionCondition.sizeGreaterThan(0))

        return (1..tmp.size())
            .map { i -> SmallUpgradeCard(section, X.cardsXpath.xIndex(i)) }
            .filter { it.level.isDisplayed }
            .associateBy { it.name.text.also { logger.info { "Cards saved: $it" } } }
            .filterKeys { it.contains(searchName) && it !in exclude }
            .map { it.key to it.value.toUpgrade(extName = it.key) }
            .toMap()
    }

    fun scrollToLastVisibleCard() {
        val visibleCards = cards.shouldHave(CollectionCondition.sizeGreaterThan(0))
        val lastVisible = visibleCards.filter(Condition.visible).last()
        val lastVisibleIndex = visibleCards.indexOf(lastVisible)

        lastVisible.scrollTo(TopMenuBlock.self)
        logger.info { "Last visible has index $lastVisibleIndex of ${visibleCards.size()}. Scroll successful!" }
    }

    fun findSmallCard(upgrade: Upgrade): SmallUpgradeCard? {
        val tmp = cards.shouldHave(CollectionCondition.sizeGreaterThan(0))

        return (1..tmp.size())
            .map { i -> SmallUpgradeCard(upgrade.section, X.cardsXpath.xIndex(i)) }
            .filter { it.name.isDisplayed && it.level.isDisplayed }
            .find { it.name.text == upgrade.name }
            ?.also { logger.info { "Card found: ${upgrade.name}" } }
    }
}
