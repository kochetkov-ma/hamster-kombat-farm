package org.brewcode.hamster.action

import com.codeborne.selenide.Condition.*
import io.github.oshai.kotlinlogging.KotlinLogging
import org.brewcode.hamster.Cfg.upgrade_cost_factor
import org.brewcode.hamster.action.GameCommonAction.goToBack
import org.brewcode.hamster.action.GameCommonAction.goToMine
import org.brewcode.hamster.action.GameMineAction.goToSection
import org.brewcode.hamster.service.Upgrade
import org.brewcode.hamster.service.UpgradeSection
import org.brewcode.hamster.service.UpgradeSection.*
import org.brewcode.hamster.service.UpgradeService
import org.brewcode.hamster.service.UpgradeService.buyUpgrade
import org.brewcode.hamster.service.UpgradeService.loadUpgrades
import org.brewcode.hamster.service.UpgradeService.updateUpgrades
import org.brewcode.hamster.util.configureSession
import org.brewcode.hamster.util.sec
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
        logger.debug { "Go to section: $section" }
        when (section) {
            Markets -> MineView.topMenuBlock.markets.click()
            PrTeam -> MineView.topMenuBlock.prTeam.click()
            Legal -> MineView.topMenuBlock.legal.click()
            SpecialsMy -> {
                MineView.topMenuBlock.specials.click()
                MineView.additionalMenuBlock.myCards.click()
            }

            SpecialsNew -> {
                MineView.topMenuBlock.specials.click()
                MineView.additionalMenuBlock.newCards.click()
            }

            None -> throw IllegalArgumentException("Cannot go to section: $section")
        }
    }

    fun loadCards(section: UpgradeSection): MutableMap<String, Upgrade> {
        val map = readSmallCards(section).toMutableMap()
        var index = 0
        while (MainView.hamsterButton.has(hidden) && index < 10) {
            scrollToLastVisibleCard(section)
            map.putAll(readSmallCards(section, exclude = map.keys))
            index++
        }

        return map
    }

    fun buyUpgradeCard(upgrade: Upgrade, dryRun: Boolean = false): Upgrade {
        var card: SmallUpgradeCard? = findSmallCard(upgrade, dryRun)
        var index = 0
        while (MainView.hamsterButton.has(hidden) && index < 10 && card == null) {
            scrollToLastVisibleCard(upgrade.section, dryRun)
            card = findSmallCard(upgrade, dryRun)
            index++
        }

        if (card == null) {
            logger.error { "Cannot find card after scrolling... $upgrade" }
            return upgrade
        }

        try {
            card.openCard()
        } catch (err: Throwable) {
            logger.error { "Card found. But cannot be open: $err... $upgrade" }
            return upgrade
        }

        val fullCard = UpgradeFullCardBlock(upgrade.name)
        try {
            fullCard.actionButton.shouldBe(visible, 5.sec)
        } catch (err: Throwable) {
            logger.error { "Click on card but it is not open. It's unavailable because countdown ... $upgrade" }
            return upgrade
        }

        val btnText = fullCard.actionButton.text
        if (btnText != "Go ahead") {
            logger.warn { "Button text: '${btnText.ifBlank { ". . ." }}'! Need update data or wait: $upgrade" }
            goToBack()
            return upgrade
        }

        if (!dryRun) {
            fullCard.actionButton.click()
            fullCard.actionButton.shouldBe(hidden)
        } else {
            fullCard.actionButton.shouldBe(clickable)
            goToBack()
        }

        if (fullCard.actionButton.isDisplayed && fullCard.actionButton.text == "Take the prize") {
            if (!dryRun) {
                fullCard.actionButton.click()
                fullCard.actionButton.shouldBe(hidden)
            } else {
                fullCard.actionButton.shouldBe(clickable)
                goToBack()
            }
        }

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
            logger.warn { "Found upgrade to need save coins a few iterations. Let's saving [$coins / ${toBuy.cost}] for: $toBuy" }
            GameCommonAction.goToExchange()
            UpgradeService.toBuy(toBuy)
            return
        }

        while (toBuy.canBuy(coins)) {
            logger.debug { "Have money for upgrade [$coins / ${toBuy.cost}]: $toBuy" }

            val result = runCatching { buyUpgrade(toBuy) }.getOrElse { false }.also { UpgradeService.clearToBuy() }
            if (!result) {
                logger.warn { "Buy upgrade fail... Add to exclude" }
                calculator.exclude(toBuy.name)
                if (toBuy.cost > coins) {
                    val newLimit = calculator.costBackPressureFactorUp()
                    logger.debug { "Reduce maximum cost limit down to: $newLimit" }
                }
            }
            GameCommonAction.goToExchange()
            coins = coinsAmount()
            toBuy = calculator.calculate(coins)
        }

        logger.warn { "Not enough coins [$coins / ${toBuy.cost}] for: $toBuy" }
        GameCommonAction.goToExchange()
    }

    private fun Upgrade.needSaveMoney(coins: Int) = cost in coins..(coins * upgrade_cost_factor).toInt()
    private fun Upgrade.canBuy(coins: Int) = cost <= coins && this != Upgrade.none
}

fun main() {
    configureSession()

    // Hamster Kombat takes the lead
    // Mega Event

    goToMine()
    goToSection(SpecialsMy)
    GameMineAction.buyUpgradeCard(Upgrade(SpecialsMy, "Top 10 Global Ranking", 1, 1, 1, 1, ""), true)
    goToBack()
    GameCommonAction.goToExchange()

    goToMine()
    goToSection(SpecialsMy)
    GameMineAction.buyUpgradeCard(Upgrade(SpecialsMy, "Special Hamster Conference", 1, 1, 1, 1, ""), true)
    goToBack()
    GameCommonAction.goToExchange()

    goToMine()
    goToSection(PrTeam)
    GameMineAction.buyUpgradeCard(Upgrade(PrTeam, "Consensus Explorer pass", 1, 1, 1, 1, ""), true)
    goToBack()
    GameCommonAction.goToExchange()

    goToMine()
    goToSection(SpecialsMy)
    GameMineAction.buyUpgradeCard(Upgrade(SpecialsMy, "YouTube Gold Button", 1, 1, 1, 1, ""), true)
    goToBack()
    GameCommonAction.goToExchange()

    goToMine()
    goToSection(SpecialsMy)
    GameMineAction.buyUpgradeCard(Upgrade(SpecialsMy, "Bitcoin Pizza Day", 1, 1, 1, 1, ""), true)
    goToBack()
    GameCommonAction.goToExchange()

    goToMine()
    goToSection(Markets)
    GameMineAction.buyUpgradeCard(Upgrade(Markets, "Derivatives", 1, 1, 1, 1, ""), true)
    goToBack()
    GameCommonAction.goToExchange()

    goToMine()
    goToSection(PrTeam)
    GameMineAction.buyUpgradeCard(Upgrade(PrTeam, "Marketing", 1, 1, 1, 1, ""), true)
    goToBack()
    GameCommonAction.goToExchange()

    goToMine()
    goToSection(SpecialsMy)
    GameMineAction.buyUpgradeCard(Upgrade(SpecialsMy, "Hamster daily show", 1, 1, 1, 1, ""), true)
    goToBack()
    GameCommonAction.goToExchange()
}
