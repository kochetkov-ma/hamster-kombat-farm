package org.brewcode.hamster.action

import com.codeborne.selenide.Condition
import io.github.oshai.kotlinlogging.KotlinLogging
import org.brewcode.hamster.Cfg.upgrade_cost_factor
import org.brewcode.hamster.action.GameCommonAction.goToBack
import org.brewcode.hamster.service.Upgrade
import org.brewcode.hamster.service.UpgradeSection
import org.brewcode.hamster.service.UpgradeService
import org.brewcode.hamster.service.UpgradeService.buyUpgrade
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
import org.brewcode.hamster.view.mine.block.UpgradeFullCardBlock

object GameMineAction {

    private val logger = KotlinLogging.logger {}

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

            UpgradeSection.None -> throw IllegalArgumentException("Cannot go to section: $section")
        }
    }

    fun loadCards(section: UpgradeSection): MutableMap<String, Upgrade> {
        val map = readSmallCards(section).toMutableMap()
        var index = 0
        while (MainView.hamsterButton.has(Condition.hidden) && index < 10) {
            scrollToLastVisibleCard(section)
            map.putAll(readSmallCards(section, exclude = map.keys))
            index++
        }

        return map
    }

    fun buyUpgradeCard(upgrade: Upgrade): Upgrade {
        var card: SmallUpgradeCard? = findSmallCard(upgrade)
        var index = 0
        while (MainView.hamsterButton.has(Condition.hidden) && index < 10 && card == null) {
            scrollToLastVisibleCard(upgrade.section)
            card = findSmallCard(upgrade)
            index++
        }

        if (card == null) {
            logger.error { "Cannot find card for $upgrade" }
            return upgrade
        }

        try {
            card.openCard()
        } catch (err: Throwable) {
            logger.error { "Cannot open card for $upgrade" }
            return upgrade
        }

        val fullCard = UpgradeFullCardBlock(upgrade.name)
        fullCard.actionButton.shouldBe(Condition.visible)

        val btnText = fullCard.actionButton.text
        if (btnText != "Go ahead") {
            logger.info { "Button: ${btnText.ifBlank { ". . ." }}! Need update data or wait: $upgrade" }
            goToBack()
            return upgrade
        }

        fullCard.actionButton.click()
        if (fullCard.actionButton.isDisplayed)
            runCatching { fullCard.actionButton.text == "Take the prize" }
                .onSuccess { fullCard.actionButton.click() }

        fullCard.actionButton.shouldBe(Condition.hidden)
        logger.info { "Bought upgrade! $upgrade" }
        UpgradeService.saveToHistory(upgrade)
        return card.toUpgrade(upgrade)
    }

    fun chooseAndBuyUpgrades() {
        loadUpgrades()

        if (UpgradeService.isEmptyUpgradesCache)
            updateUpgrades()

        runCatching { GameCommonAction.goToExchange() }
        var coins = coinsAmount()
        val calculator = UpgradeService.upgradeCalculator()

        var toBuy = if (UpgradeService.hasToBuy()) UpgradeService.toBuy() else calculator.calculate(coins)
        if (toBuy.needSaveMoney(coins)) {
            logger.info { "Found upgrade to need save coins a few iterations. Let's saving [$coins / ${toBuy.cost}] for: $toBuy" }
            GameCommonAction.goToExchange()
            UpgradeService.toBuy(toBuy)
            return
        }

        while (toBuy.canBuy(coins)) {
            logger.info { "Have money for upgrade [$coins / ${toBuy.cost}]: $toBuy" }

            val result = runCatching { buyUpgrade(toBuy) }.getOrElse { false }.also { UpgradeService.clearToBuy() }
            if (!result) {
                logger.info { "Buy upgrade fail... Add to exclude" }
                calculator.exclude(toBuy.name)
                calculator.costBackPressureFactorUp()
            }
            GameCommonAction.goToExchange()
            coins = coinsAmount()
            toBuy = calculator.calculate(coins)
        }

        logger.info { "Not enough coins [$coins / ${toBuy.cost}] for: $toBuy" }
        GameCommonAction.goToExchange()
    }

    private fun Upgrade.needSaveMoney(coins: Int) = cost in coins..(coins * upgrade_cost_factor).toInt()
    private fun Upgrade.canBuy(coins: Int) = cost <= coins && this != Upgrade.none
}

fun main() {
    configureSession()
    GameMineAction.buyUpgradeCard(Upgrade(UpgradeSection.PrTeam, "BTC pairs", 1, 1, 1, 1, ""))
    // GameMineAction.buyUpgradeCard(Upgrade(UpgradeSection.PrTeam, "Tokenomics expert", 1, 1, 1, 1, ""))
}
