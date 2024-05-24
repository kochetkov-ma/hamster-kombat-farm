package org.brewcode.hamster.action

import com.codeborne.selenide.Condition
import org.brewcode.hamster.action.GameMineAction.chooseAndBuyUpgrades
import org.brewcode.hamster.service.Upgrade
import org.brewcode.hamster.service.UpgradeSection
import org.brewcode.hamster.service.UpgradeService
import org.brewcode.hamster.service.UpgradeService.buyUpgrade
import org.brewcode.hamster.service.UpgradeService.calculateTarget
import org.brewcode.hamster.service.UpgradeService.loadUpgrades
import org.brewcode.hamster.service.UpgradeService.updateUpgrades
import org.brewcode.hamster.util.configureSession
import org.brewcode.hamster.view.HamsterKombatGameView
import org.brewcode.hamster.view.HamsterKombatGameView.coinsAmount
import org.brewcode.hamster.view.MineView
import org.brewcode.hamster.view.MineView.findSmallCard
import org.brewcode.hamster.view.MineView.readSmallCards
import org.brewcode.hamster.view.SmallUpgradeCard
import org.brewcode.hamster.view.block.CommonBlock
import org.brewcode.hamster.view.block.CommonBlock.insufficientFunds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

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

    fun loadCards(): HashMap<String, Upgrade> {
        val map = HashMap<String, Upgrade>()

        var index = 0
        while (HamsterKombatGameView.hamsterButton.has(Condition.hidden) && index < 10) {
            map.putAll(readSmallCards(exclude = map.keys))
            index++
        }

        return map
    }

    fun buyUpgradeCard(upgrade: Upgrade): Upgrade {
        var card: SmallUpgradeCard? = null
        var index = 0
        while (HamsterKombatGameView.hamsterButton.has(Condition.hidden) && index < 10) {
            card = findSmallCard(upgrade.name)
            if (card != null) break
            index++
        }

        if (card == null) {
            UpgradeService.remove(upgrade)
            logger.info { "Cannot find card for $upgrade" }
            return upgrade
        }

        logger.info { "Card found for $upgrade" }

        card.openCard()
        if (insufficientFunds.isDisplayed) {
            logger.info { "Insufficient Funds! Need update data: $upgrade" }
            return card.toUpgrade(upgrade)
        }

        CommonBlock.applyButton.click()
        CommonBlock.applyButtonDiv.shouldBe(Condition.hidden, 30.seconds.toJavaDuration())

        logger.info { "Bought upgrade! $upgrade" }

        return card.toUpgrade(upgrade)
    }

    fun chooseAndBuyUpgrades(buySomething: Boolean = false) {
        loadUpgrades()

        if (UpgradeService.isEmptyUpgradesCache)
            updateUpgrades()

        GameCommonAction.goToExchange()
        var coins = coinsAmount()
        var toBuy = calculateTarget(coins, buySomething)
        while (coins >= toBuy.cost) {
            logger.info { "Have money for upgrade [$coins / ${toBuy.cost}]: $toBuy" }
            buyUpgrade(toBuy)
            GameCommonAction.goToExchange()
            coins = coinsAmount()
            toBuy = calculateTarget(coins, buySomething)
        }

        logger.info { "Not enough coins [$coins / ${toBuy.cost}] for: $toBuy" }
        GameCommonAction.goToExchange()
    }
}

fun main() {
    configureSession()

    chooseAndBuyUpgrades(true)
}
