package org.brewcode.hamster.action

import com.codeborne.selenide.Condition
import org.brewcode.hamster.action.GameCommonAction.goToBack
import org.brewcode.hamster.action.GameMineAction.chooseAndBuyUpgrades
import org.brewcode.hamster.service.Upgrade
import org.brewcode.hamster.service.UpgradeSection
import org.brewcode.hamster.service.UpgradeService
import org.brewcode.hamster.service.UpgradeService.buyUpgrade
import org.brewcode.hamster.service.UpgradeService.calculateTarget
import org.brewcode.hamster.service.UpgradeService.loadUpgrades
import org.brewcode.hamster.service.UpgradeService.updateUpgrades
import org.brewcode.hamster.util.configureSession
import org.brewcode.hamster.view.main.MainView
import org.brewcode.hamster.view.main.MainView.coinsAmount
import org.brewcode.hamster.view.mine.MineView
import org.brewcode.hamster.view.mine.MineView.findSmallCard
import org.brewcode.hamster.view.mine.MineView.readSmallCards
import org.brewcode.hamster.view.mine.MineView.scrollToLastVisibleCard
import org.brewcode.hamster.view.mine.block.SmallUpgradeCard

object GameMineAction {

    fun goToSection(section: UpgradeSection) {
        when (section) {
            UpgradeSection.Markets -> MineView.topMenuBlock.markets.click()
            UpgradeSection.PrTeam -> MineView.topMenuBlock.prTeam.click()
            UpgradeSection.Legal -> MineView.topMenuBlock.legal.click()
            UpgradeSection.SpecialsMy -> {
                MineView.topMenuBlock.specials.click()
                MineView.additionalMenuBlock.myCards.click()
            }

            UpgradeSection.SpecialsNew -> {
                MineView.topMenuBlock.specials.click()
                MineView.additionalMenuBlock.newCards.click()
            }
        }
    }

    fun loadCards(section: UpgradeSection): MutableMap<String, Upgrade> {
        val map = readSmallCards(section).toMutableMap()
        var index = 0
        while (MainView.hamsterButton.has(Condition.hidden) && index < 10) {
            scrollToLastVisibleCard()
            map.putAll(readSmallCards(section, exclude = map.keys))
            index++
        }

        return map
    }

    fun buyUpgradeCard(upgrade: Upgrade): Upgrade {
        var card: SmallUpgradeCard? = findSmallCard(upgrade)
        var index = 0
        while (MainView.hamsterButton.has(Condition.hidden) && index < 10 && card == null) {
            scrollToLastVisibleCard()
            card = findSmallCard(upgrade)
            index++
        }

        if (card == null) {
            UpgradeService.remove(upgrade)
            logger.info { "Cannot find card for $upgrade" }
            return upgrade
        }

        card.openCard()
        MineView.confirm.actionButton.shouldBe(Condition.visible)

        val btnText = MineView.confirm.actionButton.text
        if (btnText != "Go ahead") {
            logger.info { "Button: $btnText! Need update data or wait: $upgrade" }
            goToBack()
            return upgrade
        }

        MineView.confirm.actionButton.click()
        MineView.confirm.actionButton.shouldBe(Condition.hidden)

        logger.info { "Bought upgrade! $upgrade" }
        return card.toUpgrade(upgrade)
    }

    fun chooseAndBuyUpgrades(buySomething: Boolean = false, minCost: Int = 0) {
        loadUpgrades()

        if (UpgradeService.isEmptyUpgradesCache)
            updateUpgrades()

        GameCommonAction.goToExchange()
        var coins = coinsAmount()
        var toBuy = calculateTarget(coins, buySomething, minCost = minCost)
        val exclude = mutableSetOf<String>()
        while (coins >= toBuy.cost) {
            logger.info { "Have money for upgrade [$coins / ${toBuy.cost}]: $toBuy" }
            val result = buyUpgrade(toBuy)
            if (!result) {
                logger.info { "Buy upgrade fail... Add to exclude" }
                exclude.add(toBuy.name)
            }
            GameCommonAction.goToExchange()
            coins = coinsAmount()
            toBuy = calculateTarget(coins, buySomething, exclude)
        }

        logger.info { "Not enough coins [$coins / ${toBuy.cost}] for: $toBuy" }
        GameCommonAction.goToExchange()
    }
}

fun main() {
    configureSession()
    chooseAndBuyUpgrades(true)
}
